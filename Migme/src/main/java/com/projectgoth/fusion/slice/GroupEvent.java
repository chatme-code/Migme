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

public class GroupEvent extends ObjectImpl {
   private static ObjectFactory _factory = new GroupEvent.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::GroupEvent"};
   public int id;
   public int groupId;
   public String description;
   public long startTime;
   public int duration;
   public String chatRoomName;
   public int chatRoomCategoryID;
   public long dateCreated;
   public int status;

   public GroupEvent() {
   }

   public GroupEvent(int id, int groupId, String description, long startTime, int duration, String chatRoomName, int chatRoomCategoryID, long dateCreated, int status) {
      this.id = id;
      this.groupId = groupId;
      this.description = description;
      this.startTime = startTime;
      this.duration = duration;
      this.chatRoomName = chatRoomName;
      this.chatRoomCategoryID = chatRoomCategoryID;
      this.dateCreated = dateCreated;
      this.status = status;
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
      __os.writeInt(this.id);
      __os.writeInt(this.groupId);
      __os.writeString(this.description);
      __os.writeLong(this.startTime);
      __os.writeInt(this.duration);
      __os.writeString(this.chatRoomName);
      __os.writeInt(this.chatRoomCategoryID);
      __os.writeLong(this.dateCreated);
      __os.writeInt(this.status);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.id = __is.readInt();
      this.groupId = __is.readInt();
      this.description = __is.readString();
      this.startTime = __is.readLong();
      this.duration = __is.readInt();
      this.chatRoomName = __is.readString();
      this.chatRoomCategoryID = __is.readInt();
      this.dateCreated = __is.readLong();
      this.status = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GroupEvent was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GroupEvent was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(GroupEvent.ice_staticId());

         return new GroupEvent();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
