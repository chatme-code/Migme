package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface MessageLoggerPrx extends ObjectPrx {
   void logMessage(int var1, int var2, String var3, String var4, int var5, String var6);

   void logMessage(int var1, int var2, String var3, String var4, int var5, String var6, Map<String, String> var7);
}
