package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.reputation.ReputationServiceI;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

public class CombineSummarizedResults {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ReputationServiceI.class));
   private DirectoryHolder directoryHolder;

   public CombineSummarizedResults(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   public static List<String> runDatesStringForTheWeekSoFar(Date lastDate) {
      List<String> dates = new ArrayList();
      Calendar lastDateCal = Calendar.getInstance();
      lastDateCal.setTime(DateTimeUtils.midnightOnDate(lastDate));
      int currentDayOfWeek = lastDateCal.get(7);
      dates.add(ReputationServiceI.FILE_DATE_FORMAT.format(lastDateCal.getTime()));

      while(true) {
         --currentDayOfWeek;
         if (currentDayOfWeek <= 0) {
            return dates;
         }

         lastDateCal.add(6, -1);
         dates.add(0, ReputationServiceI.FILE_DATE_FORMAT.format(lastDateCal.getTime()));
      }
   }

   public static String outputFilename(String prefix, List<String> dates) {
      return "combined." + prefix + (String)dates.get(0) + "-" + (String)dates.get(dates.size() - 1);
   }

   public String combine(Date lastDate) throws IOException {
      List<String> dates = runDatesStringForTheWeekSoFar(lastDate);
      log.info("combining results up to [" + lastDate + "]: " + dates);
      String outputFile = null;
      if (dates.size() < 2) {
         log.info("only one file to combine, NO OP, " + dates);
         return outputFile;
      } else {
         outputFile = outputFilename("scored.", dates);
         BufferedWriter combinedFile = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + outputFile));

         for(int i = 0; i < dates.size(); ++i) {
            try {
               BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + this.directoryHolder.getScoreFilename((String)dates.get(i))));

               for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                  combinedFile.write(line);
                  combinedFile.newLine();
               }

               reader.close();
            } catch (FileNotFoundException var8) {
               log.error("file [" + this.directoryHolder.getScoreFilename((String)dates.get(i)) + "] not found, leaving it out of the combined logs", var8);
            }
         }

         combinedFile.close();
         return outputFile;
      }
   }

   public static void main(String[] args) throws ParseException, IOException {
      CombineSummarizedResults combiner = new CombineSummarizedResults(DirectoryUtils.getDirectoryHolder());
      Date date = ReputationServiceI.FILE_DATE_FORMAT.parse("2009-07-11T00:00:01");
      System.out.println(runDatesStringForTheWeekSoFar(date));
      date = ReputationServiceI.FILE_DATE_FORMAT.parse("2009-07-12T00:00:01");
      System.out.println(runDatesStringForTheWeekSoFar(date));
      date = ReputationServiceI.FILE_DATE_FORMAT.parse("2009-07-13T00:00:01");
      System.out.println(runDatesStringForTheWeekSoFar(date));
      date = ReputationServiceI.FILE_DATE_FORMAT.parse("2009-07-18T00:00:01");
      System.out.println(runDatesStringForTheWeekSoFar(date));
      combiner.combine(DateTimeUtils.midnightTomorrow());
   }
}
