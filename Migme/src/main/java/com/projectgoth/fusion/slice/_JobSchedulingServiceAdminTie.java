package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _JobSchedulingServiceAdminTie extends _JobSchedulingServiceAdminDisp implements TieBase {
   private _JobSchedulingServiceAdminOperations _ice_delegate;

   public _JobSchedulingServiceAdminTie() {
   }

   public _JobSchedulingServiceAdminTie(_JobSchedulingServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_JobSchedulingServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _JobSchedulingServiceAdminTie) ? false : this._ice_delegate.equals(((_JobSchedulingServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public JobSchedulingServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
