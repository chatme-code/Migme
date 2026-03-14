package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface SMSSenderPrx extends ObjectPrx {
   void sendSMS(MessageDataIce var1, long var2) throws FusionException;

   void sendSMS(MessageDataIce var1, long var2, Map<String, String> var4) throws FusionException;

   void sendSystemSMS(SystemSMSDataIce var1, long var2) throws FusionException;

   void sendSystemSMS(SystemSMSDataIce var1, long var2, Map<String, String> var4) throws FusionException;
}
