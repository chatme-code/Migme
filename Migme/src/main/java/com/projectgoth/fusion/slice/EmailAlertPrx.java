package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EmailAlertPrx extends ObjectPrx {
   void requestUnreadEmailCount(String var1, String var2, UserPrx var3);

   void requestUnreadEmailCount(String var1, String var2, UserPrx var3, Map<String, String> var4);
}
