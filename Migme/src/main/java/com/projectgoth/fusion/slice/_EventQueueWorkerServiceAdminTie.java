package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _EventQueueWorkerServiceAdminTie extends _EventQueueWorkerServiceAdminDisp implements TieBase {
   private _EventQueueWorkerServiceAdminOperations _ice_delegate;

   public _EventQueueWorkerServiceAdminTie() {
   }

   public _EventQueueWorkerServiceAdminTie(_EventQueueWorkerServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_EventQueueWorkerServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _EventQueueWorkerServiceAdminTie) ? false : this._ice_delegate.equals(((_EventQueueWorkerServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public EventQueueWorkerServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
