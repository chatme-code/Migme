package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BotHunterDel extends _ObjectDel {
   SuspectGroupIce[] getLatestSuspects(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
