package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.recommendation.collector.CollectedDataTypeEnum;
import com.projectgoth.fusion.recommendation.collector.DataCollectorException;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.recommendation.collector.DataUploadRequest;
import com.projectgoth.fusion.recommendation.collector.DataUploadTicket;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FusionPktGetUploadDataTicket extends FusionRequest {
   private static final short FIELD_DATA_TYPE_TO_UPLOAD = 1;
   public static final short PACKET_NUMBER = 959;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetUploadDataTicket.class));

   public FusionPktGetUploadDataTicket() {
      super((short)959);
   }

   public FusionPktGetUploadDataTicket(short transactionId) {
      super((short)959, transactionId);
   }

   public FusionPktGetUploadDataTicket(FusionPacket packet) {
      super(packet);
   }

   public Integer getDataTypeToUpload() {
      return this.getIntField((short)1);
   }

   public void setDataTypeToUpload(int type) {
      this.setField((short)1, type);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return processRequest(connection.getUserID(), connection.getSessionID(), this.getTransactionId(), this.getDataTypeToUpload());
   }

   public static FusionPacket[] processRequest(int userID, String sessionID, short transactionID, Integer dataTypeToUpload) {
      if (dataTypeToUpload == null) {
         return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)959, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("'dataTypeToUpload' is null")).toArray();
      } else {
         CollectedDataTypeEnum dataTypeToUploadEnum = CollectedDataTypeEnum.fromCode(dataTypeToUpload);
         if (dataTypeToUploadEnum == null) {
            return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)959, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("Unsupported 'dataTypeToUpload':" + dataTypeToUpload)).toArray();
         } else {
            try {
               DataCollectorUtils.hitUploadTicketRequestRateLimit(userID, dataTypeToUploadEnum);
               DataCollectorUtils.checkUserAccess(userID, dataTypeToUploadEnum);
               DataUploadRequest uploadRequest = new DataUploadRequest();
               uploadRequest.dataType = dataTypeToUploadEnum.getCode();
               uploadRequest.requestTimestamp = System.currentTimeMillis();
               uploadRequest.sessionid = sessionID;
               uploadRequest.userid = userID;
               DataUploadTicket uploadTicket = DataCollectorUtils.createUploadTicket(uploadRequest);
               return (new FusionPktOk(transactionID, uploadTicket.ticketRef)).toArray();
            } catch (DataCollectorException var8) {
               ErrorCause errorCause = var8.getErrorCause();
               if (errorCause instanceof ErrorCause.DataCollectorErrorReasonType) {
                  ErrorCause.DataCollectorErrorReasonType dataCollectorReason = (ErrorCause.DataCollectorErrorReasonType)errorCause;
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)959, log, dataCollectorReason.getSeverityLevel(), DataCollectorUtils.newErrorID(), transactionID, "Unable to get an upload ticket")).setExceptionCause(var8)).toArray();
               } else {
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)959, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Unable to get an upload ticket")).setExceptionCause(var8)).toArray();
               }
            } catch (Exception var9) {
               return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)959, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Failed to get an upload ticket")).setExceptionCause(var9)).toArray();
            }
         }
      }
   }
}
