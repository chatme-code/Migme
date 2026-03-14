package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _ObjectCacheAdminTie extends _ObjectCacheAdminDisp implements TieBase {
   private _ObjectCacheAdminOperations _ice_delegate;

   public _ObjectCacheAdminTie() {
   }

   public _ObjectCacheAdminTie(_ObjectCacheAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_ObjectCacheAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _ObjectCacheAdminTie) ? false : this._ice_delegate.equals(((_ObjectCacheAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public int getLoadWeightage(Current __current) {
      return this._ice_delegate.getLoadWeightage(__current);
   }

   public ObjectCacheStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }

   public String[] getUsernames(Current __current) {
      return this._ice_delegate.getUsernames(__current);
   }

   public int ping(Current __current) {
      return this._ice_delegate.ping(__current);
   }

   public void reloadEmotes(Current __current) {
      this._ice_delegate.reloadEmotes(__current);
   }

   public void setLoadWeightage(int weightage, Current __current) {
      this._ice_delegate.setLoadWeightage(weightage, __current);
   }
}
