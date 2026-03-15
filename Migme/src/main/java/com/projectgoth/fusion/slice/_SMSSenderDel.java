package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _SMSSenderDel extends _ObjectDel {
   void sendSMS(MessageDataIce var1, long var2, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void sendSystemSMS(SystemSMSDataIce var1, long var2, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
