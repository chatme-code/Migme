package com.projectgoth.fusion.slice;

import java.util.Map;

public interface ConnectionWSPrx extends ConnectionPrx {
   void accessed();

   void accessed(Map<String, String> var1);

   void addRemoteChildConnectionWS(String var1, ConnectionWSPrx var2);

   void addRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Map<String, String> var3);

   void removeRemoteChildConnectionWS(String var1, ConnectionWSPrx var2);

   void removeRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Map<String, String> var3);
}
