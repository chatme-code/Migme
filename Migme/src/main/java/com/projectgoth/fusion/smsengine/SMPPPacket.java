package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class SMPPPacket {
   public static byte INTERFACE_VERSION = 52;
   private static String DEFAULT_CHARSET = "UTF-8";
   private static int HEADER_SIZE = 16;
   private static int MAX_BODY_SIZE = 1024;
   private int length;
   private SMPPID id;
   private SMPPStatus status;
   private int sequence;
   private ByteBuffer body;

   public SMPPPacket(SMPPID id) {
      this.length = HEADER_SIZE;
      this.id = id;
      this.status = SMPPStatus.ESME_ROK;
      this.sequence = 0;
      this.body = ByteBuffer.allocate(MAX_BODY_SIZE);
   }

   public SMPPPacket(InputStream in) throws IOException {
      DataInputStream dis = new DataInputStream(in);
      this.length = dis.readInt();
      this.id = SMPPID.fromValue(dis.readInt());
      this.status = SMPPStatus.fromValue(dis.readInt());
      this.sequence = dis.readInt();
      byte[] ba = new byte[this.length - HEADER_SIZE];
      dis.readFully(ba);
      this.body = ByteBuffer.wrap(ba);
   }

   public int getLength() {
      return this.length;
   }

   public SMPPID getId() {
      return this.id;
   }

   public SMPPStatus getStatus() {
      return this.status;
   }

   public int getSequence() {
      return this.sequence;
   }

   public void setSequence(int sequence) {
      this.sequence = sequence;
   }

   public SMPPBody getBody() {
      return new SMPPBody(this.body);
   }

   public SMPPPacket put(byte value) {
      this.body.put(value);
      ++this.length;
      return this;
   }

   public SMPPPacket put(int value) {
      this.body.putInt(value);
      this.length += 4;
      return this;
   }

   public SMPPPacket put(String value) throws UnsupportedEncodingException {
      this.body.put(value.getBytes(DEFAULT_CHARSET));
      this.body.put((byte)0);
      this.length += value.length() + 1;
      return this;
   }

   public SMPPPacket put(byte[] value) {
      this.body.put(value);
      this.length += value.length;
      return this;
   }

   public byte[] toByteArray() {
      ByteBuffer ba = ByteBuffer.allocate(this.length);
      ba.putInt(this.length);
      ba.putInt(this.id.value());
      ba.putInt(this.status.value());
      ba.putInt(this.sequence);
      ba.put(this.body.array(), 0, this.length - HEADER_SIZE);
      return ba.array();
   }

   public String toString() {
      return ByteBufferHelper.toHexString(this.toByteArray(), " ");
   }
}
