package com.projectgoth.fusion.gateway.packet;

import Ice.Communicator;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.Gateway;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.recommendation.collector.CollectedDataTypeEnum;
import com.projectgoth.fusion.recommendation.collector.DataCollectorException;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.recommendation.collector.DataUploadTicket;
import com.projectgoth.fusion.recommendation.collector.addressbook.AddressBookContactTypeEnum;
import com.projectgoth.fusion.slice.CollectedAddressBookDataIce;
import com.projectgoth.fusion.slice.FusionExceptionWithRefCode;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServicePrx;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FusionPktUploadAddressBookContactsOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktUploadAddressBookContactsOld.class));
   private static final CollectedDataTypeEnum EXPECTED_DATA_TYPE;
   private static final long EXPECTED_COLLECTED_DATA_TYPE_CODE;
   private static final short FIELD_CONTACT_TYPE = 1;
   private static final short FIELD_CONTACT_VALUES = 2;
   private static final short FIELD_TICKET_ID = 3;
   public static final short PACKET_NUMBER = 960;

   public Byte getContactType() {
      return this.getByteField((short)1);
   }

   public void setContactType(byte contactType) {
      this.setField((short)1, contactType);
   }

   public String[] getContactValues() {
      return this.getStringArrayField((short)2);
   }

   public void setContactValues(String[] contactValues) {
      this.setField((short)2, contactValues);
   }

   public String getTicketID() {
      return this.getStringField((short)3);
   }

   public void setTicketID(String ticketID) {
      this.setField((short)3, ticketID);
   }

   public FusionPktUploadAddressBookContactsOld() {
      super((short)960);
   }

   public FusionPktUploadAddressBookContactsOld(short transactionId) {
      super((short)960, transactionId);
   }

   public FusionPktUploadAddressBookContactsOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   public Gateway.ThreadPoolName getThreadPool() {
      return Gateway.ThreadPoolName.DATA_UPLOAD;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return processRequest(connection.getGatewayContext().getCommunicator(), connection.getUserID(), connection.getSessionID(), this.getTransactionId(), this.getContactType(), this.getContactValues(), this.getTicketID());
   }

   public static FusionPacket[] processRequest(Communicator communicator, int userid, String sessionid, short transactionID, Byte contactType, String[] contactValues, String ticketID) {
      long now = System.currentTimeMillis();
      if (communicator == null) {
         return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Invalid communication state")).setDetailedErrorMessage("'communicator' is null")).toArray();
      } else if (sessionid == null) {
         return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Invalid communication state")).setDetailedErrorMessage("'sessionid' is null")).toArray();
      } else if (StringUtil.isBlank(ticketID)) {
         return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("'ticketID' is blank")).toArray();
      } else if (contactType == null) {
         return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("'contactType' is null")).toArray();
      } else {
         AddressBookContactTypeEnum addrBookContactTypeEnum = AddressBookContactTypeEnum.fromCode(contactType);
         if (addrBookContactTypeEnum == null) {
            return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("'contactType' is " + contactType)).toArray();
         } else if (contactValues == null) {
            return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("'contactValues' is null")).toArray();
         } else {
            try {
               DataCollectorUtils.hitUploadDataRateLimit(userid, EXPECTED_DATA_TYPE);
               DataCollectorUtils.checkUserAccess(userid, EXPECTED_DATA_TYPE);
               DataUploadTicket uploadTicket = DataCollectorUtils.decryptDataUploadTicket(ticketID, sessionid, CollectedDataTypeEnum.ADDRESSBOOKCONTACT);
               if ((long)uploadTicket.dataType != EXPECTED_COLLECTED_DATA_TYPE_CODE) {
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("upload ticket was meant for uploading data type:[" + uploadTicket.dataType + "].Expected:[" + EXPECTED_COLLECTED_DATA_TYPE_CODE + "]")).toArray();
               } else if (uploadTicket.userid != userid) {
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("upload ticket was meant for userid:[" + uploadTicket.userid + "].Current userid:[" + userid + "]")).toArray();
               } else {
                  long timeDiff = now - uploadTicket.requestTimestamp;
                  if (timeDiff >= 0L) {
                     long maxTicketAgeMSec = 1000L * SystemProperty.getLong(SystemPropertyEntities.RecommendationServiceSettings.UPLOAD_TICKET_EXPIRY_SECS.forCollectedDataType(EXPECTED_DATA_TYPE));
                     if (timeDiff > maxTicketAgeMSec) {
                        return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("Ticket expired.Configured max ticket age:[" + maxTicketAgeMSec + "] millisec.System timestamp:[" + now + "].Upload ticket request timestamp:[" + uploadTicket.requestTimestamp + "]")).toArray();
                     } else {
                        int maxNumberOfEntriesPerUpload = SystemProperty.getInt(SystemPropertyEntities.RecommendationServiceSettings.MAX_NUMBER_OF_ENTRIES_PER_UPLOAD.forCollectedDataType(CollectedDataTypeEnum.ADDRESSBOOKCONTACT));
                        if (contactValues.length > maxNumberOfEntriesPerUpload) {
                           return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("Number of uploaded contacts is [" + contactValues.length + "], which exceeds [" + maxNumberOfEntriesPerUpload + "]")).toArray();
                        } else {
                           long maxTotalEntrySizesPerUpload = SystemProperty.getLong(SystemPropertyEntities.RecommendationServiceSettings.MAX_TOTAL_ENTRY_SIZES_PER_UPLOAD.forCollectedDataType(CollectedDataTypeEnum.ADDRESSBOOKCONTACT));
                           long totalEntrySizesUploaded = DataCollectorUtils.sumStringLength(contactValues);
                           if (totalEntrySizesUploaded > maxTotalEntrySizesPerUpload) {
                              return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("Total entry sizes uploaded is [" + totalEntrySizesUploaded + "], which exceeds [" + maxTotalEntrySizesPerUpload + "]")).toArray();
                           } else {
                              CollectedAddressBookDataIce collectedAddrBook = new CollectedAddressBookDataIce();
                              collectedAddrBook.contactType = addrBookContactTypeEnum.getCode();
                              collectedAddrBook.contactValues = contactValues;
                              collectedAddrBook.createTimestamp = now;
                              collectedAddrBook.dataType = uploadTicket.dataType;
                              collectedAddrBook.submitterUserId = userid;
                              RecommendationDataCollectionServicePrx rdcsPrx = DataCollectorUtils.getRDCSProxy(communicator);
                              rdcsPrx.logData(collectedAddrBook);
                              return (new FusionPktOk(transactionID)).toArray();
                           }
                        }
                     }
                  } else {
                     return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.WARN, DataCollectorUtils.newErrorID(), transactionID, "Invalid input")).setDetailedErrorMessage("Invalid ticket timestamp or system time inaccurate.System timestamp:[" + now + "] Upload ticket request timestamp:[" + uploadTicket.requestTimestamp + "]")).toArray();
                  }
               }
            } catch (DataCollectorException var20) {
               ErrorCause errorCause = var20.getErrorCause();
               if (errorCause instanceof ErrorCause.DataCollectorErrorReasonType) {
                  ErrorCause.DataCollectorErrorReasonType dataCollectorReason = (ErrorCause.DataCollectorErrorReasonType)errorCause;
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, dataCollectorReason.getSeverityLevel(), DataCollectorUtils.newErrorID(), transactionID, "Unable to upload data")).setExceptionCause(var20)).toArray();
               } else {
                  return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Unable to upload data")).setExceptionCause(var20)).toArray();
               }
            } catch (FusionExceptionWithRefCode var21) {
               String errorRef;
               if (!StringUtil.isBlank(var21.errorRef)) {
                  errorRef = var21.errorRef;
               } else {
                  errorRef = DataCollectorUtils.newErrorID();
               }

               return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.ERROR, errorRef, transactionID, "Failed to upload data")).setExceptionCause(var21)).toArray();
            } catch (Exception var22) {
               return PacketUtils.logAndPrepareFusionPktError((new PacketUtils.ErrorLogData((short)960, log, Level.ERROR, DataCollectorUtils.newErrorID(), transactionID, "Failed to upload data")).setExceptionCause(var22)).toArray();
            }
         }
      }
   }

   static {
      EXPECTED_DATA_TYPE = CollectedDataTypeEnum.ADDRESSBOOKCONTACT;
      EXPECTED_COLLECTED_DATA_TYPE_CODE = (long)EXPECTED_DATA_TYPE.getCode();
   }
}
