package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _BotServiceAdminTie extends _BotServiceAdminDisp implements TieBase {
   private _BotServiceAdminOperations _ice_delegate;

   public _BotServiceAdminTie() {
   }

   public _BotServiceAdminTie(_BotServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_BotServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _BotServiceAdminTie) ? false : this._ice_delegate.equals(((_BotServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public BotServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }

   public int ping(Current __current) {
      return this._ice_delegate.ping(__current);
   }
}
