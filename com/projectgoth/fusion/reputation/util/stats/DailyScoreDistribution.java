package com.projectgoth.fusion.reputation.util.stats;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.cache.ScoreFormulaParameters;
import com.projectgoth.fusion.reputation.file.ScoreSummary;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;

public class DailyScoreDistribution {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DailyScoreDistribution.class));
   private static MemCachedClient memCached;
   private DirectoryHolder directoryHolder;
   private ScoreFormulaParameters scoreFormulaParameters;

   public DailyScoreDistribution(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
      this.scoreFormulaParameters = (ScoreFormulaParameters)memCached.get(MemCachedUtils.getCacheKeyInNamespace("REP", "SCFP"));
   }

   public static void initializeMap(SortedMap<Integer, Integer> scoreDistribution, int scoreCap, int scoreDistributionStep) {
      scoreDistribution.clear();

      for(int i = 0; i <= scoreCap / scoreDistributionStep + 1; ++i) {
         scoreDistribution.put(i * scoreDistributionStep, 0);
         System.out.println(i * scoreDistributionStep);
      }

   }

   private static int findMapKey(SortedMap<Integer, Integer> scoreDistribution, int score) {
      Iterator<Integer> ite = scoreDistribution.keySet().iterator();
      int keyValue = (Integer)ite.next();

      int previousKeyValue;
      for(previousKeyValue = 0; ite.hasNext() && score >= keyValue; keyValue = (Integer)ite.next()) {
         previousKeyValue = keyValue;
      }

      return previousKeyValue;
   }

   public static void populateMap(SortedMap<Integer, Integer> scoreDistribution, int score) {
      int mapKey = findMapKey(scoreDistribution, score);
      scoreDistribution.put(mapKey, (Integer)scoreDistribution.get(mapKey) + 1);
   }

   private void processFile(String filename) throws IOException {
      log.info("distributizing " + filename);
      SortedMap<Integer, Integer> distribution = new TreeMap();
      initializeMap(distribution, this.scoreFormulaParameters.getDailyHardCap(), 20);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      int lineNumber = 0;
      boolean var9 = false;

      String line;
      while((line = reader.readLine()) != null) {
         List<String> parts = StringUtil.split(line, ',');
         if (parts.size() != ScoreSummary.EXPECTED_FIELD_COUNT) {
            log.info("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
         } else {
            int score = Integer.parseInt((String)parts.get(ScoreSummary.TOTAL_SCORE_INDEX));
            populateMap(distribution, score);
            ++lineNumber;
            if (lineNumber % '썐' == 0) {
               System.out.println("line: " + lineNumber);
            }
         }
      }

      long end = System.currentTimeMillis();
      log.info("done distributizing" + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
      Iterator i$ = distribution.keySet().iterator();

      while(i$.hasNext()) {
         Integer key = (Integer)i$.next();
         System.out.println(key + "," + distribution.get(key));
      }

   }

   public static void main(String[] args) {
      DailyScoreDistribution dist = new DailyScoreDistribution(DirectoryUtils.getDirectoryHolder());

      try {
         dist.processFile(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }

   static {
      memCached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }
}
