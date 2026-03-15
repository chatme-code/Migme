package com.projectgoth.fusion.smsengine;

public class DispatchStatus {
   public DispatchStatus.StatusEnum status;
   public String transactionID;
   public String failedReason;
   public String source;
   public boolean billed;

   public DispatchStatus(DispatchStatus.StatusEnum status, SMSMessage message) {
      this.status = status;
      this.source = message.getSource();
   }

   public static enum StatusEnum {
      SUCCEEDED,
      FAILED,
      TRY_OTHER_GATEWAYS;
   }
}
