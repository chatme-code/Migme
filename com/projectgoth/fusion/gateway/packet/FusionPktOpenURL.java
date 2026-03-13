package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.Gateway;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.stats.FusionPktOpenURLStats;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class FusionPktOpenURL extends FusionRequest {
   private static final String HTTP_TUNNEL_AUDIT = "HttpTunnelAudit";
   private static final String URL_ENCODING = "UTF-8";
   private static final int CONNECT_TIMEOUT = 10000;
   private static final int READ_TIMEOUT = 40000;
   private static final int READ_BUFFER_SIZE = 1024;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktOpenURL.class));
   private static final Logger auditLog = Logger.getLogger("HttpTunnelAudit");
   private static final Pattern WHITE_LIST_SPLIT_PATTERN = Pattern.compile("[,;]");
   private static final LazyLoader<String[]> WHITE_LIST_PREFIXES = new LazyLoader<String[]>("WhiteListPrefixes", 60000L) {
      protected String[] fetchValue() {
         String whiteListedURLs = SystemProperty.get("WhiteListedHTTPTunnelingURLs", "");
         return FusionPktOpenURL.WHITE_LIST_SPLIT_PATTERN.split(whiteListedURLs);
      }
   };

   public FusionPktOpenURL() {
      super((short)922);
   }

   public FusionPktOpenURL(short transactionId) {
      super((short)922, transactionId);
   }

   public FusionPktOpenURL(FusionPacket packet) {
      super(packet);
   }

   public Byte getMethod() {
      return this.getByteField((short)1);
   }

   public void setMethod(byte method) {
      this.setField((short)1, method);
   }

   public String getURL() {
      return this.getStringField((short)2);
   }

   public void setURL(String url) {
      this.setField((short)2, url);
   }

   public String getParameter() {
      return this.getStringField((short)3);
   }

   public void setParameter(String parameter) {
      this.setField((short)3, parameter);
   }

   public String[] getCookies() {
      return this.getStringArrayField((short)11);
   }

   public void setCookies(String[] cookies) {
      this.setField((short)11, cookies);
   }

   public boolean sessionRequired() {
      return true;
   }

   public Gateway.ThreadPoolName getThreadPool() {
      return Gateway.ThreadPoolName.HTTP_TUNNEL;
   }

   public FusionPacket getErrorPacket(String errorMessage) {
      return new FusionPktOpenURLResponse(this.transactionId, 503);
   }

   private static boolean checkWhiteListedURLs(String url) {
      String[] arr$ = (String[])WHITE_LIST_PREFIXES.getValue();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String s = arr$[i$];
         if (url.startsWith(s)) {
            return true;
         }
      }

      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      long startTime = System.currentTimeMillis();
      String urlString = null;

      try {
         urlString = this.getURL();
         if (urlString != null && urlString.length() != 0) {
            urlString = urlString.replaceAll("mogw0[0-9]", "img.mig.me");
            if (!checkWhiteListedURLs(urlString)) {
               throw new GatewayFusionHTTPException("Not whitelisted", urlString, 403);
            } else {
               URL url = new URL(urlString);
               HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
               httpConn.setConnectTimeout(SystemProperty.getInt((String)"OpenURLConnectTimeout", 10000));
               httpConn.setReadTimeout(SystemProperty.getInt((String)"OpenURLReadTimeout", 40000));
               httpConn.setRequestProperty("User-Agent", connection.getUserAgent() + " " + connection.getUsername() + " Tunnel/" + connection.getRemoteAddress());
               httpConn.setRequestProperty("X-Forwarded-For", connection.getRemoteAddress());
               httpConn.setRequestProperty("Accept-Language", connection.getLanguage() != null ? connection.getLanguage() : "");
               httpConn.setRequestProperty("sid", connection.getSessionID());
               httpConn.setRequestProperty("ver", String.format("%.2f", (double)connection.getClientVersion() / 100.0D));
               httpConn.setRequestProperty("ua", connection.getUserAgent());
               httpConn.setRequestProperty("epoch", Long.toString(System.currentTimeMillis()));
               httpConn.setRequestProperty("sw", Integer.toString(connection.getScreenWidth()));
               httpConn.setRequestProperty("sh", Integer.toString(connection.getScreenHeight()));
               if (this.getCookies() != null && this.getCookies().length > 0) {
                  String cookieString = "";
                  String[] arr$ = this.getCookies();
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String cookie = arr$[i$];
                     cookieString = cookieString + cookie + ",";
                  }

                  httpConn.setRequestProperty("Cookie", cookieString);
               }

               Byte method = this.getMethod();
               if (method != null && method != 1) {
                  httpConn.setDoOutput(true);
                  httpConn.setUseCaches(false);
                  String parameter = this.getParameter();
                  if (parameter != null && parameter.length() > 0) {
                     httpConn.getOutputStream().write(parameter.getBytes("UTF-8"));
                  }
               } else {
                  httpConn.connect();
               }

               int responseCode = httpConn.getResponseCode();
               if (responseCode != 200) {
                  throw new GatewayFusionHTTPException(httpConn.getResponseMessage(), urlString, responseCode);
               } else {
                  byte[] buffer = new byte[1024];
                  InputStream in = httpConn.getInputStream();
                  ByteArrayOutputStream out = new ByteArrayOutputStream();

                  for(int bytesRead = in.read(buffer); bytesRead != -1; bytesRead = in.read(buffer)) {
                     out.write(buffer, 0, bytesRead);
                  }

                  FusionPktOpenURLResponse pkt = new FusionPktOpenURLResponse(this.transactionId, responseCode, out.toByteArray());
                  String ttl = httpConn.getHeaderField("TTL");
                  if (ttl != null) {
                     pkt.setTimeToLive(Long.valueOf(ttl));
                  }

                  Map<String, List<String>> headerFields = httpConn.getHeaderFields();
                  if (headerFields != null) {
                     List<String> cookies = (List)headerFields.get("Set-Cookie");
                     if (cookies != null && cookies.size() > 0) {
                        pkt.setCookies((String[])cookies.toArray(new String[cookies.size()]));
                     }
                  }

                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.OPEN_URL_STATS_ENABLED)) {
                     FusionPktOpenURLStats.getInstance().addSuccess(System.currentTimeMillis() - startTime);
                  }

                  return pkt.toArray();
               }
            }
         } else {
            throw new GatewayFusionHTTPException("Not set", urlString, 400);
         }
      } catch (GatewayFusionHTTPException var16) {
         auditLog.warn(var16.toString());
         this.onException(urlString);
         return (new FusionPktOpenURLResponse(this.transactionId, var16.getHttpResponseCode())).toArray();
      } catch (IOException var17) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.OPEN_URL_DETAILED_EXCEPTION_LOGGING)) {
            log.error("IOException when opening " + this.getURL() + " cause=" + var17.getCause() + " message=" + var17.getMessage(), var17);
         } else {
            log.warn("IOException when opening " + this.getURL() + " cause=" + var17.getCause() + " message=" + var17.getMessage(), var17);
         }

         this.onException(urlString);
         return (new FusionPktOpenURLResponse(this.transactionId, 503)).toArray();
      } catch (Exception var18) {
         log.warn("Unexpected exception when opening" + this.getURL(), var18);
         this.onException(urlString);
         return (new FusionPktOpenURLResponse(this.transactionId, 500)).toArray();
      }
   }

   private void onException(String urlString) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.OPEN_URL_STATS_ENABLED)) {
         FusionPktOpenURLStats.getInstance().addFailure(urlString);
      }

   }
}
