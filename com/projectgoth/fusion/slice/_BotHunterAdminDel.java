package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BotHunterAdminDel extends _ObjectDel {
   BotHunterStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
