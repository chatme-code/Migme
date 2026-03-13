package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _SessionCacheDel extends _ObjectDel {
   void logSession(SessionIce var1, SessionMetricsIce var2, Map<String, String> var3) throws LocalExceptionWrapper;
}
