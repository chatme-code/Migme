package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class SMPPBody {
   private static String DEFAULT_CHARSET = "UTF-8";
   private ByteBuffer body;

   public SMPPBody(ByteBuffer body) {
      this.body = body.duplicate();
   }

   public void flip() {
      this.body.flip();
   }

   public byte getByte() {
      return this.body.get();
   }

   public int getInt() {
      return this.body.getInt();
   }

   public int getIntFromUnsignedByte() {
      return this.body.get() & 255;
   }

   public String getCString() throws UnsupportedEncodingException {
      return ByteBufferHelper.readCString(this.body, DEFAULT_CHARSET);
   }

   public String getString(int length) throws UnsupportedEncodingException {
      return new String(this.getByteArray(length), DEFAULT_CHARSET);
   }

   public byte[] getByteArray(int length) {
      return ByteBufferHelper.readBytes(this.body, length);
   }
}
