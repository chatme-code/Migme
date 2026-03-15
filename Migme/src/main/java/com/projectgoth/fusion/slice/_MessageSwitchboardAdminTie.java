package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _MessageSwitchboardAdminTie extends _MessageSwitchboardAdminDisp implements TieBase {
   private _MessageSwitchboardAdminOperations _ice_delegate;

   public _MessageSwitchboardAdminTie() {
   }

   public _MessageSwitchboardAdminTie(_MessageSwitchboardAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_MessageSwitchboardAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _MessageSwitchboardAdminTie) ? false : this._ice_delegate.equals(((_MessageSwitchboardAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public MessageSwitchboardStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
