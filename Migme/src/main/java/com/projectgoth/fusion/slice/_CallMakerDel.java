package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _CallMakerDel extends _ObjectDel {
   CallDataIce requestCallback(CallDataIce var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
