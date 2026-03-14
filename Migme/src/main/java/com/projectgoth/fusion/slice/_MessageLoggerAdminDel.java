package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _MessageLoggerAdminDel extends _ObjectDel {
   MessageLoggerStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
