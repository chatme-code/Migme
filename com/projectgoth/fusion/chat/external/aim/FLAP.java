package com.projectgoth.fusion.chat.external.aim;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FLAP {
   private static final byte ID = 42;
   private byte channel;
   private short sequence;
   private byte[] data;

   public FLAP(byte channel) {
      this.channel = channel;
      this.data = new byte[0];
   }

   public FLAP(SNAC snac) {
      this.channel = 2;
      this.data = snac.toByteArray();
   }

   public FLAP(ByteBuffer buffer) throws IOException {
      if (buffer.get() != 42) {
         throw new IOException("Incorrect FLAP ID");
      } else {
         this.channel = buffer.get();
         this.sequence = buffer.getShort();
         this.data = new byte[buffer.getShort()];
         buffer.get(this.data);
      }
   }

   public byte getChannel() {
      return this.channel;
   }

   public void setChannel(byte channel) {
      this.channel = channel;
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public short getSequence() {
      return this.sequence;
   }

   public void setSequence(short sequence) {
      this.sequence = sequence;
   }

   public FLAP append(int i) {
      this.data = ByteBufferHelper.concat(this.data, ByteBuffer.allocate(4).putInt(i).array());
      return this;
   }

   public FLAP append(TLV tlv) {
      this.data = ByteBufferHelper.concat(this.data, tlv.toByteArray());
      return this;
   }

   public byte[] toByteArray() {
      ByteBuffer buffer = ByteBuffer.allocate(6 + this.data.length);
      buffer.put((byte)42).put(this.channel).putShort(this.sequence).putShort((short)this.data.length).put(this.data);
      return buffer.array();
   }

   public String toString() {
      return "FLAP - Channel " + this.channel + ". Sequence " + this.sequence + ". Length " + this.data.length + "\n" + ByteBufferHelper.toHexString(this.data, " ");
   }
}
