package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _RecommendationGenerationServiceAdminTie extends _RecommendationGenerationServiceAdminDisp implements TieBase {
   private _RecommendationGenerationServiceAdminOperations _ice_delegate;

   public _RecommendationGenerationServiceAdminTie() {
   }

   public _RecommendationGenerationServiceAdminTie(_RecommendationGenerationServiceAdminOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_RecommendationGenerationServiceAdminOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _RecommendationGenerationServiceAdminTie) ? false : this._ice_delegate.equals(((_RecommendationGenerationServiceAdminTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public RecommendationGenerationServiceStats getStats(Current __current) throws FusionException {
      return this._ice_delegate.getStats(__current);
   }
}
