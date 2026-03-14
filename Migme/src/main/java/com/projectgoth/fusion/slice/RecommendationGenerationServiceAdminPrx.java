package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RecommendationGenerationServiceAdminPrx extends ObjectPrx {
   RecommendationGenerationServiceStats getStats() throws FusionException;

   RecommendationGenerationServiceStats getStats(Map<String, String> var1) throws FusionException;
}
