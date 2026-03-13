package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface SessionCachePrx extends ObjectPrx {
   void logSession(SessionIce var1, SessionMetricsIce var2);

   void logSession(SessionIce var1, SessionMetricsIce var2, Map<String, String> var3);
}
