package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BotServiceAdminDel extends _ObjectDel {
   int ping(Map<String, String> var1) throws LocalExceptionWrapper;

   BotServiceStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
