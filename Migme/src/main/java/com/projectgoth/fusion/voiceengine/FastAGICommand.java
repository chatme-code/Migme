package com.projectgoth.fusion.voiceengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FastAGICommand {
   private String request;
   private String callerID;
   private String channel;
   private Map<String, String> map = new HashMap();
   private List<String> lines = new ArrayList();

   public FastAGICommand() {
   }

   public FastAGICommand(InputStream in) throws IOException {
      this.read(in);
   }

   public String getRequest() {
      return this.request;
   }

   public String getCallerID() {
      return this.callerID;
   }

   public void setCallerID(String callerID) {
      this.callerID = callerID;
   }

   public String getChannel() {
      return this.channel;
   }

   public String getParameter(String name) {
      return (String)this.map.get(name);
   }

   public String setParameter(String name, String value) {
      return (String)this.map.put(name, value);
   }

   public Integer getParameterAsInt(String name) {
      try {
         String param = (String)this.map.get(name);
         return param == null ? null : Integer.valueOf(param);
      } catch (Exception var3) {
         return null;
      }
   }

   public String getRawCommand() {
      StringBuilder builder = new StringBuilder();
      Iterator i$ = this.lines.iterator();

      while(i$.hasNext()) {
         String line = (String)i$.next();
         builder.append(line).append('\n');
      }

      return builder.toString();
   }

   public void read(InputStream in) throws IOException {
      this.request = null;
      this.callerID = null;
      this.map.clear();
      this.lines.clear();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      for(String line = reader.readLine(); line != null && line.length() > 0 && line != "\r" && line != "\r\n"; line = reader.readLine()) {
         String[] tokens = line.split(":", 2);
         if (tokens.length == 2) {
            if ("agi_callerid".equalsIgnoreCase(tokens[0])) {
               this.callerID = tokens[1].trim();
            } else if ("agi_uniqueid".equalsIgnoreCase(tokens[0])) {
               this.channel = tokens[1].trim();
            } else if ("agi_request".equalsIgnoreCase(tokens[0])) {
               String[] arr$ = tokens[1].split("[/&]");
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  String param = arr$[i$];
                  tokens = param.split("=", 2);
                  if (tokens.length == 2) {
                     if ("request".equalsIgnoreCase(tokens[0])) {
                        this.request = tokens[1];
                     } else {
                        this.map.put(tokens[0], tokens[1]);
                     }
                  }
               }
            }
         }

         this.lines.add(line);
      }

   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Reqeust: ").append(this.request).append('\n');
      builder.append("Caller ID: ").append(this.callerID).append('\n');
      Iterator i$ = this.map.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         builder.append("Param: ").append(key).append(" = ").append((String)this.map.get(key)).append('\n');
      }

      return builder.toString();
   }
}
