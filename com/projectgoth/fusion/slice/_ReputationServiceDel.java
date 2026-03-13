package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ReputationServiceDel extends _ObjectDel {
   void gatherAndProcess(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void processPreviouslyDumpedData(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void processPreviouslySortedData(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void updateScoreFromPreviouslyProcessedData(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void updateLastRunDate(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   int getUserLevel(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   ScoreAndLevel[] getUserScoreAndLevels(int[] var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;
}
