package com.projectgoth.fusion.slice;

import Ice.InputStream;
import Ice.MarshalException;
import Ice.OutputStream;
import IceInternal.BasicStream;

public class ObjectNotFoundException extends FusionException {
   public ObjectNotFoundException() {
   }

   public ObjectNotFoundException(String message) {
      super(message);
   }

   public String ice_name() {
      return "com::projectgoth::fusion::slice::ObjectNotFoundException";
   }

   public void __write(BasicStream __os) {
      __os.writeString("::com::projectgoth::fusion::slice::ObjectNotFoundException");
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readString();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::ObjectNotFoundException was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "exception com::projectgoth::fusion::slice::ObjectNotFoundException was not generated with stream support";
      throw ex;
   }
}
