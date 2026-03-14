package com.projectgoth.fusion.slice;

import Ice.InputStream;
import Ice.MarshalException;
import Ice.OutputStream;
import IceInternal.BasicStream;

public class FusionExceptionWithErrorCauseCode extends FusionException {
   public String errorCauseCode;

   public FusionExceptionWithErrorCauseCode() {
   }

   public FusionExceptionWithErrorCauseCode(String message, String errorCauseCode) {
      super(message);
      this.errorCauseCode = errorCauseCode;
   }

   public String ice_name() {
      return "com::projectgoth::fusion::slice::FusionExceptionWithErrorCauseCode";
   }

   public void __write(BasicStream __os) {
      __os.writeString("::com::projectgoth::fusion::slice::FusionExceptionWithErrorCauseCode");
      __os.startWriteSlice();
      __os.writeString(this.errorCauseCode);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readString();
      }

      __is.startReadSlice();
      this.errorCauseCode = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionExceptionWithErrorCauseCode was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionExceptionWithErrorCauseCode was not generated with stream support";
      throw ex;
   }
}
