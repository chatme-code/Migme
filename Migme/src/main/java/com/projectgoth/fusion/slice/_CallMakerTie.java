package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _CallMakerTie extends _CallMakerDisp implements TieBase {
   private _CallMakerOperations _ice_delegate;

   public _CallMakerTie() {
   }

   public _CallMakerTie(_CallMakerOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_CallMakerOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _CallMakerTie) ? false : this._ice_delegate.equals(((_CallMakerTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries, Current __current) throws FusionException {
      return this._ice_delegate.requestCallback(call, maxDuration, retries, __current);
   }
}
