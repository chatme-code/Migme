package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class HTTPPacket {
   protected static final String LINE_TERMINATOR = "\r\n";
   protected static final String CHAR_ENCODING = "8859_1";
   protected byte[] content;
   protected Hashtable<String, String> properties = new Hashtable();

   public void setProperty(String name, String value) {
      this.properties.put(name, value);
   }

   public String getProperty(String name) {
      return (String)this.properties.get(name);
   }

   public void setContent(String contentType, byte[] content) {
      this.content = content;
      if (content == null) {
         this.properties.remove("Content-Type");
         this.setProperty("Content-Length", "0");
      } else {
         this.setProperty("Content-Type", contentType);
         this.setProperty("Content-Length", Integer.toString(content.length));
      }

   }

   public byte[] getContent() {
      return this.content;
   }

   public void read(ByteBuffer buffer) throws IOException {
      this.properties = new Hashtable();

      for(String line = ByteBufferHelper.readLine(buffer, "8859_1"); line != null && line.length() > 0; line = ByteBufferHelper.readLine(buffer, "8859_1")) {
         int idx = line.indexOf(":");
         if (idx == -1) {
            throw new IOException("Cannot parse property line - " + line);
         }

         String name = line.substring(0, idx);
         String value = line.substring(idx + 1);
         this.properties.put(name, value.trim());
      }

      this.content = null;
      String lenStr = (String)this.properties.get("Content-Length");
      if (lenStr == null) {
         lenStr = (String)this.properties.get("content-length");
      }

      if (lenStr != null) {
         int contentLength = Integer.parseInt(lenStr);
         if (contentLength > 0) {
            this.content = ByteBufferHelper.readBytes(buffer, contentLength);
         }
      }

   }

   public void read(InputStream in) throws IOException {
      byte[] buffer = new byte[0];

      while(true) {
         try {
            byte[] bytes = new byte[in.available()];
            if (in.read(bytes) > 0) {
               buffer = ByteBufferHelper.concat(buffer, bytes);
               this.read(ByteBuffer.wrap(buffer));
               return;
            }

            Thread.sleep(100L);
         } catch (BufferUnderflowException var4) {
         } catch (InterruptedException var5) {
         }
      }
   }

   public void write(OutputStream out) throws IOException {
      out.write(this.toString().getBytes("8859_1"));
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      Iterator i$ = this.properties.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, String> entry = (Entry)i$.next();
         builder.append((String)entry.getKey()).append(": ").append((String)entry.getValue()).append("\r\n");
      }

      builder.append("\r\n");
      if (this.content != null) {
         try {
            builder.append(new String(this.content, "8859_1"));
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

      return builder.toString();
   }
}
