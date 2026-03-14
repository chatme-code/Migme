package com.projectgoth.fusion.reputation.util;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.ReputationServiceI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SplitByDay {
   public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

   public static String getOutputFilename(String prefix, long date) {
      return prefix + ReputationServiceI.FILE_DATE_FORMAT.format(new Date(date)) + ".csv";
   }

   public static void dumpLine(BufferedWriter writer, String line) throws IOException {
      writer.write(line);
      writer.newLine();
   }

   public static long lineDate(String line, char delimeter, int dateIndex) {
      List<String> lineParts = StringUtil.split(line, delimeter);
      return Long.parseLong((String)lineParts.get(dateIndex)) * 1000L;
   }

   public static void main(String[] args) throws IOException, ParseException {
      if (args.length < 5) {
         System.err.println("Usage SplitByDay <filename> <prefix> <delimeter> <dateIndex> <firstDate>");
         System.exit(1);
      }

      String filename = args[0];
      String prefix = args[1];
      char delimeter = args[2].charAt(0);
      int dateIndex = Integer.parseInt(args[3]);
      Date firstDate = DATE_FORMAT.parse(args[4]);
      long[] dates = new long[30];
      dates[0] = firstDate.getTime();

      for(int i = 1; i < 30; ++i) {
         dates[i] = dates[i - 1] + 86400000L;
         System.out.println("adding date boundary " + new Date(dates[i]));
      }

      BufferedReader reader = new BufferedReader(new FileReader(filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(getOutputFilename(prefix, dates[0])));
      String line = null;
      String lastLine = null;

      for(int currentDateIndex = 0; (line = reader.readLine()) != null; lastLine = line) {
         long lineDate = lineDate(line, delimeter, dateIndex);
         if (lineDate < dates[currentDateIndex]) {
            dumpLine(writer, line);
         } else {
            writer.flush();
            writer.close();
            System.out.println("done with " + new Date(dates[currentDateIndex]) + " last line date was " + new Date(lineDate(lastLine == null ? line : lastLine, delimeter, dateIndex)));
            ++currentDateIndex;
            writer = new BufferedWriter(new FileWriter(getOutputFilename(prefix, dates[currentDateIndex])));
            dumpLine(writer, line);
            System.out.println("first line date of new file is " + new Date(lineDate));
         }
      }

      writer.flush();
      writer.close();
   }
}
