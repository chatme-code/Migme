package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.ObjectImpl;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class UserEventIce extends ObjectImpl {
   private static ObjectFactory _factory = new UserEventIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::UserEventIce"};
   public long timestamp;
   public String generatingUsername;
   public String generatingUserDisplayPicture;
   public String text;

   public UserEventIce() {
   }

   public UserEventIce(long timestamp, String generatingUsername, String generatingUserDisplayPicture, String text) {
      this.timestamp = timestamp;
      this.generatingUsername = generatingUsername;
      this.generatingUserDisplayPicture = generatingUserDisplayPicture;
      this.text = text;
   }

   public static ObjectFactory ice_factory() {
      return _factory;
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

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.writeLong(this.timestamp);
      __os.writeString(this.generatingUsername);
      __os.writeString(this.generatingUserDisplayPicture);
      __os.writeString(this.text);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.timestamp = __is.readLong();
      this.generatingUsername = __is.readString();
      this.generatingUserDisplayPicture = __is.readString();
      this.text = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::UserEventIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::UserEventIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(UserEventIce.ice_staticId());

         return new UserEventIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
