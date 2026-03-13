package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.SMSGatewayData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class HTTPGateway extends SMSGateway {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(HTTPGateway.class));
   private static final String URL_ENCODING = "UTF-8";
   private static final int CONNECT_TIMEOUT = 15000;
   private static final int READ_TIMEOUT = 15000;
   private String separator;
   private boolean encodeParam;

   public HTTPGateway(SMSGatewayData gatewayData) {
      super(gatewayData);
      if (gatewayData.method == SMSGatewayData.MethodEnum.GET) {
         this.separator = "&";
         this.encodeParam = true;
      } else {
         this.separator = "\r\n";
         this.encodeParam = false;
      }

   }

   private void addParam(StringBuilder builder, String param) {
      if (param != null) {
         if (builder.length() > 0) {
            builder.append(this.separator);
         }

         builder.append(param);
      }

   }

   private void addParam(StringBuilder builder, String param, String value) throws UnsupportedEncodingException {
      if (param != null) {
         if (builder.length() > 0) {
            builder.append(this.separator);
         }

         if (this.encodeParam) {
            builder.append(URLEncoder.encode(param, "UTF-8"));
         } else {
            builder.append(param);
         }

         builder.append("=");
         if (this.encodeParam) {
            builder.append(URLEncoder.encode(value, "UTF-8"));
         } else {
            builder.append(value.replaceAll("\n", "<LF>").replaceAll("\r", "<CR>"));
         }
      }

   }

   public DispatchStatus dispatchMessage(SMSMessage message) {
      StringBuilder param = null;
      Pattern successPattern = null;
      Pattern errorPattern = null;
      DispatchStatus dispatchStatus = new DispatchStatus(DispatchStatus.StatusEnum.TRY_OTHER_GATEWAYS, message);

      String messageText;
      try {
         successPattern = Pattern.compile(this.gatewayData.successPattern);
         errorPattern = Pattern.compile(this.gatewayData.errorPattern);
         param = new StringBuilder();
         this.addParam(param, this.gatewayData.usernameParam);
         this.addParam(param, this.gatewayData.passwordParam);
         this.addParam(param, this.gatewayData.sourceParam, message.getSource());
         this.addParam(param, this.gatewayData.destinationParam, message.getDestination());
         this.addParam(param, this.gatewayData.extraParam);
         messageText = message.getMessageText();
         if (!messageText.matches("^[\\x00-\\xFF]*$")) {
            if (this.gatewayData.unicodeCharset != null) {
               messageText = this.byteArrayToHexString(messageText.getBytes(this.gatewayData.unicodeCharset));
               messageText = messageText.replaceAll("feff", "");
            }

            if (this.gatewayData.unicodeMessageParam == null) {
               this.addParam(param, this.gatewayData.messageParam, messageText);
            } else {
               this.addParam(param, this.gatewayData.unicodeMessageParam, messageText);
            }

            this.addParam(param, this.gatewayData.unicodeParam);
         } else {
            this.addParam(param, this.gatewayData.messageParam, messageText);
         }
      } catch (Exception var25) {
         log.warn("Gateway " + this.gatewayData.id + ". Unable to dispatch " + message.getShortDescription() + " - " + var25.toString(), var25);
         return dispatchStatus;
      }

      BufferedReader reader = null;

      try {
         URL url;
         HttpURLConnection httpConn;
         if (this.gatewayData.method == SMSGatewayData.MethodEnum.GET) {
            url = new URL(this.gatewayData.url + "?" + param.toString());
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setConnectTimeout(15000);
            httpConn.setReadTimeout(15000);
            httpConn.connect();
         } else {
            url = new URL(this.gatewayData.url);
            httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setConnectTimeout(15000);
            httpConn.setReadTimeout(15000);
            httpConn.setDoOutput(true);
            if (this.gatewayData.authorization != null) {
               httpConn.setRequestProperty("Authorization", this.gatewayData.authorization);
            }

            httpConn.setUseCaches(false);
            httpConn.getOutputStream().write(param.toString().getBytes("UTF-8"));
         }

         log.debug("Gateway " + this.gatewayData.id + ". Dispatching " + message.getShortDescription() + ". " + url.toString());
         if (httpConn.getResponseCode() != 200) {
            dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
            dispatchStatus.failedReason = "HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage();
         } else {
            reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
               response.append(line);
            }

            Matcher matcher = successPattern.matcher(response.toString());
            if (matcher.find()) {
               dispatchStatus.status = DispatchStatus.StatusEnum.SUCCEEDED;
               dispatchStatus.transactionID = matcher.group(1);
               dispatchStatus.billed = this.gatewayData.deliveryReporting == null || !this.gatewayData.deliveryReporting;
            } else {
               dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
               matcher = errorPattern.matcher(response);
               if (matcher.find()) {
                  StringBuilder error = new StringBuilder();
                  int i = 0;

                  for(int c = matcher.groupCount(); i < c; ++i) {
                     error.append(matcher.group(i + 1)).append(". ");
                  }

                  dispatchStatus.failedReason = error.toString();
               }
            }
         }

         if (dispatchStatus.status == DispatchStatus.StatusEnum.SUCCEEDED) {
            log.info("Gateway " + this.gatewayData.id + ". Successfully dispatched " + message.getShortDescription() + ". Transaction ID = " + dispatchStatus.transactionID);
         } else if (dispatchStatus.status == DispatchStatus.StatusEnum.FAILED) {
            log.warn("Gateway " + this.gatewayData.id + ". Failed to dispatch " + message.getShortDescription() + " - " + dispatchStatus.failedReason);
         }
      } catch (Exception var26) {
         log.warn("Gateway " + this.gatewayData.id + ". Exception caught while dispatching " + message.getShortDescription() + " - " + var26.toString(), var26);
      } finally {
         try {
            if (reader != null) {
               reader.close();
            }
         } catch (IOException var24) {
            messageText = null;
         }

      }

      return dispatchStatus;
   }

   private String byteArrayToHexString(byte[] ba) {
      StringBuilder builder = new StringBuilder();
      byte[] arr$ = ba;
      int len$ = ba.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte b = arr$[i$];
         builder.append(Integer.toHexString(b >> 4 & 15));
         builder.append(Integer.toHexString(b & 15));
      }

      return builder.toString();
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("SMS Gateway: ID=");
      builder.append(this.gatewayData.id);
      builder.append(" Name=");
      builder.append(this.gatewayData.name);
      builder.append(" URL=");
      builder.append(this.gatewayData.url);
      return builder.toString();
   }
}
