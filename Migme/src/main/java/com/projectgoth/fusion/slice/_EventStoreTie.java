package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _EventStoreTie extends _EventStoreDisp implements TieBase {
   private _EventStoreOperations _ice_delegate;

   public _EventStoreTie() {
   }

   public _EventStoreTie(_EventStoreOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_EventStoreOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _EventStoreTie) ? false : this._ice_delegate.equals(((_EventStoreTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void deleteUserEvents(String username, Current __current) throws FusionException {
      this._ice_delegate.deleteUserEvents(username, __current);
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Current __current) throws FusionException {
      return this._ice_delegate.getPublishingPrivacyMask(username, __current);
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Current __current) throws FusionException {
      return this._ice_delegate.getReceivingPrivacyMask(username, __current);
   }

   public UserEventIce[] getUserEventsForUser(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserEventsForUser(username, __current);
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserEventsGeneratedByUser(username, __current);
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      this._ice_delegate.setPublishingPrivacyMask(username, mask, __current);
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      this._ice_delegate.setReceivingPrivacyMask(username, mask, __current);
   }

   public void storeGeneratorEvent(String username, UserEventIce event, Current __current) throws FusionException {
      this._ice_delegate.storeGeneratorEvent(username, event, __current);
   }

   public void storeUserEvent(String username, UserEventIce event, Current __current) throws FusionException {
      this._ice_delegate.storeUserEvent(username, event, __current);
   }
}
