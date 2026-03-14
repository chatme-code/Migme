package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _RecommendationDataCollectionServiceDel extends _ObjectDel {
   void logData(CollectedDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionExceptionWithRefCode;
}
