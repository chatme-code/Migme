package com.projectgoth.fusion.chat.external.aim;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TLV {
   private short type;
   private byte[] data;

   public TLV(short type) {
      this.type = type;
      this.data = new byte[0];
   }

   public TLV(short type, String s, String charset) throws UnsupportedEncodingException {
      this.type = type;
      this.data = s.getBytes(charset);
   }

   public TLV(short type, int i) {
      this.type = type;
      this.data = ByteBuffer.wrap(new byte[4]).putInt(i).array();
   }

   public TLV(short type, short s) {
      this.type = type;
      this.data = ByteBuffer.wrap(new byte[2]).putShort(s).array();
   }

   public TLV(short type, byte[] data) {
      this.type = type;
      this.data = data;
   }

   public TLV(ByteBuffer buffer) {
      this.type = buffer.getShort();
      this.data = new byte[buffer.getShort()];
      buffer.get(this.data);
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public short getType() {
      return this.type;
   }

   public void setType(short type) {
      this.type = type;
   }

   public String getDataAsString(String charset) throws UnsupportedEncodingException {
      return new String(this.data, charset);
   }

   public int getDataAsInt() {
      return ByteBuffer.wrap(this.data).getInt();
   }

   public short getDataAsShort() {
      return ByteBuffer.wrap(this.data).getShort();
   }

   public TLV append(String s, String charset) throws UnsupportedEncodingException {
      this.data = ByteBufferHelper.concat(this.data, s.getBytes(charset));
      return this;
   }

   public TLV append(byte[] ba) {
      this.data = ByteBufferHelper.concat(this.data, ba);
      return this;
   }

   public TLV append(TLV tlv) {
      this.data = ByteBufferHelper.concat(this.data, tlv.toByteArray());
      return this;
   }

   public static Map<Integer, TLV> parse(ByteBuffer buffer) {
      return parse(buffer, Integer.MAX_VALUE);
   }

   public static Map<Integer, TLV> parse(ByteBuffer buffer, int count) {
      HashMap map = new HashMap();

      while(buffer.hasRemaining() && count-- > 0) {
         TLV tlv = new TLV(buffer);
         map.put(Integer.valueOf(tlv.type), tlv);
      }

      return map;
   }

   public byte[] toByteArray() {
      ByteBuffer buffer = ByteBuffer.allocate(4 + this.data.length);
      buffer.putShort(this.type).putShort((short)this.data.length).put(this.data);
      return buffer.array();
   }
}
