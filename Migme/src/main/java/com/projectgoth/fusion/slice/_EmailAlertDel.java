package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EmailAlertDel extends _ObjectDel {
   void requestUnreadEmailCount(String var1, String var2, UserPrx var3, Map<String, String> var4) throws LocalExceptionWrapper;
}
