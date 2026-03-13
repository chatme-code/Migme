package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _JobSchedulingServiceAdminDel extends _ObjectDel {
   JobSchedulingServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
