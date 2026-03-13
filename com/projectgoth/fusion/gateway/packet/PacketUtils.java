package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.fdl.enums.PacketType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class PacketUtils {
   public static FusionPktError logAndPrepareFusionPktError(PacketUtils.ErrorLogData errorLogData) {
      String pktErrorDesc = String.format("%s(Ref:%s)", errorLogData.errorMessage, errorLogData.errorRefId);
      StringBuilder logMessageBuilder = new StringBuilder();
      logMessageBuilder.append("Error processing packet[").append(errorLogData.packetRequestType).append("].");
      logMessageBuilder.append("Tx[").append(errorLogData.transactionId).append("].");
      logMessageBuilder.append("Msg:[").append(pktErrorDesc).append("].");
      if (!StringUtil.isBlank(errorLogData.detailedErrorMessage)) {
         logMessageBuilder.append("Details:[").append(errorLogData.detailedErrorMessage).append("].");
      }

      if (errorLogData.exceptionCause != null) {
         logMessageBuilder.append("Exception:[").append(errorLogData.exceptionCause).append("].");
      }

      Log4JUtils.log(errorLogData.logger, errorLogData.logLevel, logMessageBuilder, errorLogData.exceptionCause);
      return new FusionPktError(errorLogData.transactionId, errorLogData.errorCode, pktErrorDesc);
   }

   public static class ErrorLogData {
      private PacketType packetRequestType;
      private Logger logger;
      private Level logLevel;
      private String errorRefId;
      private String errorMessage;
      private short transactionId;
      private FusionPktError.Code errorCode;
      private String detailedErrorMessage;
      private Throwable exceptionCause;

      public PacketUtils.ErrorLogData setDetailedErrorMessage(String detailedErrorMessage) {
         this.detailedErrorMessage = detailedErrorMessage;
         return this;
      }

      public PacketUtils.ErrorLogData setExceptionCause(Throwable exceptionCause) {
         this.exceptionCause = exceptionCause;
         return this;
      }

      public PacketUtils.ErrorLogData setErrorCode(FusionPktError.Code errorCode) {
         this.errorCode = errorCode;
         return this;
      }

      public ErrorLogData(short packetRequestType, Logger logger, Level logLevel, String errorRefId, short transactionId, String errorMessage) {
         this(PacketType.fromValue(packetRequestType), logger, logLevel, errorRefId, transactionId, errorMessage);
      }

      public ErrorLogData(PacketType packetRequestType, Logger logger, Level logLevel, String errorRefId, short transactionId, String errorMessage) {
         if (logger == null) {
            throw new IllegalArgumentException("'logger' is null");
         } else {
            this.packetRequestType = packetRequestType;
            this.logger = logger;
            this.logLevel = logLevel;
            this.errorRefId = errorRefId;
            this.transactionId = transactionId;
            this.errorMessage = errorMessage;
            this.errorCode = FusionPktError.Code.UNDEFINED;
         }
      }
   }
}
