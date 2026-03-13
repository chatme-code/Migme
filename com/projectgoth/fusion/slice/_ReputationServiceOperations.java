package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _ReputationServiceOperations {
   void gatherAndProcess(Current var1) throws FusionException;

   void processPreviouslyDumpedData(String var1, Current var2) throws FusionException;

   void processPreviouslySortedData(String var1, Current var2) throws FusionException;

   void updateScoreFromPreviouslyProcessedData(String var1, Current var2) throws FusionException;

   void updateLastRunDate(Current var1) throws FusionException;

   int getUserLevel(String var1, Current var2) throws FusionException;

   ScoreAndLevel[] getUserScoreAndLevels(int[] var1, Current var2) throws FusionException;
}
