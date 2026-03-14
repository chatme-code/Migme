package com.projectgoth.fusion.slice;

public interface _SMSSenderOperationsNC {
   void sendSMS(MessageDataIce var1, long var2) throws FusionException;

   void sendSystemSMS(SystemSMSDataIce var1, long var2) throws FusionException;
}
