package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class VirtualGiftUserEventIce extends UserEventIce {
   private static ObjectFactory _factory = new VirtualGiftUserEventIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::UserEventIce", "::com::projectgoth::fusion::slice::VirtualGiftUserEventIce"};
   public String recipient;
   public String giftName;
   public int virtualGiftReceivedId;

   public VirtualGiftUserEventIce() {
   }

   public VirtualGiftUserEventIce(long timestamp, String generatingUsername, String generatingUserDisplayPicture, String text, String recipient, String giftName, int virtualGiftReceivedId) {
      super(timestamp, generatingUsername, generatingUserDisplayPicture, text);
      this.recipient = recipient;
      this.giftName = giftName;
      this.virtualGiftReceivedId = virtualGiftReceivedId;
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
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.writeString(this.recipient);
      __os.writeString(this.giftName);
      __os.writeInt(this.virtualGiftReceivedId);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.recipient = __is.readString();
      this.giftName = __is.readString();
      this.virtualGiftReceivedId = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::VirtualGiftUserEventIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::VirtualGiftUserEventIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(VirtualGiftUserEventIce.ice_staticId());

         return new VirtualGiftUserEventIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
