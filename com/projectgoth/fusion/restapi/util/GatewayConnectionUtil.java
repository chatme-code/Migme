package com.projectgoth.fusion.restapi.util;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

public class GatewayConnectionUtil {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatewayConnectionUtil.class));
   private String gatewayHttpHost;
   private int gatewayHttpPort;
   private HttpHost hcHost;
   private static GatewayConnectionUtil theInstance;
   private static Object lock = new Object();

   private GatewayConnectionUtil() {
      Properties props = null;
      String propFile = System.getProperty("fusion.datasvc.config", "/usr/fusion/etc/datasvc.properties");
      File f = new File(propFile);
      if (f.exists()) {
         props = new Properties();

         try {
            log.info(String.format("Loading config from file %s", propFile));
            props.load(new FileInputStream(f));
         } catch (FileNotFoundException var5) {
            log.error(String.format("Unable to find config file from %s", propFile));
            props = null;
         } catch (IOException var6) {
            log.error(String.format("Unable to load config from %s", propFile), var6);
            props = null;
         }
      } else {
         log.info(String.format("Config file %s not found", propFile));
      }

      if (props == null) {
         log.info(String.format("Loading config from System.properties"));
         props = System.getProperties();
      }

      this.gatewayHttpHost = props.getProperty("migcore.gatewayhttp.host", "localhost");
      this.gatewayHttpPort = Integer.parseInt(props.getProperty("migcore.gatewayhttp.port", "80"));
      this.hcHost = new HttpHost(this.gatewayHttpHost, this.gatewayHttpPort, "http");
      log.info(String.format("api url is %s", this.hcHost.toString()));
   }

   public static GatewayConnectionUtil getInstance() {
      if (theInstance == null) {
         synchronized(lock) {
            if (theInstance == null) {
               theInstance = new GatewayConnectionUtil();
            }
         }
      }

      return theInstance;
   }

   public FusionPacket[] sendRequest(FusionRequest request) throws GatewayConnectionUtil.GatewayConnectionException {
      return this.sendRequest(request, (String)null);
   }

   public FusionPacket[] sendRequest(FusionRequest request, String sid) throws GatewayConnectionUtil.GatewayConnectionException {
      try {
         return this.sendGatewayRequest(request.toJSON(2), sid, "");
      } catch (JSONException var4) {
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, var4.getMessage());
      } catch (GatewayConnectionUtil.GatewayConnectionException var5) {
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, var5.getMessage());
      }
   }

   public FusionPacket[] sendRequest(FusionRequest request, String sid, String remoteIp) throws GatewayConnectionUtil.GatewayConnectionException {
      try {
         return this.sendGatewayRequest(request.toJSON(2), sid, remoteIp);
      } catch (JSONException var5) {
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, var5.getMessage());
      } catch (GatewayConnectionUtil.GatewayConnectionException var6) {
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, var6.getMessage());
      }
   }

   private FusionPacket[] sendGatewayRequest(String jsonRequest, String sid, String remoteIp) throws GatewayConnectionUtil.GatewayConnectionException {
      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost = new HttpPost("/xml");
      httppost.addHeader("X-Mig33-JSON-Version", Integer.toString(2));
      httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
      httppost.addHeader("Connection", "close");
      httppost.addHeader("Cookie", String.format("sid=%s", sid));
      if (!StringUtil.isBlank(remoteIp)) {
         httppost.addHeader("X-Forwarded-For", remoteIp);
      }

      jsonRequest = jsonRequest.trim();

      try {
         httppost.setEntity(new StringEntity(jsonRequest));
      } catch (UnsupportedEncodingException var16) {
         String errMsg = String.format("Error in create request to make gateway http call to %s", this.hcHost.toString());
         log.error(errMsg, var16);
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, errMsg);
      }

      HttpResponse response = null;

      String res;
      try {
         log.info(String.format("Making gateway http call to %s", this.hcHost.toString()));
         response = httpclient.execute(this.hcHost, httppost);
         log.info(String.format("Received response from gateway http call %s", this.hcHost.toString()));
      } catch (ClientProtocolException var14) {
         res = String.format("Error in making gateway http call to %s", this.hcHost.toString());
         log.error(res, var14);
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, res);
      } catch (IOException var15) {
         res = String.format("IOException in making gateway http call to %s", this.hcHost.toString());
         log.error(res, var15);
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, res);
      }

      HttpEntity entity = response.getEntity();
      if (entity != null) {
         String errMsg;
         try {
            res = EntityUtils.toString(entity);
            if (res.length() == 0) {
               log.error("No response returned from request " + jsonRequest);
               throw new GatewayConnectionUtil.GatewayConnectionException(-1, "Unable to parse gateway http call result.");
            } else {
               try {
                  return FusionPacket.parseJSONArray(res, 2);
               } catch (Exception var11) {
                  String errMsg = String.format("Unable to parse gateway http call result into JSON array, api %s, %s", this.hcHost.toString(), var11.getMessage());
                  log.error(errMsg, var11);
                  throw new GatewayConnectionUtil.GatewayConnectionException(-1, errMsg);
               }
            }
         } catch (ParseException var12) {
            errMsg = String.format("Unable to parse gateway http call result, api %s", this.hcHost.toString());
            log.error(errMsg, var12);
            throw new GatewayConnectionUtil.GatewayConnectionException(-1, errMsg);
         } catch (IOException var13) {
            errMsg = String.format("Unable to parse gateway http call result, api %s", this.hcHost.toString());
            log.error(errMsg, var13);
            throw new GatewayConnectionUtil.GatewayConnectionException(-1, errMsg);
         }
      } else {
         log.error(String.format("null entity in making gateway http call to %s", this.hcHost.toString()));
         throw new GatewayConnectionUtil.GatewayConnectionException(-1, "null entity");
      }
   }

   public static class GatewayConnectionException extends Exception {
      public int errno;

      public GatewayConnectionException(int errno, String errMsg) {
         super(errMsg);
         this.errno = errno;
      }

      public boolean isLocalException() {
         return this.errno <= 100;
      }
   }
}
