package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RecommendationGenerationServicePrx extends ObjectPrx {
   void runTransformation(int var1);

   void runTransformation(int var1, Map<String, String> var2);
}
