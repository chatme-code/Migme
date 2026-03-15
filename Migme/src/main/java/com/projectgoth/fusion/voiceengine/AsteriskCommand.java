package com.projectgoth.fusion.voiceengine;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class AsteriskCommand {
   private static final String PROPERTY_DELIMITER = ": ";
   private static final String LINE_TERMINATOR = "\r\n";
   private AsteriskCommand.Type type;
   private String name;
   private Map<String, String> properties = new HashMap();

   public AsteriskCommand(AsteriskCommand.Type type, String name) {
      this.type = type;
      this.name = name;
   }

   public AsteriskCommand(String rawCommand) throws ParseException {
      String[] lines = rawCommand.split("\r\n");
      if (lines.length == 0) {
         throw new ParseException("Invalid command", 0);
      } else {
         for(int i = 0; i < lines.length; ++i) {
            String[] tokens = lines[i].split(": ", 2);
            if (tokens.length == 0) {
               throw new ParseException("Incorrect command format. Line " + i + 1 + ". " + lines[i], 0);
            }

            if (i == 0) {
               try {
                  this.type = AsteriskCommand.Type.valueOf(tokens[0].toUpperCase());
               } catch (Exception var6) {
                  throw new ParseException("Unknown command type " + tokens[0], 0);
               }

               this.name = tokens.length == 1 ? null : tokens[1];
            } else {
               this.properties.put(tokens[0], tokens.length == 1 ? null : tokens[1]);
            }
         }

      }
   }

   public AsteriskCommand.Type getType() {
      return this.type;
   }

   public void setType(AsteriskCommand.Type type) {
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getProperty(String key) {
      if (key != null && key.length() >= 1) {
         String value = (String)this.properties.get(key);
         if (value != null) {
            return value;
         } else {
            Iterator i$ = this.properties.keySet().iterator();

            while(i$.hasNext()) {
               String keyc = (String)i$.next();
               if (key.equalsIgnoreCase(keyc)) {
                  value = (String)this.properties.get(keyc);
                  break;
               }
            }

            return value;
         }
      } else {
         return null;
      }
   }

   public void setProperty(String key, String value) {
      if (key != null && key.length() >= 1) {
         this.properties.put(key, value);
      }
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.type).append(": ").append(this.name).append("\r\n");

      for(Iterator i$ = this.properties.entrySet().iterator(); i$.hasNext(); builder.append("\r\n")) {
         Entry<String, String> property = (Entry)i$.next();
         builder.append((String)property.getKey()).append(": ");
         if (property.getValue() != null) {
            builder.append((String)property.getValue());
         }
      }

      return builder.toString();
   }

   public static enum Type {
      ACTION,
      RESPONSE,
      EVENT;
   }
}
