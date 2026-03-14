package com.projectgoth.fusion.slice;

public interface _ReputationServiceOperationsNC {
   void gatherAndProcess() throws FusionException;

   void processPreviouslyDumpedData(String var1) throws FusionException;

   void processPreviouslySortedData(String var1) throws FusionException;

   void updateScoreFromPreviouslyProcessedData(String var1) throws FusionException;

   void updateLastRunDate() throws FusionException;

   int getUserLevel(String var1) throws FusionException;

   ScoreAndLevel[] getUserScoreAndLevels(int[] var1) throws FusionException;
}
