package com.projectgoth.fusion.slice;

import Ice.InputStream;
import Ice.MarshalException;
import Ice.OutputStream;
import Ice.UserException;
import IceInternal.BasicStream;

public class FusionException extends UserException {
   public String message;

   public FusionException() {
   }

   public FusionException(String message) {
      this.message = message;
   }

   public String ice_name() {
      return "com::projectgoth::fusion::slice::FusionException";
   }

   public void __write(BasicStream __os) {
      __os.writeString("::com::projectgoth::fusion::slice::FusionException");
      __os.startWriteSlice();
      __os.writeString(this.message);
      __os.endWriteSlice();
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readString();
      }

      __is.startReadSlice();
      this.message = __is.readString();
      __is.endReadSlice();
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionException was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::FusionException was not generated with stream support";
      throw ex;
   }
}
