package com.projectgoth.fusion.voiceengine;

public interface AsteriskListener {
   void asteriskDisconnected(String var1);

   void asteriskEventReceived(AsteriskCommand var1);
}
