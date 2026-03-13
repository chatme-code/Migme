package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EventSystemAdminDel extends _ObjectDel {
   EventSystemStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
