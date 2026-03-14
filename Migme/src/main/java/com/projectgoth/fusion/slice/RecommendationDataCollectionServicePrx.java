package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RecommendationDataCollectionServicePrx extends ObjectPrx {
   void logData(CollectedDataIce var1) throws FusionExceptionWithRefCode;

   void logData(CollectedDataIce var1, Map<String, String> var2) throws FusionExceptionWithRefCode;
}
