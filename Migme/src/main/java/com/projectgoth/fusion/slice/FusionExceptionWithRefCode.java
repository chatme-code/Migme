package com.projectgoth.fusion.slice;

import Ice.InputStream;
import Ice.MarshalException;
import Ice.OutputStream;
import IceInternal.BasicStream;

public class FusionExceptionWithRefCode extends FusionExceptionWithErrorCauseCode {
   public String errorRef;

   public FusionExceptionWithRefCode() {
   }

   public FusionExceptionWithRefCode(String message, String errorCauseCode, String errorRef) {
      super(message, errorCauseCode);
      this.errorRef = errorRef;
   }

   public String ice_name() {
      return "com::projectgoth::fusion::slice::FusionExceptionWithRefCode";
   }

   public void __write(BasicStream __os) {
      __os.writeString("::com::projectgoth::fusion::slice::FusionExceptionWithRefCode");
      __os.startWriteSlice();
      __os.writeString(this.errorRef);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readString();
      }

      __is.startReadSlice();
      this.errorRef = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionExceptionWithRefCode was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionExceptionWithRefCode was not generated with stream support";
      throw ex;
   }
}
