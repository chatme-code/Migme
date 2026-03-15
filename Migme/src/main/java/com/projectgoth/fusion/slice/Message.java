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
import java.util.Map;

public class Message extends ObjectImpl {
   private static ObjectFactory _factory = new Message.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::Message"};
   public String key;
   public int toUserId;
   public String toUsername;
   public int notificationType;
   public long dateCreated;
   public Map<String, String> parameters;

   public Message() {
   }

   public Message(String key, int toUserId, String toUsername, int notificationType, long dateCreated, Map<String, String> parameters) {
      this.key = key;
      this.toUserId = toUserId;
      this.toUsername = toUsername;
      this.notificationType = notificationType;
      this.dateCreated = dateCreated;
      this.parameters = parameters;
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
      __os.writeString(this.key);
      __os.writeInt(this.toUserId);
      __os.writeString(this.toUsername);
      __os.writeInt(this.notificationType);
      __os.writeLong(this.dateCreated);
      ParamMapHelper.write(__os, this.parameters);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.key = __is.readString();
      this.toUserId = __is.readInt();
      this.toUsername = __is.readString();
      this.notificationType = __is.readInt();
      this.dateCreated = __is.readLong();
      this.parameters = ParamMapHelper.read(__is);
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::Message was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::Message was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(Message.ice_staticId());

         return new Message();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
