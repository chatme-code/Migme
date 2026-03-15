package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RecommendationDataCollectionServiceAdminPrx extends ObjectPrx {
   RecommendationDataCollectionServiceStats getStats() throws FusionExceptionWithRefCode;

   RecommendationDataCollectionServiceStats getStats(Map<String, String> var1) throws FusionExceptionWithRefCode;
}
