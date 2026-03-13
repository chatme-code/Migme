package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.data.SMSGatewayData;

public abstract class SMSGateway {
   protected SMSGatewayData gatewayData;

   public SMSGateway(SMSGatewayData gatewayData) {
      this.gatewayData = gatewayData;
   }

   public Integer getID() {
      return this.gatewayData.id;
   }

   public abstract DispatchStatus dispatchMessage(SMSMessage var1);
}
