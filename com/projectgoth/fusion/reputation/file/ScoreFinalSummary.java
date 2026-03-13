package com.projectgoth.fusion.reputation.file;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.cache.ScoreFormulaParameters;
import com.projectgoth.fusion.reputation.domain.ScoreCategory;
import com.projectgoth.fusion.reputation.util.CSVUtils;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.stats.DailyScoreDistribution;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;

public class ScoreFinalSummary {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ScoreFinalSummary.class));
   private static final int METRIC_PROPORTIONATE_MAX = 100;
   private static final int CATEGORY_MAX = 100;
   public static final int SCORE_DISTRIBUTION_STEP = 20;
   public static MemCachedClient memCached;
   private ScoreFormulaParameters scoreFormulaParameters;
   private double[] reasonableMaximums;
   private int[] weights;
   private SortedMap<Double, Integer> bonuses = new TreeMap();
   private DirectoryHolder directoryHolder;

   public ScoreFinalSummary(DirectoryHolder directoryHolder) {
      this.scoreFormulaParameters = (ScoreFormulaParameters)memCached.get(MemCachedUtils.getCacheKeyInNamespace("REP", "SCFP"));
      if (this.scoreFormulaParameters == null) {
         this.scoreFormulaParameters = new ScoreFormulaParameters();
         ScoreFormulaParameters.setScoreFormulaParameters(memCached, this.scoreFormulaParameters);
      }

      this.updateReasonableMaximums();
      this.updateWeights();
      this.updateBonuses();
      this.directoryHolder = directoryHolder;
   }

   private void updateReasonableMaximums() {
      this.reasonableMaximums = new double[ScoreSummary.EXPECTED_INPUT_FIELD_COUNT];
      log.info("reasonable maximums");
      this.reasonableMaximums[1] = (double)this.scoreFormulaParameters.getChatRoomMessagesSentReasonableMax();
      log.info("chatroom messages sent: " + this.reasonableMaximums[1]);
      this.reasonableMaximums[2] = (double)this.scoreFormulaParameters.getPrivateMessagesSentReasonableMax();
      log.info("private messages sent: " + this.reasonableMaximums[2]);
      this.reasonableMaximums[3] = (double)this.scoreFormulaParameters.getTotalTimeReasonableMax();
      log.info("total time: " + this.reasonableMaximums[3]);
      this.reasonableMaximums[4] = (double)this.scoreFormulaParameters.getPhotosUploadedReasonableMax();
      log.info("photos uploaded: " + this.reasonableMaximums[4]);
      this.reasonableMaximums[5] = (double)this.scoreFormulaParameters.getKicksInitiatedReasonableMax();
      log.info("kicks: " + this.reasonableMaximums[5]);
      this.reasonableMaximums[6] = (double)this.scoreFormulaParameters.getAuthenticatedReferralsReasonableMax();
      log.info("authenticated referrals: " + this.reasonableMaximums[6]);
      this.reasonableMaximums[7] = (double)this.scoreFormulaParameters.getRechargedAmountReasonableMax();
      log.info("recharged amount: " + this.reasonableMaximums[7]);
      this.reasonableMaximums[8] = (double)this.scoreFormulaParameters.getVirtualGiftsReceivedReasonableMax();
      log.info("virtual gifts received: " + this.reasonableMaximums[8]);
      this.reasonableMaximums[9] = (double)this.scoreFormulaParameters.getVirtualGiftsSentReasonableMax();
      log.info("virtual gifts sent: " + this.reasonableMaximums[9]);
      this.reasonableMaximums[10] = (double)this.scoreFormulaParameters.getPhoneCallDurationReasonableMax();
      log.info("call duration: " + this.reasonableMaximums[10]);
   }

   private void updateWeights() {
      this.weights = new int[ScoreSummary.EXPECTED_INPUT_FIELD_COUNT];
      this.weights[1] = this.scoreFormulaParameters.getChatRoomMessagesSentWeight();
      this.weights[2] = this.scoreFormulaParameters.getPrivateMessagesSentWeight();
      this.weights[3] = this.scoreFormulaParameters.getTotalTimeWeight();
      this.weights[4] = this.scoreFormulaParameters.getPhotosUploadedWeight();
      this.weights[5] = this.scoreFormulaParameters.getKicksInitiatedWeight();
      this.weights[6] = this.scoreFormulaParameters.getAuthenticatedReferralsWeight();
      this.weights[7] = this.scoreFormulaParameters.getRechargedAmountWeight();
      this.weights[8] = this.scoreFormulaParameters.getVirtualGiftsReceivedWeight();
      this.weights[9] = this.scoreFormulaParameters.getVirtualGiftsSentWeight();
      this.weights[10] = this.scoreFormulaParameters.getPhoneCallDurationWeight();
   }

   private void updateBonuses() {
      this.bonuses.clear();
      this.bonuses.put(0.5D, this.scoreFormulaParameters.getReasonableMaximumMinus50());
      this.bonuses.put(0.45D, this.scoreFormulaParameters.getReasonableMaximumMinus45());
      this.bonuses.put(0.4D, this.scoreFormulaParameters.getReasonableMaximumMinus40());
      this.bonuses.put(0.35D, this.scoreFormulaParameters.getReasonableMaximumMinus35());
      this.bonuses.put(0.3D, this.scoreFormulaParameters.getReasonableMaximumMinus30());
      this.bonuses.put(0.25D, this.scoreFormulaParameters.getReasonableMaximumMinus25());
      this.bonuses.put(0.2D, this.scoreFormulaParameters.getReasonableMaximumMinus20());
      this.bonuses.put(0.15D, this.scoreFormulaParameters.getReasonableMaximumMinus15());
      this.bonuses.put(0.1D, this.scoreFormulaParameters.getReasonableMaximumMinus10());
   }

   private int[] score(List<String> parts) {
      int[] scores = new int[ScoreSummary.EXPECTED_INPUT_FIELD_COUNT + 4 + 1];
      int[] categoryScores = new int[ScoreCategory.values().length];

      int i;
      for(i = 1; i < parts.size(); ++i) {
         scores[i] = Integer.parseInt((String)parts.get(i));
      }

      for(i = 1; i < ScoreSummary.EXPECTED_INPUT_FIELD_COUNT; ++i) {
         scores[i] = (int)Math.round((double)scores[i] / this.reasonableMaximums[i] * 100.0D);
         if (scores[i] < 0) {
            scores[i] = 0;
         }
      }

      Set<Double> keys = this.bonuses.keySet();
      Iterator<Double> keyIterator = keys.iterator();
      double previousKey = 1.0D;

      int i;
      for(i = 1; i < ScoreSummary.EXPECTED_INPUT_FIELD_COUNT; ++i) {
         while(keyIterator.hasNext()) {
            Double key = (Double)keyIterator.next();
            if ((double)scores[i] < key * this.reasonableMaximums[i]) {
               break;
            }

            previousKey = key;
         }

         if (previousKey > (Double)this.bonuses.lastKey() && previousKey <= (Double)this.bonuses.firstKey()) {
            log.debug("awarding bonus for part " + i + " " + scores[i] + " -> " + previousKey + " = " + this.bonuses.get(previousKey));
            scores[i] += (Integer)this.bonuses.get(previousKey);
         }
      }

      for(i = 1; i < ScoreSummary.EXPECTED_INPUT_FIELD_COUNT; ++i) {
         if (scores[i] > 100) {
            scores[i] = 100;
         }
      }

      for(i = 1; i < ScoreSummary.EXPECTED_INPUT_FIELD_COUNT; ++i) {
         scores[i] *= this.weights[i];
      }

      categoryScores[ScoreCategory.TIME_IN_PRODUCT.value()] = scores[3];
      categoryScores[ScoreCategory.CREDITS_SPENT.value()] = scores[7];
      int var10001 = ScoreCategory.CREDITS_SPENT.value();
      categoryScores[var10001] += scores[9];
      var10001 = ScoreCategory.CREDITS_SPENT.value();
      categoryScores[var10001] += scores[10];
      categoryScores[ScoreCategory.HUMAN_LIKELY_BEHAVIOUR.value()] = scores[4];
      var10001 = ScoreCategory.HUMAN_LIKELY_BEHAVIOUR.value();
      categoryScores[var10001] += scores[8];
      var10001 = ScoreCategory.HUMAN_LIKELY_BEHAVIOUR.value();
      categoryScores[var10001] += scores[6];
      categoryScores[ScoreCategory.BASIC_ACTIVITY.value()] = Math.min(scores[1], 75);
      var10001 = ScoreCategory.BASIC_ACTIVITY.value();
      categoryScores[var10001] += Math.min(scores[2], 75);

      for(i = 0; i < categoryScores.length; ++i) {
         if (categoryScores[i] > 100) {
            categoryScores[i] = 100;
         }
      }

      scores[ScoreSummary.TIME_IN_PRODUCT_CATEGORY_INDEX] = categoryScores[ScoreCategory.TIME_IN_PRODUCT.value()];
      scores[ScoreSummary.CREDITS_SPENT_CATEGORY_INDEX] = categoryScores[ScoreCategory.CREDITS_SPENT.value()];
      scores[ScoreSummary.HUMAN_LIKE_BEHAVIOUR_CATEGORY_INDEX] = categoryScores[ScoreCategory.HUMAN_LIKELY_BEHAVIOUR.value()];
      scores[ScoreSummary.BASIC_ACTIVITY_CATEGORY_INDEX] = categoryScores[ScoreCategory.BASIC_ACTIVITY.value()];
      scores[ScoreSummary.TOTAL_SCORE_INDEX] = 0;

      for(i = 0; i < categoryScores.length; ++i) {
         var10001 = ScoreSummary.TOTAL_SCORE_INDEX;
         scores[var10001] += categoryScores[i];
      }

      if (scores[ScoreSummary.TOTAL_SCORE_INDEX] > this.scoreFormulaParameters.getDailyHardCap()) {
         scores[ScoreSummary.TOTAL_SCORE_INDEX] = this.scoreFormulaParameters.getDailyHardCap();
      }

      return scores;
   }

   public SortedMap<Integer, Integer> scoreFile(String filename) throws IOException {
      log.info("scoring " + filename);
      SortedMap<Integer, Integer> scoreDistribution = new TreeMap();
      DailyScoreDistribution.initializeMap(scoreDistribution, this.scoreFormulaParameters.getDailyHardCap(), 20);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + "scored." + filename));

      while(true) {
         String line;
         while((line = reader.readLine()) != null) {
            List<String> parts = StringUtil.split(line, ',');
            if (parts.size() != ScoreSummary.EXPECTED_INPUT_FIELD_COUNT) {
               log.warn("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
            } else {
               int[] scores = this.score(parts);
               writer.write(CSVUtils.getColumnFromLine(line, 0, ','));
               writer.write(44);

               for(int i = 1; i < scores.length; ++i) {
                  writer.write(Integer.toString(scores[i]));
                  if (i < scores.length - 1) {
                     writer.write(44);
                  }
               }

               writer.newLine();
               DailyScoreDistribution.populateMap(scoreDistribution, scores[ScoreSummary.TOTAL_SCORE_INDEX]);
            }
         }

         writer.flush();
         writer.close();
         long end = System.currentTimeMillis();
         log.info("done scoring " + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
         scoreDistribution.remove(scoreDistribution.lastKey());
         return scoreDistribution;
      }
   }

   public static void main(String[] args) {
      ScoreFinalSummary scorer = new ScoreFinalSummary(DirectoryUtils.getDirectoryHolder());

      try {
         scorer.scoreFile(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }

   static {
      memCached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }
}
