package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _ReputationServiceTie extends _ReputationServiceDisp implements TieBase {
   private _ReputationServiceOperations _ice_delegate;

   public _ReputationServiceTie() {
   }

   public _ReputationServiceTie(_ReputationServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_ReputationServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _ReputationServiceTie) ? false : this._ice_delegate.equals(((_ReputationServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void gatherAndProcess(Current __current) throws FusionException {
      this._ice_delegate.gatherAndProcess(__current);
   }

   public int getUserLevel(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserLevel(username, __current);
   }

   public ScoreAndLevel[] getUserScoreAndLevels(int[] userIDs, Current __current) throws FusionException {
      return this._ice_delegate.getUserScoreAndLevels(userIDs, __current);
   }

   public void processPreviouslyDumpedData(String runDateString, Current __current) throws FusionException {
      this._ice_delegate.processPreviouslyDumpedData(runDateString, __current);
   }

   public void processPreviouslySortedData(String runDateString, Current __current) throws FusionException {
      this._ice_delegate.processPreviouslySortedData(runDateString, __current);
   }

   public void updateLastRunDate(Current __current) throws FusionException {
      this._ice_delegate.updateLastRunDate(__current);
   }

   public void updateScoreFromPreviouslyProcessedData(String runDateString, Current __current) throws FusionException {
      this._ice_delegate.updateScoreFromPreviouslyProcessedData(runDateString, __current);
   }
}
