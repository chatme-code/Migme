package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktError extends FusionPacket {
   public FusionPktError() {
      super((short)0);
   }

   public FusionPktError(short transactionId) {
      super((short)0, transactionId);
   }

   public FusionPktError(short transactionId, FusionPktError.Code code, String errorDescription) {
      super((short)0, transactionId);
      this.setErrorCode(code);
      this.setErrorDescription(errorDescription);
   }

   public FusionPktError(short transactionId, FusionPktError.Code code, int infoId) {
      super((short)0, transactionId);
      this.setErrorCode(code);
      String infoText = null;

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         infoText = misEJB.getInfoText(infoId);
      } catch (Exception var9) {
      } finally {
         if (infoText == null) {
            this.setErrorDescription("Request failed");
         } else {
            this.setErrorDescription(infoText);
         }

      }

   }

   public FusionPktError(FusionPacket packet) {
      super(packet);
   }

   public String getErrorDescription() {
      return this.getField((short)2).getStringVal();
   }

   public void setErrorDescription(String errorDescription) {
      this.setField((short)2, errorDescription);
   }

   public Short getErrorNumber() {
      return this.getShortField((short)1);
   }

   public void setErrorCode(FusionPktError.Code errorCode) {
      this.setField((short)1, errorCode.getErrorNumber());
   }

   public static enum Code {
      UNDEFINED((short)1),
      CHAT_SYNC_ENTITY_NOT_FOUND((short)2),
      INCORRECT_CREDENTIAL((short)3),
      INVALID_VERSION((short)100),
      UNSUPPORTED_PROTOCOL((short)101);

      private short errorNumber;

      private Code(short errorNumber) {
         this.errorNumber = errorNumber;
      }

      public short getErrorNumber() {
         return this.errorNumber;
      }
   }
}
