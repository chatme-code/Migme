package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;

public abstract class _ReputationServiceDisp extends ObjectImpl implements ReputationService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::ReputationService"};
   private static final String[] __all = new String[]{"gatherAndProcess", "getUserLevel", "getUserScoreAndLevels", "ice_id", "ice_ids", "ice_isA", "ice_ping", "processPreviouslyDumpedData", "processPreviouslySortedData", "updateLastRunDate", "updateScoreFromPreviouslyProcessedData"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final void gatherAndProcess() throws FusionException {
      this.gatherAndProcess((Current)null);
   }

   public final int getUserLevel(String username) throws FusionException {
      return this.getUserLevel(username, (Current)null);
   }

   public final ScoreAndLevel[] getUserScoreAndLevels(int[] userIDs) throws FusionException {
      return this.getUserScoreAndLevels(userIDs, (Current)null);
   }

   public final void processPreviouslyDumpedData(String runDateString) throws FusionException {
      this.processPreviouslyDumpedData(runDateString, (Current)null);
   }

   public final void processPreviouslySortedData(String runDateString) throws FusionException {
      this.processPreviouslySortedData(runDateString, (Current)null);
   }

   public final void updateLastRunDate() throws FusionException {
      this.updateLastRunDate((Current)null);
   }

   public final void updateScoreFromPreviouslyProcessedData(String runDateString) throws FusionException {
      this.updateScoreFromPreviouslyProcessedData(runDateString, (Current)null);
   }

   public static DispatchStatus ___gatherAndProcess(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.gatherAndProcess(__current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___processPreviouslyDumpedData(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String runDateString = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.processPreviouslyDumpedData(runDateString, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___processPreviouslySortedData(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String runDateString = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.processPreviouslySortedData(runDateString, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateScoreFromPreviouslyProcessedData(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String runDateString = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.updateScoreFromPreviouslyProcessedData(runDateString, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateLastRunDate(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.updateLastRunDate(__current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUserLevel(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.getUserLevel(username, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUserScoreAndLevels(ReputationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int[] userIDs = IntArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         ScoreAndLevel[] __ret = __obj.getUserScoreAndLevels(userIDs, __current);
         ScoreAndLevelSequenceHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___gatherAndProcess(this, in, __current);
         case 1:
            return ___getUserLevel(this, in, __current);
         case 2:
            return ___getUserScoreAndLevels(this, in, __current);
         case 3:
            return ___ice_id(this, in, __current);
         case 4:
            return ___ice_ids(this, in, __current);
         case 5:
            return ___ice_isA(this, in, __current);
         case 6:
            return ___ice_ping(this, in, __current);
         case 7:
            return ___processPreviouslyDumpedData(this, in, __current);
         case 8:
            return ___processPreviouslySortedData(this, in, __current);
         case 9:
            return ___updateLastRunDate(this, in, __current);
         case 10:
            return ___updateScoreFromPreviouslyProcessedData(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ReputationService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ReputationService was not generated with stream support";
      throw ex;
   }
}
