package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EmailAlertAdminDel extends _ObjectDel {
   EmailAlertStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
