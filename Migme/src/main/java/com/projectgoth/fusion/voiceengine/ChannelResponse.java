package com.projectgoth.fusion.voiceengine;

public class ChannelResponse {
   protected int responseCode = 0;
   protected int returnValue = 0;
   protected String returnData = null;

   public ChannelResponse() {
   }

   public ChannelResponse(int responseCode, int returnValue, String returnData) {
      this.responseCode = responseCode;
      this.returnValue = returnValue;
      this.returnData = returnData;
   }

   public void setResponseCode(int responseCode) {
      this.responseCode = responseCode;
   }

   public int getResponseCode() {
      return this.responseCode;
   }

   public void setReturnValue(int returnValue) {
      this.returnValue = returnValue;
   }

   public int getReturnValue() {
      return this.returnValue;
   }

   public void setReturnData(String returnData) {
      this.returnData = returnData;
   }

   public String getReturnData() {
      return this.returnData;
   }
}
