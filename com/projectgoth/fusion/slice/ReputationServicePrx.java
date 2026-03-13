package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ReputationServicePrx extends ObjectPrx {
   void gatherAndProcess() throws FusionException;

   void gatherAndProcess(Map<String, String> var1) throws FusionException;

   void processPreviouslyDumpedData(String var1) throws FusionException;

   void processPreviouslyDumpedData(String var1, Map<String, String> var2) throws FusionException;

   void processPreviouslySortedData(String var1) throws FusionException;

   void processPreviouslySortedData(String var1, Map<String, String> var2) throws FusionException;

   void updateScoreFromPreviouslyProcessedData(String var1) throws FusionException;

   void updateScoreFromPreviouslyProcessedData(String var1, Map<String, String> var2) throws FusionException;

   void updateLastRunDate() throws FusionException;

   void updateLastRunDate(Map<String, String> var1) throws FusionException;

   int getUserLevel(String var1) throws FusionException;

   int getUserLevel(String var1, Map<String, String> var2) throws FusionException;

   ScoreAndLevel[] getUserScoreAndLevels(int[] var1) throws FusionException;

   ScoreAndLevel[] getUserScoreAndLevels(int[] var1, Map<String, String> var2) throws FusionException;
}
