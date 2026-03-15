package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _RegistryAdminDel extends _ObjectDel {
   RegistryStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
