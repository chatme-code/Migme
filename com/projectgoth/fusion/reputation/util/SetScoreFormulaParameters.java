package com.projectgoth.fusion.reputation.util;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.reputation.cache.ScoreFormulaParameters;

public class SetScoreFormulaParameters {
   public static MemCachedClient memCached;

   public static void main(String[] args) {
      ScoreFormulaParameters params = new ScoreFormulaParameters();
      ScoreFormulaParameters.setScoreFormulaParameters(memCached, params);
   }

   static {
      memCached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }
}
