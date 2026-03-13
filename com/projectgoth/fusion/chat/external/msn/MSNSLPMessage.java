package com.projectgoth.fusion.chat.external.msn;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MSNSLPMessage {
   private static final String VERSION = "MSNSLP/1.0";
   private MSNSLPMessage.Type type;
   private String destination;
   private int statusCode;
   private String statusReason;
   private MSNSLPMessage.Content header;
   private MSNSLPMessage.Content content;

   public MSNSLPMessage(MSNSLPMessage.Type type, String destination) {
      this.type = type;
      this.destination = destination;
      this.header = new MSNSLPMessage.Content();
   }

   public MSNSLPMessage(MSNSLPMessage.Type type, int statusCode, String statusReason) {
      this.type = type;
      this.statusCode = statusCode;
      this.statusReason = statusReason;
      this.header = new MSNSLPMessage.Content();
   }

   public MSNSLPMessage(String rawMessage) {
      String[] lines = rawMessage.split("\r\n", 2);
      if (lines.length == 2) {
         String[] tokens = lines[0].split(" ");
         if (tokens.length > 2) {
            String token = tokens[0];
            if ("MSNSLP/1.0".equals(token)) {
               this.type = MSNSLPMessage.Type.STATUS;
               this.statusCode = Integer.parseInt(tokens[1]);
               this.statusReason = tokens[2];
            } else {
               try {
                  this.type = MSNSLPMessage.Type.valueOf(token);
                  this.destination = tokens[1];
               } catch (Exception var6) {
                  this.type = MSNSLPMessage.Type.UNKNOWN;
               }
            }
         }

         if (this.type != MSNSLPMessage.Type.UNKNOWN) {
            lines = lines[1].split("\r\n\r\n", 2);
            if (lines.length > 0) {
               this.header = new MSNSLPMessage.Content(lines[0]);
               if (lines.length > 1) {
                  this.content = new MSNSLPMessage.Content(lines[1]);
               }
            }
         }
      }

   }

   public MSNSLPMessage.Type getType() {
      return this.type;
   }

   public void setType(MSNSLPMessage.Type type) {
      this.type = type;
   }

   public String getDestination() {
      return this.destination;
   }

   public void setDestination(String destination) {
      this.destination = destination;
   }

   public String getHeader(String key) {
      return this.header == null ? null : this.header.getString(key);
   }

   public void setHeader(String key, String value) {
      this.header.setValue(key, value);
   }

   public MSNSLPMessage.Content getContent() {
      return this.content;
   }

   public void setContent(MSNSLPMessage.Content content) {
      this.content = content;
      this.header.setValue("Content-Length", String.valueOf(content.toString().length()));
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      if (this.type == MSNSLPMessage.Type.STATUS) {
         builder.append("MSNSLP/1.0").append(" ").append(this.statusCode).append(" ").append(this.statusReason).append("\r\n");
      } else {
         builder.append(this.type.toString()).append(" ").append(this.destination).append(" ").append("MSNSLP/1.0").append("\r\n");
      }

      if (this.header != null) {
         builder.append(this.header.toString());
      }

      builder.append("\r\n");
      if (this.content != null) {
         builder.append(this.content.toString());
      }

      return builder.append('\u0000').toString();
   }

   public class Content {
      private Map<String, String> map = new ConcurrentHashMap();

      public Content() {
      }

      public Content(String rawContent) {
         String[] lines = rawContent.split("\r\n");
         String[] arr$ = lines;
         int len$ = lines.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String line = arr$[i$];
            String[] tokens = line.split(": ", 2);
            if (tokens.length == 2) {
               this.map.put(tokens[0], tokens[1]);
            }
         }

      }

      public String getString(String key) {
         return (String)this.map.get(key);
      }

      public Integer getInteger(String key) {
         try {
            String s = (String)this.map.get(key);
            return s == null ? null : Integer.parseInt(s);
         } catch (Exception var3) {
            return null;
         }
      }

      public void setValue(String key, String value) {
         this.map.put(key, value);
      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         Iterator i$ = this.map.keySet().iterator();

         while(i$.hasNext()) {
            String key = (String)i$.next();
            builder.append(key).append(": ").append((String)this.map.get(key)).append("\r\n");
         }

         return builder.toString();
      }
   }

   public static enum Type {
      UNKNOWN,
      INVITE,
      BYE,
      STATUS;
   }
}
