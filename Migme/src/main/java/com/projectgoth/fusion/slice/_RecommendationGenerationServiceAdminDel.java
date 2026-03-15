package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _RecommendationGenerationServiceAdminDel extends _ObjectDel {
   RecommendationGenerationServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
