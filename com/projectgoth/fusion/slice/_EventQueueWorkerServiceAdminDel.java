package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EventQueueWorkerServiceAdminDel extends _ObjectDel {
   EventQueueWorkerServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
