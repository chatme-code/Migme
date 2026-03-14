package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _RecommendationGenerationServiceTie extends _RecommendationGenerationServiceDisp implements TieBase {
   private _RecommendationGenerationServiceOperations _ice_delegate;

   public _RecommendationGenerationServiceTie() {
   }

   public _RecommendationGenerationServiceTie(_RecommendationGenerationServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_RecommendationGenerationServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _RecommendationGenerationServiceTie) ? false : this._ice_delegate.equals(((_RecommendationGenerationServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void runTransformation(int transformationID, Current __current) {
      this._ice_delegate.runTransformation(transformationID, __current);
   }
}
