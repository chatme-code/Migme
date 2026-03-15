package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _SMSEngineAdminTie extends _SMSEngineAdminDisp implements TieBase {
   private _SMSEngineAdminOperations _ice_delegate;

   public _SMSEngineAdminTie() {
   }

   public _SMSEngineAdminTie(_SMSEngineAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_SMSEngineAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _SMSEngineAdminTie) ? false : this._ice_delegate.equals(((_SMSEngineAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public SMSEngineStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
