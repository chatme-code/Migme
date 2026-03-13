package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.StringUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SummarizeSortedSessionArchiveFileByStartTime {
   public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   public static final int BRACKET_SIZE_IN_MINUTES = 5;

   private void increaseTally(SortedMap<Long, AtomicInteger> map, long bracket) {
      if (!map.containsKey(bracket)) {
         map.put(bracket, new AtomicInteger(1));
      } else {
         ((AtomicInteger)map.get(bracket)).incrementAndGet();
      }

   }

   private void processFile(String filename) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename + ".timebrackets"));
      long currentBracketStartDate = DateTimeUtils.minusDays(DateTimeUtils.midnightToday(), 18).getTime() / 1000L;
      long currentBracketEndDate = currentBracketStartDate + 300L;
      System.out.println("starting at " + new Date(currentBracketStartDate * 1000L));
      TreeMap map = new TreeMap();

      String line;
      while((line = reader.readLine()) != null) {
         List<String> parts = StringUtil.split(line, '|');
         if (parts.size() != 26) {
            System.out.println("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
         } else {
            long startDate = Long.parseLong((String)parts.get(3));
            int country = Integer.parseInt((String)parts.get(2));
            if (startDate > currentBracketEndDate) {
               currentBracketStartDate += 300L;
               currentBracketEndDate += 300L;
            }

            if (country == 107 && startDate > currentBracketStartDate) {
               this.increaseTally(map, currentBracketStartDate);
            }
         }
      }

      System.out.println("ending at " + new Date(currentBracketStartDate));
      int totalUsers = 0;
      Iterator i$ = map.keySet().iterator();

      while(i$.hasNext()) {
         long bracket = (Long)i$.next();
         System.out.println(new Date(bracket * 1000L) + " -> " + map.get(bracket));
         totalUsers += ((AtomicInteger)map.get(bracket)).intValue();
         writer.write(DATE_FORMAT.format(new Date(bracket * 1000L)));
         writer.write(",");
         writer.write(((AtomicInteger)map.get(bracket)).toString());
         writer.newLine();
      }

      System.out.println("total users " + totalUsers);
      writer.flush();
      writer.close();
   }

   public void go(String filename) throws IOException {
      this.processFile(filename);
   }

   public static void main(String[] args) {
      SummarizeSortedSessionArchiveFileByStartTime summarizeFile = new SummarizeSortedSessionArchiveFileByStartTime();

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
