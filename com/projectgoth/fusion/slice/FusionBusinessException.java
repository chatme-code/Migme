package com.projectgoth.fusion.slice;

import Ice.InputStream;
import Ice.MarshalException;
import Ice.OutputStream;
import IceInternal.BasicStream;

public class FusionBusinessException extends FusionException {
   public int errorCode;

   public FusionBusinessException() {
   }

   public FusionBusinessException(String message, int errorCode) {
      super(message);
      this.errorCode = errorCode;
   }

   public String ice_name() {
      return "com::projectgoth::fusion::slice::FusionBusinessException";
   }

   public void __write(BasicStream __os) {
      __os.writeString("::com::projectgoth::fusion::slice::FusionBusinessException");
      __os.startWriteSlice();
      __os.writeInt(this.errorCode);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readString();
      }

      __is.startReadSlice();
      this.errorCode = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionBusinessException was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionBusinessException was not generated with stream support";
      throw ex;
   }
}
