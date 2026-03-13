package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _RecommendationDataCollectionServiceTie extends _RecommendationDataCollectionServiceDisp implements TieBase {
   private _RecommendationDataCollectionServiceOperations _ice_delegate;

   public _RecommendationDataCollectionServiceTie() {
   }

   public _RecommendationDataCollectionServiceTie(_RecommendationDataCollectionServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_RecommendationDataCollectionServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _RecommendationDataCollectionServiceTie) ? false : this._ice_delegate.equals(((_RecommendationDataCollectionServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void logData(CollectedDataIce dataIce, Current __current) throws FusionExceptionWithRefCode {
      this._ice_delegate.logData(dataIce, __current);
   }
}
