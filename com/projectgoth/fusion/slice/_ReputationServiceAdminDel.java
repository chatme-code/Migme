package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ReputationServiceAdminDel extends _ObjectDel {
   ReputationServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
