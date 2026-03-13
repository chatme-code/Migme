package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _EventStoreOperations {
   void storeUserEvent(String var1, UserEventIce var2, Current var3) throws FusionException;

   void storeGeneratorEvent(String var1, UserEventIce var2, Current var3) throws FusionException;

   UserEventIce[] getUserEventsForUser(String var1, Current var2) throws FusionException;

   UserEventIce[] getUserEventsGeneratedByUser(String var1, Current var2) throws FusionException;

   void deleteUserEvents(String var1, Current var2) throws FusionException;

   EventPrivacySettingIce getPublishingPrivacyMask(String var1, Current var2) throws FusionException;

   void setPublishingPrivacyMask(String var1, EventPrivacySettingIce var2, Current var3) throws FusionException;

   EventPrivacySettingIce getReceivingPrivacyMask(String var1, Current var2) throws FusionException;

   void setReceivingPrivacyMask(String var1, EventPrivacySettingIce var2, Current var3) throws FusionException;
}
