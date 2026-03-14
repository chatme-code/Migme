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

public abstract class _RecommendationDataCollectionServiceDisp extends ObjectImpl implements RecommendationDataCollectionService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::RecommendationDataCollectionService"};
   private static final String[] __all = new String[]{"ice_id", "ice_ids", "ice_isA", "ice_ping", "logData"};

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

   public final void logData(CollectedDataIce dataIce) throws FusionExceptionWithRefCode {
      this.logData(dataIce, (Current)null);
   }

   public static DispatchStatus ___logData(RecommendationDataCollectionService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CollectedDataIceHolder dataIce = new CollectedDataIceHolder();
      __is.readObject(dataIce.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.logData(dataIce.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionExceptionWithRefCode var7) {
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
            return ___ice_id(this, in, __current);
         case 1:
            return ___ice_ids(this, in, __current);
         case 2:
            return ___ice_isA(this, in, __current);
         case 3:
            return ___ice_ping(this, in, __current);
         case 4:
            return ___logData(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationDataCollectionService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationDataCollectionService was not generated with stream support";
      throw ex;
   }
}
