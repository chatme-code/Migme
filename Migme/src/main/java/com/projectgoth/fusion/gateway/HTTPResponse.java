package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HTTPResponse extends HTTPPacket {
   public HTTPResponse() {
      this.setProperty("Content-Length", "0");
   }

   public HTTPResponse(String contentType, byte[] content) {
      this();
      this.setContent(contentType, content);
   }

   public void read(ByteBuffer buffer) throws IOException {
      String line = ByteBufferHelper.readLine(buffer, "8859_1");
      if (line != null && line.length() != 0) {
         super.read(buffer);
      } else {
         throw new IOException("Empty HTTP response header");
      }
   }

   public String toString() {
      return "HTTP/1.1 200 OK" + "\r\n" + super.toString();
   }
}
