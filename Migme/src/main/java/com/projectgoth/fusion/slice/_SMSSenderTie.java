package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _SMSSenderTie extends _SMSSenderDisp implements TieBase {
   private _SMSSenderOperations _ice_delegate;

   public _SMSSenderTie() {
   }

   public _SMSSenderTie(_SMSSenderOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_SMSSenderOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _SMSSenderTie) ? false : this._ice_delegate.equals(((_SMSSenderTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void sendSMS(MessageDataIce message, long delay, Current __current) throws FusionException {
      this._ice_delegate.sendSMS(message, delay, __current);
   }

   public void sendSystemSMS(SystemSMSDataIce message, long delay, Current __current) throws FusionException {
      this._ice_delegate.sendSystemSMS(message, delay, __current);
   }
}
