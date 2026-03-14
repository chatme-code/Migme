package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _GatewayAdminTie extends _GatewayAdminDisp implements TieBase {
   private _GatewayAdminOperations _ice_delegate;

   public _GatewayAdminTie() {
   }

   public _GatewayAdminTie(_GatewayAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_GatewayAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _GatewayAdminTie) ? false : this._ice_delegate.equals(((_GatewayAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public GatewayStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }

   public void sendAlertToAllConnections(String message, String title, Current __current) {
      this._ice_delegate.sendAlertToAllConnections(message, title, __current);
   }
}
