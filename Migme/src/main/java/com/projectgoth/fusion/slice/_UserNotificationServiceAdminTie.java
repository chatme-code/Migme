package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _UserNotificationServiceAdminTie extends _UserNotificationServiceAdminDisp implements TieBase {
   private _UserNotificationServiceAdminOperations _ice_delegate;

   public _UserNotificationServiceAdminTie() {
   }

   public _UserNotificationServiceAdminTie(_UserNotificationServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_UserNotificationServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _UserNotificationServiceAdminTie) ? false : this._ice_delegate.equals(((_UserNotificationServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public UserNotificationServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
