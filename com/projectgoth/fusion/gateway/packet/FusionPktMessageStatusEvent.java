package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.chatsync.ChatSyncStorageExecutor;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.chatsync.MessageStatusEventPersistable;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;
import org.apache.log4j.Logger;

public class FusionPktMessageStatusEvent extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktMessageStatusEvent.class));
   public static final short MESSAGE_TYPE_FIELD = 1;
   public static final short SOURCE_FIELD = 2;
   public static final short DESTINATION_TYPE_FIELD = 3;
   public static final short DESTINATION_FIELD = 4;
   public static final short GUID_FIELD = 5;
   public static final short STATUS_EVENT_TYPE_FIELD = 6;
   public static final short SERVER_GENERATED_FIELD = 7;
   public static final short MESSAGE_TIMESTAMP_FIELD = 8;

   public FusionPktMessageStatusEvent() {
      super((short)505);
   }

   public FusionPktMessageStatusEvent(short transactionId) {
      super((short)505, transactionId);
   }

   public FusionPktMessageStatusEvent(FusionPacket packet) {
      super(packet);
   }

   public FusionPktMessageStatusEvent(MessageStatusEvent mse) throws FusionException {
      super((short)505);
      this.setMessageType(mse.getMessageType().value());
      this.setMessageSource(mse.getMessageSource());
      this.setMessageDestinationType((byte)mse.getMessageDestinationType().value());
      this.setMessageDestination(mse.getMessageDestination());
      this.setMessageGUID(mse.getMessageGUID());
      this.setMessageEventType((byte)mse.getMessageStatus().value());
      this.setServerGenerated((byte)(mse.getServerGenerated() ? 1 : 0));
   }

   public Byte getMessageType() {
      return this.getByteField((short)1);
   }

   public void setMessageType(byte messageType) {
      this.setField((short)1, messageType);
   }

   public String getMessageSource() {
      return this.getStringField((short)2);
   }

   public void setMessageSource(String source) {
      this.setField((short)2, source);
   }

   public Byte getMessageDestinationType() {
      return this.getByteField((short)3);
   }

   public void setMessageDestinationType(byte destinationType) {
      this.setField((short)3, destinationType);
   }

   public String getMessageDestination() {
      return this.getStringField((short)4);
   }

   public void setMessageDestination(String destination) {
      this.setField((short)4, destination);
   }

   public String getMessageGUID() {
      return this.getStringField((short)5);
   }

   public void setMessageGUID(String guid) {
      this.setField((short)5, guid);
   }

   public Byte getMessageEventType() {
      return this.getByteField((short)6);
   }

   public void setMessageEventType(byte type) {
      this.setField((short)6, type);
   }

   public byte getServerGenerated() {
      Byte serverGenerated = this.getByteField((short)7);
      return serverGenerated != null ? serverGenerated : 0;
   }

   public void setServerGenerated(byte serverGenerated) {
      this.setField((short)7, this.type);
   }

   public long getMessageTimestamp() {
      return this.getLongField((short)8);
   }

   public void setMessageTimestamp(long timestamp) {
      this.setField((short)8, timestamp);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktSetMessageEvent.processRequest for userID=" + connection.getUserID());
         }

         if (!(Boolean)SystemPropertyEntities.MessageStatusEventSettings.Cache.enabled.getValue()) {
            if (log.isDebugEnabled()) {
               log.debug("Fusion message status events feature disabled via kill switch... dropping out");
            }

            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         } else {
            byte eventType = this.getMessageEventType();
            Enums.MessageStatusEventTypeEnum typeEnum = Enums.MessageStatusEventTypeEnum.fromValue(eventType);
            if (typeEnum == null) {
               FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "The message event request could not be processed");
               return new FusionPacket[]{pktError};
            } else {
               String guid = this.getMessageGUID();
               if (guid.length() >= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MessageStatusEventSettings.MIN_GUID_CHARS) && guid.length() <= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MessageStatusEventSettings.MAX_GUID_CHARS)) {
                  SessionPrx sessionPrx = connection.getSessionPrx();
                  if (sessionPrx == null) {
                     if (log.isDebugEnabled()) {
                        log.debug("Fusion message status events: event originator not logged in");
                     }

                     FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in");
                     return new FusionPacket[]{pktError};
                  } else if (!MessageStatusEvent.isClientMessageStatusEventCapable(connection.getDeviceType(), connection.getClientVersion())) {
                     return new FusionPacket[]{new FusionPktOk(this.transactionId)};
                  } else if (this.getMessageType() == MessageType.FUSION.value() && this.getMessageDestinationType() == (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
                     if (log.isDebugEnabled()) {
                        log.debug("1-1 fusion message: processing message status event request from " + connection.getUsername());
                     }

                     MessageStatusEventPersistable mse = new MessageStatusEventPersistable(this);
                     if (mse.shouldStore()) {
                        ChatSyncStorageExecutor.getInstance().scheduleStorage(mse);
                        if (log.isDebugEnabled()) {
                           log.debug("Scheduled storage of message status event for msg guid=" + mse.getMessageGUID());
                        }
                     }

                     try {
                        UserPrx userPrx;
                        switch(mse.getMessageStatus()) {
                        case RECEIVED:
                        case READ:
                           userPrx = connection.getGatewayContext().getRegistryPrx().findUserObject(this.getMessageSource());
                           break;
                        case COMPOSING:
                           userPrx = connection.getGatewayContext().getRegistryPrx().findUserObject(this.getMessageDestination());
                           break;
                        default:
                           throw new FusionException("Invalid message status event type");
                        }

                        userPrx.putMessageStatusEvent(mse.toIceObject());
                        if (log.isDebugEnabled()) {
                           log.debug("Broadcast message status event for msg guid=" + mse.getMessageGUID());
                        }
                     } catch (ObjectNotFoundException var8) {
                        if (log.isDebugEnabled()) {
                           log.debug("MessageStatusEvent recipient offline for msg guid=" + mse.getMessageGUID());
                        }
                     }

                     return new FusionPacket[]{new FusionPktOk(this.transactionId)};
                  } else {
                     if (log.isDebugEnabled()) {
                        log.debug("Fusion message status events: only supported for 1-1 fusion messages. Dropping out");
                     }

                     return new FusionPacket[]{new FusionPktOk(this.transactionId)};
                  }
               } else {
                  FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "The message event request could not be processed");
                  return new FusionPacket[]{pktError};
               }
            }
         }
      } catch (LocalException var9) {
         log.error("Exception setting message event: e=" + var9, var9);
         return (new FusionPktInternalServerError(this.transactionId, var9, "Failed to set message event")).toArray();
      } catch (Exception var10) {
         log.error("Exception setting message event: e=" + var10, var10);
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set message event - " + var10.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
