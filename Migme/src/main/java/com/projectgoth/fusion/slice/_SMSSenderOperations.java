package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _SMSSenderOperations {
   void sendSMS(MessageDataIce var1, long var2, Current var4) throws FusionException;

   void sendSystemSMS(SystemSMSDataIce var1, long var2, Current var4) throws FusionException;
}
