package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _RecommendationDataCollectionServiceAdminDel extends _ObjectDel {
   RecommendationDataCollectionServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionExceptionWithRefCode;
}
