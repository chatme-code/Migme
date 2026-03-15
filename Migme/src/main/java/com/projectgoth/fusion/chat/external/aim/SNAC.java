package com.projectgoth.fusion.chat.external.aim;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.nio.ByteBuffer;

public class SNAC {
   private static int nextRequestID = 0;
   private short service;
   private short subType;
   private short flags;
   private int requestID;
   private byte[] data;

   public SNAC(short service, short subType) {
      this.service = service;
      this.subType = subType;
      this.flags = 0;
      synchronized(this) {
         this.requestID = nextRequestID++;
      }

      this.data = new byte[0];
   }

   public SNAC(ByteBuffer buffer) {
      this.service = buffer.getShort();
      this.subType = buffer.getShort();
      this.flags = buffer.getShort();
      this.requestID = buffer.getInt();
      this.data = new byte[buffer.remaining()];
      buffer.get(this.data);
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public short getFlags() {
      return this.flags;
   }

   public void setFlags(short flags) {
      this.flags = flags;
   }

   public int getRequestID() {
      return this.requestID;
   }

   public void setRequestID(int requestID) {
      this.requestID = requestID;
   }

   public short getService() {
      return this.service;
   }

   public void setService(short service) {
      this.service = service;
   }

   public short getSubType() {
      return this.subType;
   }

   public void setSubType(short subType) {
      this.subType = subType;
   }

   public SNAC append(int i) {
      this.data = ByteBufferHelper.concat(this.data, ByteBuffer.allocate(4).putInt(i).array());
      return this;
   }

   public SNAC append(long l) {
      this.data = ByteBufferHelper.concat(this.data, ByteBuffer.allocate(8).putLong(l).array());
      return this;
   }

   public SNAC append(short s) {
      this.data = ByteBufferHelper.concat(this.data, ByteBuffer.allocate(2).putShort(s).array());
      return this;
   }

   public SNAC append(byte b) {
      this.data = ByteBufferHelper.concat(this.data, new byte[]{b});
      return this;
   }

   public SNAC append(byte[] ba) {
      this.data = ByteBufferHelper.concat(this.data, ba);
      return this;
   }

   public SNAC append(TLV tlv) {
      this.data = ByteBufferHelper.concat(this.data, tlv.toByteArray());
      return this;
   }

   public ByteBuffer getAdjustedData() {
      ByteBuffer buffer = ByteBuffer.wrap(this.data);
      if ((this.flags & '耀') == 32768) {
         ByteBufferHelper.readBytes(buffer, buffer.getShort());
      }

      return buffer;
   }

   public byte[] toByteArray() {
      ByteBuffer buffer = ByteBuffer.allocate(10 + this.data.length);
      buffer.putShort(this.service).putShort(this.subType).putShort(this.flags).putInt(this.requestID).put(this.data);
      return buffer.array();
   }
}
