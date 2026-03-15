package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;

public class GroupUserPostUserEventIce extends GroupUserEventIce {
   private static ObjectFactory _factory = new GroupUserPostUserEventIce.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::GroupUserEventIce", "::com::projectgoth::fusion::slice::GroupUserPostUserEventIce", "::com::projectgoth::fusion::slice::UserEventIce"};
   public int userPostId;
   public int topicId;
   public String topicText;

   public GroupUserPostUserEventIce() {
   }

   public GroupUserPostUserEventIce(long timestamp, String generatingUsername, String generatingUserDisplayPicture, String text, int groupId, String groupName, int userPostId, int topicId, String topicText) {
      super(timestamp, generatingUsername, generatingUserDisplayPicture, text, groupId, groupName);
      this.userPostId = userPostId;
      this.topicId = topicId;
      this.topicText = topicText;
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
      __os.writeInt(this.userPostId);
      __os.writeInt(this.topicId);
      __os.writeString(this.topicText);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.userPostId = __is.readInt();
      this.topicId = __is.readInt();
      this.topicText = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GroupUserPostUserEventIce was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GroupUserPostUserEventIce was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(GroupUserPostUserEventIce.ice_staticId());

         return new GroupUserPostUserEventIce();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
