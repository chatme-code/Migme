package com.projectgoth.fusion.gateway.packet;

class GatewayFusionHTTPException extends Exception {
   private static final String SEPARATOR = ";";
   private String url;
   private int httpResponseCode;

   public GatewayFusionHTTPException(String message, String url, int httpResponseCode) {
      super(message);
      this.url = url;
      this.httpResponseCode = httpResponseCode;
   }

   public int getHttpResponseCode() {
      return this.httpResponseCode;
   }

   public String toString() {
      return this.httpResponseCode + ";" + this.url + ";" + this.getMessage();
   }
}
