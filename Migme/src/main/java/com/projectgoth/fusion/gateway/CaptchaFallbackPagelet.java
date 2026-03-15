package com.projectgoth.fusion.gateway;

public class CaptchaFallbackPagelet {
   private String url;
   private String message;
   private boolean sendOkPacket;

   private CaptchaFallbackPagelet(String url, String message, boolean sendOkPacket) {
      this.url = url;
      this.message = message;
      this.sendOkPacket = sendOkPacket;
   }

   public CaptchaFallbackPagelet(String url) {
      this(url, (String)null, false);
   }

   public CaptchaFallbackPagelet(String url, String message) {
      this(url, message, true);
   }

   public String getUrl() {
      return this.url;
   }

   public CaptchaFallbackPagelet setUrl(String url) {
      this.url = url;
      return this;
   }

   public String getMessage() {
      return this.message;
   }

   public CaptchaFallbackPagelet setMessage(String message) {
      this.message = message;
      return this;
   }

   public boolean isSendOkPacket() {
      return this.sendOkPacket;
   }

   public CaptchaFallbackPagelet setSendOkPacket(boolean sendOkPacket) {
      this.sendOkPacket = sendOkPacket;
      return this;
   }
}
