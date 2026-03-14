package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _BotHunterAdminTie extends _BotHunterAdminDisp implements TieBase {
   private _BotHunterAdminOperations _ice_delegate;

   public _BotHunterAdminTie() {
   }

   public _BotHunterAdminTie(_BotHunterAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_BotHunterAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _BotHunterAdminTie) ? false : this._ice_delegate.equals(((_BotHunterAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public BotHunterStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
