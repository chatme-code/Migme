package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _SessionCacheAdminDel extends _ObjectDel {
   SessionCacheStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
