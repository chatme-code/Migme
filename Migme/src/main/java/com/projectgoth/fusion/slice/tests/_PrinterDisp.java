package com.projectgoth.fusion.slice.tests;

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

public abstract class _PrinterDisp extends ObjectImpl implements Printer {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::tests::Printer"};
   private static final String[] __all = new String[]{"circular", "ice_id", "ice_ids", "ice_isA", "ice_ping", "printString"};

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

   public final void circular(String s, int level) {
      this.circular(s, level, (Current)null);
   }

   public final void printString(String s) {
      this.printString(s, (Current)null);
   }

   public static DispatchStatus ___printString(Printer __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String s = __is.readString();
      __is.endReadEncaps();
      __obj.printString(s, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___circular(Printer __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String s = __is.readString();
      int level = __is.readInt();
      __is.endReadEncaps();
      __obj.circular(s, level, __current);
      return DispatchStatus.DispatchOK;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___circular(this, in, __current);
         case 1:
            return ___ice_id(this, in, __current);
         case 2:
            return ___ice_ids(this, in, __current);
         case 3:
            return ___ice_isA(this, in, __current);
         case 4:
            return ___ice_ping(this, in, __current);
         case 5:
            return ___printString(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::tests::Printer was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::tests::Printer was not generated with stream support";
      throw ex;
   }
}
