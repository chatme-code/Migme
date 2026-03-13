package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class HTTPRequest extends HTTPPacket {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(HTTPRequest.class));
   private HTTPRequest.RequestMethod requestMethod;
   private String path;
   private Hashtable<String, String> parameters;
   public static final String HEADER_JSON_VERSION = "X-Mig33-JSON-Version";
   private static final Pattern PATTERN_SID = Pattern.compile("(?:.*[;,\\s])?sid\\s*=\\s*(\\w+).*");
   private static final Pattern PATTERN_EID = Pattern.compile("(?:.*[;,\\s])?eid\\s*=\\s*([\\w%]+).*");

   public HTTPRequest() {
   }

   public HTTPRequest(HTTPRequest.RequestMethod requestMethod, String path) {
      this(requestMethod, path, (String)null, (byte[])null);
   }

   public HTTPRequest(HTTPRequest.RequestMethod requestMethod, String path, String contentType, byte[] content) {
      this.requestMethod = requestMethod;
      this.path = path;
      this.setContent(contentType, content);
   }

   public String getSessionId() {
      boolean fromEid = true;
      boolean logging = false;
      String sessionId = this.getSessionIdFromCookie(true, false);
      if (null == sessionId) {
         sessionId = this.getSessionIdFromPath();
      }

      return sessionId;
   }

   public String getSessionIdFromCookie(boolean fromEid, boolean logging) {
      String cookies = this.getProperty("Cookie");
      if (cookies != null) {
         if (SystemProperty.getBool("HTTPRequestGetSIDByRegexEnabled", false)) {
            Matcher m = null;
            boolean matches = false;
            if (fromEid) {
               m = PATTERN_EID.matcher(cookies);
               matches = m.matches();
            }

            if (!matches) {
               m = PATTERN_SID.matcher(cookies);
               matches = m.matches();
               if (fromEid && matches) {
                  log.warn(String.format("UseEID is enabled but found regex 'sid' instead of 'eid' in cookie '%s'", cookies));
               }
            }

            if (matches) {
               if (logging) {
                  log.info("GET-SESSION-ID called with URI [" + this.getRequestMethod().toString() + " " + this.getPath() + "] USERAGENT [" + this.getProperty("User-Agent") + "] REFERRER [" + this.getProperty("Referer") + "]");
               }

               return m.group(1);
            }
         } else {
            int cookieIndex = -1;
            if (fromEid) {
               cookieIndex = cookies.indexOf("eid=");
            }

            if (cookieIndex == -1) {
               cookieIndex = cookies.indexOf("sid=");
               if (fromEid && cookieIndex != -1) {
                  log.warn(String.format("UseEID is enabled but found 'sid' instead of 'eid' in cookie '%s'", cookies));
               }
            }

            if (cookieIndex != -1) {
               if (logging) {
                  log.info("GET-SESSION-ID called with URI [" + this.getRequestMethod().toString() + " " + this.getPath() + "] USERAGENT [" + this.getProperty("User-Agent") + "] REFERRER [" + this.getProperty("Referer") + "]");
               }

               int endOfSessionId = cookies.indexOf(59, cookieIndex);
               if (endOfSessionId == -1) {
                  endOfSessionId = cookies.length();
               }

               return cookies.substring(cookieIndex + 4, endOfSessionId);
            }
         }
      }

      return null;
   }

   public String getSessionIdFromPath() {
      String sessionId = this.path;
      if (sessionId != null && sessionId.length() > 0) {
         int idx = sessionId.lastIndexOf(47);
         if (idx != -1) {
            sessionId = sessionId.substring(idx + 1);
         }

         if (sessionId.length() > 0) {
            if (sessionId.charAt(0) == '?') {
               sessionId = sessionId.substring(1);
            }

            idx = sessionId.indexOf(63);
            if (idx != -1) {
               sessionId = sessionId.substring(0, idx);
            }
         }
      }

      return sessionId;
   }

   public String getParameter(String name) {
      if (this.parameters == null) {
         this.parameters = new Hashtable();
         int idx = this.path.indexOf("?");
         if (idx != -1) {
            String[] ss = this.path.substring(idx + 1).split("&");
            String[] arr$ = ss;
            int len$ = ss.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String s = arr$[i$];
               String[] tokens = s.split("=");
               if (tokens.length == 2) {
                  this.parameters.put(tokens[0], tokens[1]);
               }
            }
         }
      }

      return (String)this.parameters.get(name);
   }

   public Integer getParameterAsInt(String name) {
      String s = this.getParameter(name);
      return s == null ? null : Integer.parseInt(s);
   }

   public Float getParameterAsFloat(String name) {
      String s = this.getParameter(name);
      return s == null ? null : Float.parseFloat(s);
   }

   public Double getParameterAsDouble(String name) {
      String s = this.getParameter(name);
      return s == null ? null : Double.parseDouble(s);
   }

   public String getPath() {
      return this.path;
   }

   public void setPath(String path) {
      this.path = path;
      this.parameters = null;
   }

   public HTTPRequest.RequestMethod getRequestMethod() {
      return this.requestMethod;
   }

   public void setRequestMethod(HTTPRequest.RequestMethod requestMethod) {
      this.requestMethod = requestMethod;
   }

   public void read(ByteBuffer buffer) throws IOException {
      String line = ByteBufferHelper.readLine(buffer, "8859_1");
      if (line != null && line.length() != 0) {
         String[] tokens = line.split(" ");
         this.requestMethod = null;
         HTTPRequest.RequestMethod[] arr$ = HTTPRequest.RequestMethod.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            HTTPRequest.RequestMethod method = arr$[i$];
            if (tokens[0].equalsIgnoreCase(method.toString())) {
               this.requestMethod = method;
            }
         }

         if (this.requestMethod == null) {
            throw new IOException("Invalid request method");
         } else {
            this.path = tokens[1];
            super.read(buffer);
         }
      } else {
         throw new IOException("Empty HTTP request header");
      }
   }

   public String toString() {
      return this.requestMethod.toString() + " " + this.path + " HTTP/1.1" + "\r\n" + super.toString();
   }

   public static enum RequestMethod {
      GET,
      POST;
   }
}
