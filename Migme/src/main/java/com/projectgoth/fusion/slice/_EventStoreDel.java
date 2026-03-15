package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _EventStoreDel extends _ObjectDel {
   void storeUserEvent(String var1, UserEventIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void storeGeneratorEvent(String var1, UserEventIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   UserEventIce[] getUserEventsForUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   UserEventIce[] getUserEventsGeneratedByUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void deleteUserEvents(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   EventPrivacySettingIce getPublishingPrivacyMask(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void setPublishingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   EventPrivacySettingIce getReceivingPrivacyMask(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void setReceivingPrivacyMask(String var1, EventPrivacySettingIce var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;
}
