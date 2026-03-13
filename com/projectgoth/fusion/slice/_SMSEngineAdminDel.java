package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _SMSEngineAdminDel extends _ObjectDel {
   SMSEngineStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
