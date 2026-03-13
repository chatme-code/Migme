package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import Ice.ObjectNotExistException;
import com.projectgoth.fusion.chat.CoreChatStats;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktMessage extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktMessage.class));

   public FusionPktMessage() {
      super((short)500);
   }

   public FusionPktMessage(short transactionId) {
      super((short)500, transactionId);
   }

   public FusionPktMessage(MessageData message) {
      super((short)500);
      this.setMessageType(message.type.value());
      this.setSource(message.source);
      this.setContentType((short)message.contentType.value());
      this.setContentAsString(message.messageText);
      if (message.sourceContactID != null) {
         this.setContactId(message.sourceContactID);
      }

      if (message.messageDestinations != null && message.messageDestinations.size() == 1) {
         MessageDestinationData destination = (MessageDestinationData)message.messageDestinations.get(0);
         this.setDestinationType((byte)destination.type.value());
         this.setDestination(destination.destination);
      }

      if (message.fromAdministrator != null && message.fromAdministrator) {
         this.setFromAdministrator((byte)1);
      }

      if (message.sourceDisplayPicture != null) {
         this.setDisplayPicture(message.sourceDisplayPicture);
      }

      if (message.emoticonKeys != null && message.emoticonKeys.size() > 0) {
         StringBuilder hotKeysBuilder = new StringBuilder();
         Iterator i$ = message.emoticonKeys.iterator();

         while(i$.hasNext()) {
            String emoticonKey = (String)i$.next();
            hotKeysBuilder.append(emoticonKey).append(" ");
         }

         this.setHotKeys(hotKeysBuilder.toString());
      }

      if (message.sourceColour != null) {
         this.setSourceColor(message.sourceColour);
      }

      if (message.messageColour != null) {
         this.setMessageColor(message.messageColour);
      }

      if (message.guid != null) {
         this.setGUID(message.guid);
      }

      if (message.messageTimestamp != null) {
         this.setTimestamp(message.messageTimestamp);
      }

      if (message.groupChatName != null) {
         this.setGroupChatName(message.groupChatName);
      }

      if (message.groupChatOwner != null) {
         this.setGroupChatOwner(message.groupChatOwner);
      }

      if (message.contentType == MessageData.ContentTypeEnum.EMOTE && message.emoteContentType != null && message.emoteContentType != MessageData.EmoteContentTypeEnum.PLAIN) {
         this.setEmoteContentType(message.emoteContentType.value());
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED) && !StringUtil.isBlank(message.mimeType)) {
         this.setMimeType(message.mimeType);
         if (!StringUtil.isBlank(message.mimeTypeData)) {
            this.setMimeTypeData(message.mimeTypeData);
         }
      }

   }

   public FusionPktMessage(FusionPacket packet) {
      super(packet);
   }

   public boolean isFusionMessage() {
      Byte b = this.getMessageType();
      return b != null && b.intValue() == MessageType.FUSION.value();
   }

   public boolean isIndividualMessage() {
      Byte b = this.getDestinationType();
      return b != null && b.intValue() == MessageDestinationData.TypeEnum.INDIVIDUAL.value();
   }

   public Integer getContactId() {
      return this.getIntField((short)5);
   }

   public void setContactId(int contactId) {
      this.setField((short)5, contactId);
   }

   public Short getContentType() {
      return this.getShortField((short)6);
   }

   public void setContentType(short contactType) {
      this.setField((short)6, contactType);
   }

   public String getContentAsString() {
      return this.getStringField((short)8);
   }

   public void setContentAsString(String content) {
      this.setField((short)8, content);
   }

   public byte[] getContentAsByteArray() throws Exception {
      return this.getByteArrayField((short)8);
   }

   public void setContentAsByteArray(byte[] content) {
      this.setField((short)8, content);
   }

   public String getDestination() {
      String s = this.getStringField((short)4);
      return s != null && this.isFusionMessage() && this.isIndividualMessage() ? s.toLowerCase() : s;
   }

   public void setDestination(String destination) {
      this.setField((short)4, destination);
   }

   public Byte getDestinationType() {
      return this.getByteField((short)3);
   }

   public MessageDestinationData.TypeEnum getDestinationTypeEnum() {
      return MessageDestinationData.TypeEnum.fromValue(this.getDestinationType());
   }

   public void setDestinationType(byte destinationType) {
      this.setField((short)3, destinationType);
   }

   public String getFileName() {
      return this.getStringField((short)7);
   }

   public void setFileName(String fileName) {
      this.setField((short)7, fileName);
   }

   public Byte getMessageType() {
      return this.getByteField((short)1);
   }

   public MessageType getMessageTypeEnum() {
      return MessageType.fromValue(this.getMessageType());
   }

   public void setMessageType(byte messageType) {
      this.setField((short)1, messageType);
   }

   public String getSource() {
      String s = this.getStringField((short)2);
      return s != null && this.isFusionMessage() ? s.trim().toLowerCase() : s;
   }

   public void setSource(String source) {
      this.setField((short)2, source);
   }

   public Byte getFromAdministrator() {
      return this.getByteField((short)9);
   }

   public void setFromAdministrator(byte fromAdministrator) {
      this.setField((short)9, fromAdministrator);
   }

   public String getDisplayPicture() {
      return this.getStringField((short)10);
   }

   public void setDisplayPicture(String displayPicture) {
      this.setField((short)10, displayPicture);
   }

   public String getHotKeys() {
      return this.getStringField((short)11);
   }

   public void setHotKeys(String hotKeys) {
      this.setField((short)11, hotKeys);
   }

   public Integer getSourceColor() {
      return this.getIntField((short)12);
   }

   public void setSourceColor(int sourceColor) {
      this.setField((short)12, sourceColor);
   }

   public Integer getMessageColor() {
      return this.getIntField((short)13);
   }

   public void setMessageColor(int messageColor) {
      this.setField((short)13, messageColor);
   }

   public String getBadgeHotKey() {
      return this.getStringField((short)14);
   }

   public void setBadgeHotKey(String badgeHotKey) {
      this.setField((short)14, badgeHotKey);
   }

   public String getGUID() {
      if (log.isDebugEnabled()) {
         log.debug("Getting message GUID=" + this.getStringField((short)15));
      }

      return this.getStringField((short)15);
   }

   public void setGUID(String guid) {
      if (log.isDebugEnabled()) {
         log.debug("Setting message GUID=" + guid);
      }

      this.setField((short)15, guid);
   }

   public Long getTimestamp() {
      return this.getLongField((short)16);
   }

   public void setTimestamp(long ts) {
      this.setField((short)16, ts);
   }

   public String getGroupChatName() {
      return this.getStringField((short)17);
   }

   public void setGroupChatName(String name) {
      this.setField((short)17, name);
   }

   public String getGroupChatOwner() {
      return this.getStringField((short)18);
   }

   public void setGroupChatOwner(String ownerUsername) {
      this.setField((short)18, ownerUsername);
   }

   public String getPreviousMessageGUID() {
      return this.getStringField((short)19);
   }

   public void setPreviousMessageGUID(String guid) {
      this.setField((short)19, guid);
   }

   public void setEmoteContentType(byte emoteContentType) {
      this.setField((short)20, emoteContentType);
   }

   public String getMimeType() {
      return this.getStringField((short)21);
   }

   public void setMimeType(String type) {
      this.setField((short)21, type);
   }

   public String getMimeTypeData() {
      return this.getStringField((short)22);
   }

   public void setMimeTypeData(String data) {
      this.setField((short)22, data);
   }

   public Byte getStatus() {
      return this.getByteField((short)23);
   }

   public void setStatus(Enums.MessageStatusEventTypeEnum status) {
      this.setField((short)23, (byte)status.value());
   }

   public Byte getEmoteContentType() {
      return this.getByteField((short)20);
   }

   public boolean sessionRequired() {
      return true;
   }

   public boolean isPrivateOrGroupChatMessage() {
      return this.getDestinationType() == MessageDestinationData.TypeEnum.INDIVIDUAL.value() || this.getDestinationType() == MessageDestinationData.TypeEnum.GROUP.value();
   }

   public String getChatRoomNameForRateLimit() {
      return MessageDestinationData.TypeEnum.CHAT_ROOM.value() == this.getDestinationType() ? this.getDestination() : null;
   }

   public void preValidate(ConnectionI connection) throws FusionRequestException {
      if (MessageDestinationData.TypeEnum.CHAT_ROOM.value() == this.getDestinationType() && connection.isBannedFromChatrooms()) {
         throw new FusionRequestException(FusionRequestException.ExceptionType.PREVALIDATION, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BAN_ERROR_MESSAGE));
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      long startMillis = System.currentTimeMillis();

      FusionPacket[] var5;
      try {
         CoreChatStats.getInstance().incrementTotalMessagePacketsReceived();
         Byte type = this.getMessageType();
         if (type == null) {
            throw new Exception("Message type not set");
         }

         MessageType msgDataType = MessageType.fromValue(type);
         MessageDestinationData.TypeEnum msgDestinationType = MessageDestinationData.TypeEnum.fromValue(this.getDestinationType());
         String msgDestinationName = this.getDestination();
         if (msgDataType == MessageType.FUSION && msgDestinationType == MessageDestinationData.TypeEnum.CHAT_ROOM) {
            ChatRoomUtils.validateChatRoomNameForAccess(msgDestinationName);
         }

         MessageData msg = new MessageData();
         MessageDestinationData destination = new MessageDestinationData();
         msg.username = connection.getUsername();
         msg.dateCreated = new Date(System.currentTimeMillis());
         msg.requestReceivedTimestamp = new Date(this.timeReceived);
         msg.sendReceive = MessageData.SendReceiveEnum.SEND;
         msg.type = msgDataType;
         if (msg.type == null) {
            throw new Exception("Invalid message type");
         }

         if (msg.type == MessageType.FUSION) {
            msg.source = msg.username;
            msg.sourceColour = this.getSourceColor();
         } else {
            msg.source = this.getSource();
         }

         msg.contentType = MessageData.ContentTypeEnum.fromValue(this.getContentType());
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED) && !StringUtil.isBlank(this.getMimeType())) {
            msg.messageText = this.getContentAsString();
            msg.mimeType = this.getMimeType();
            msg.mimeTypeData = this.getMimeTypeData();
         } else if (msg.contentType != MessageData.ContentTypeEnum.TEXT && msg.contentType != MessageData.ContentTypeEnum.EXISTING_FILE) {
            msg.binaryData = this.getContentAsByteArray();
            if (msg.binaryData == null) {
               throw new Exception("Empty binary message");
            }

            FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.FILE_UPLOAD_PER_MINUTE, FloodControl.Action.FILE_UPLOAD_DAILY.setMaxHits(SystemProperty.getLong("UploadFileDailyRateLimit", 1000L))});
            if (!msg.username.equalsIgnoreCase(msg.source)) {
               throw new Exception("Invalid username specified");
            }

            int limit = SystemProperty.getInt("MaxImageUploadSize", Integer.MAX_VALUE);
            if (msg.binaryData.length > limit) {
               throw new Exception("Binary message exceeds size limit");
            }
         } else {
            msg.messageText = this.getContentAsString();
            if (msg.messageText == null || msg.messageText.trim().length() == 0) {
               throw new Exception("Empty message");
            }

            msg.messageColour = this.getMessageColor();
         }

         destination.type = msgDestinationType;
         destination.contactID = this.getContactId();
         destination.destination = msgDestinationName;
         msg.messageDestinations = new LinkedList();
         msg.messageDestinations.add(destination);
         msg.sourceColour = null;
         msg.messageColour = null;
         msg.guid = this.getGUID();
         if (StringUtil.isBlank(msg.guid)) {
            msg.guid = UUID.randomUUID().toString();
            if (log.isDebugEnabled()) {
               log.debug("Set GUID to server-generated value=" + msg.guid);
            }
         }

         msg.messageTimestamp = System.currentTimeMillis();
         FusionPacket[] var29;
         switch(msg.type) {
         case OFFLINE_MESSAGE:
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.EMAIL_BASED_OFFLINE_MESSAGE_DISABLED)) {
               var29 = this.oldStyleEmailBasedOfflineMessaging(connection, msg, destination);
               return var29;
            }

            if (log.isDebugEnabled()) {
               log.debug("Converting old-style email OFFLINE_MESSAGE from a J2ME client to a fusion message");
            }

            msg.type = MessageType.FUSION;
         case FUSION:
         case MSN:
         case AIM:
         case YAHOO:
         case GTALK:
         case FACEBOOK:
            SessionPrx sessionPrx = connection.getSessionPrx();
            if (sessionPrx == null) {
               throw new Exception("User not logged in");
            }

            sessionPrx.sendMessage(msg.toIceObject());
            break;
         case SMS:
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            msg = messageEJB.sendSMS(msg, new AccountEntrySourceData(connection));
            break;
         case EMAIL:
            throw new Exception("Unsupported message type " + type);
         }

         var29 = this.generateOkPacket(msg.type, msg.guid, msg.messageTimestamp);
         return var29;
      } catch (CreateException var19) {
         var5 = (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "EJB create exception - " + var19.getMessage())).toArray();
         return var5;
      } catch (RemoteException var20) {
         var5 = (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to send message - " + RMIExceptionHelper.getRootMessage(var20))).toArray();
         return var5;
      } catch (ObjectNotExistException var21) {
         log.error("ObjectNotExistException with connection.lastAccessed=" + connection.getLastAccessed() + ", connection.sessionPrxLastTouchedTime=" + connection.getSessionPrxLastTouchedTime() + ", connection.sessionCreatedTime=" + connection.getSessionCreatedTime());
         var5 = (new FusionPktInternalServerError(this.transactionId, var21, "Failed to send message")).toArray();
      } catch (LocalException var22) {
         var5 = (new FusionPktInternalServerError(this.transactionId, var22, "Failed to send message")).toArray();
         return var5;
      } catch (FusionException var23) {
         var5 = (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var23.message)).toArray();
         return var5;
      } catch (Exception var24) {
         var5 = (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var24.getMessage())).toArray();
         return var5;
      } finally {
         CoreChatStats.getInstance().addMessagePacketProcessRequestWallclockTime(System.currentTimeMillis() - startMillis);
      }

      return var5;
   }

   private FusionPacket[] generateOkPacket(MessageType msgType, String msgGUID, long msgTimestamp) {
      return msgType != MessageType.FUSION ? (new FusionPktOk(this.transactionId)).toArray() : (new FusionPktOk(this.transactionId, msgGUID, msgTimestamp)).toArray();
   }

   /** @deprecated */
   @Deprecated
   private FusionPacket[] oldStyleEmailBasedOfflineMessaging(ConnectionI connection, MessageData msg, MessageDestinationData destination) throws Exception {
      if (!connection.getGateway().getEnableOfflineMessage()) {
         throw new Exception("Offline messaging is temporarily unavailable. Please try again later");
      } else {
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), FloodControl.Action.SEND_EMAIL);
         UserData userData = new UserData(connection.getUserObject().getUserData());
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         messageEJB.sendEmail(msg.username, userData.password, destination.destination, "", msg.messageText);
         return (new FusionPktOk(this.transactionId, "Mail successfully sent to " + destination.destination)).toArray();
      }
   }
}
