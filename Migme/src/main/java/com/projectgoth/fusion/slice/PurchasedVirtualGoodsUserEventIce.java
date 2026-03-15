package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class PurchasedVirtualGoodsUserEventIce extends UserEventIce {
   private static ObjectFactory _factory = new PurchasedVirtualGoodsUserEventIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce", "::com::projectgoth::fusion::slice::UserEventIce"};
   public byte itemType;
   public int itemId;
   public String itemName;

   public PurchasedVirtualGoodsUserEventIce() {
   }

   public PurchasedVirtualGoodsUserEventIce(long timestamp, String generatingUsername, String generatingUserDisplayPicture, String text, byte itemType, int itemId, String itemName) {
      super(timestamp, generatingUsername, generatingUserDisplayPicture, text);
      this.itemType = itemType;
      this.itemId = itemId;
      this.itemName = itemName;
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
      __os.writeByte(this.itemType);
      __os.writeInt(this.itemId);
      __os.writeString(this.itemName);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.itemType = __is.readByte();
      this.itemId = __is.readInt();
      this.itemName = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(PurchasedVirtualGoodsUserEventIce.ice_staticId());

         return new PurchasedVirtualGoodsUserEventIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
