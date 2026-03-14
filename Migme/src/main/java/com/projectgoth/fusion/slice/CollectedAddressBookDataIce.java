package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class CollectedAddressBookDataIce extends CollectedDataIce {
   private static ObjectFactory _factory = new CollectedAddressBookDataIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::CollectedAddressBookDataIce", "::com::projectgoth::fusion::slice::CollectedDataIce"};
   public int submitterUserId;
   public int contactType;
   public String[] contactValues;

   public CollectedAddressBookDataIce() {
   }

   public CollectedAddressBookDataIce(int dataType, long createTimestamp, int submitterUserId, int contactType, String[] contactValues) {
      super(dataType, createTimestamp);
      this.submitterUserId = submitterUserId;
      this.contactType = contactType;
      this.contactValues = contactValues;
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
      __os.writeInt(this.submitterUserId);
      __os.writeInt(this.contactType);
      StringArrayHelper.write(__os, this.contactValues);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.submitterUserId = __is.readInt();
      this.contactType = __is.readInt();
      this.contactValues = StringArrayHelper.read(__is);
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::CollectedAddressBookDataIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::CollectedAddressBookDataIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(CollectedAddressBookDataIce.ice_staticId());

         return new CollectedAddressBookDataIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
