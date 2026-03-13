package com.projectgoth.fusion.reputation.util.stats;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.file.ScoreSummary;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;

public class CombinedScoreDistribution {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CombinedScoreDistribution.class));
   private int totalUniqueUsers = 0;
   private DirectoryHolder directoryHolder;
   private SortedMap<Integer, Integer> map = new TreeMap();
   private final int step = 20;

   public CombinedScoreDistribution(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   public void initializeMap() {
      this.map.clear();

      for(int i = 1; i <= 15; ++i) {
         this.map.put(i * 20, 0);
         System.out.println(i * 20);
      }

   }

   public int findMapKey(int score) {
      Iterator<Integer> ite = this.map.keySet().iterator();

      int keyValue;
      for(keyValue = (Integer)ite.next(); ite.hasNext() && score > keyValue; keyValue = (Integer)ite.next()) {
      }

      return keyValue;
   }

   private void processFile(String filename) throws IOException {
      log.info("distributizing " + filename);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + filename + ".distribution"));
      String previousUsername = null;
      int score = 0;

      String line;
      while((line = reader.readLine()) != null) {
         List<String> parts = StringUtil.split(line, ',');
         if (parts.size() != ScoreSummary.EXPECTED_FIELD_COUNT) {
            log.info("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
         } else {
            String username = (String)parts.get(0);
            score += Integer.parseInt((String)parts.get(ScoreSummary.TOTAL_SCORE_INDEX));
            if (previousUsername != null && !username.equals(previousUsername)) {
            }

            previousUsername = username;
         }
      }

      writer.flush();
      writer.close();
      long end = System.currentTimeMillis();
      log.info("done summarizing" + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
      log.info("total unique users " + this.totalUniqueUsers);
   }

   public void go(String filename) throws IOException {
      this.processFile(filename);
   }

   public static void main(String[] args) {
      CombinedScoreDistribution dist = new CombinedScoreDistribution(DirectoryUtils.getDirectoryHolder());

      try {
         dist.initializeMap();
         System.out.println(dist.findMapKey(0));
         System.out.println(dist.findMapKey(6));
         System.out.println(dist.findMapKey(25));
         System.out.println(dist.findMapKey(200));
         System.out.println(dist.findMapKey(299));
         System.out.println(dist.findMapKey(300));
         System.out.println(dist.findMapKey(301));
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
