package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ObjectCacheAdminDel extends _ObjectDel {
   int ping(Map<String, String> var1) throws LocalExceptionWrapper;

   ObjectCacheStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   String[] getUsernames(Map<String, String> var1) throws LocalExceptionWrapper;

   void reloadEmotes(Map<String, String> var1) throws LocalExceptionWrapper;

   void setLoadWeightage(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   int getLoadWeightage(Map<String, String> var1) throws LocalExceptionWrapper;
}
