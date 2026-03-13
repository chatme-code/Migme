package com.projectgoth.fusion.chatsync;

import com.google.gson.annotations.Expose;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.packet.FusionPktMessageStatusEvent;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageStatusEventIce;
import org.apache.log4j.Logger;

public class MessageStatusEvent extends MessageStatusEventKey {
   private static final LogFilter log;
   protected MessageType messageType;
   @Expose
   protected String messageGUID;
   protected Enums.MessageStatusEventTypeEnum messageStatus;
   protected boolean serverGenerated;
   protected long messageTimestamp;

   protected MessageStatusEvent() throws FusionException {
   }

   public MessageStatusEvent(MessageStatusEvent mse) throws FusionException {
      this(mse.toIceObject());
   }

   public MessageStatusEvent(MessageStatusEventIce msei) throws FusionException {
      super(new ChatDefinition(msei.messageDestination, (byte)msei.messageDestinationType, msei.messageSource), msei.messageSource, msei.messageDestination);
      this.messageType = MessageType.fromValue(msei.messageType);
      this.messageGUID = msei.messageGUID;
      this.messageStatus = Enums.MessageStatusEventTypeEnum.fromValue(msei.messageStatus);
      this.serverGenerated = msei.serverGenerated;
      this.messageTimestamp = msei.messageTimestamp;
   }

   public MessageStatusEvent(FusionPktMessageStatusEvent pkt) throws FusionException {
      super(new ChatDefinition(pkt.getMessageDestination(), pkt.getMessageDestinationType(), pkt.getMessageSource()), pkt.getMessageSource(), pkt.getMessageDestination());
      this.messageType = MessageType.fromValue(pkt.getMessageType());
      this.messageSource = pkt.getMessageSource();
      this.messageGUID = pkt.getMessageGUID();
      this.messageStatus = Enums.MessageStatusEventTypeEnum.fromValue(pkt.getMessageEventType());
      this.serverGenerated = pkt.getServerGenerated() == 1;
      this.messageTimestamp = pkt.getMessageTimestamp();
   }

   protected MessageStatusEvent(ChatDefinition chatID, String messageSource) throws FusionException {
      super(chatID, messageSource);
   }

   public MessageType getMessageType() {
      return this.messageType;
   }

   public String getMessageGUID() {
      return this.messageGUID;
   }

   public Enums.MessageStatusEventTypeEnum getMessageStatus() {
      return this.messageStatus;
   }

   public boolean getServerGenerated() {
      return this.serverGenerated;
   }

   public long getMessageTimestamp() {
      return this.messageTimestamp;
   }

   public static boolean isClientMessageStatusEventCapable(ClientType deviceType, short clientVersion) {
      Short propMinVer = SystemPropertyEntities.MessageStatusEventSettings.getReceiveFromMinVersion(deviceType);
      int minVer;
      if (propMinVer != null) {
         minVer = propMinVer;
      } else {
         minVer = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MessageStatusEventSettings.OTHER_CLIENTS_RX_ENABLED) ? 0 : 32767;
      }

      if (log.isDebugEnabled()) {
         log.debug((clientVersion >= minVer ? "Allowed" : "Not allowed") + " to generate message status events: " + " on device=" + deviceType + " client ver=" + clientVersion + " minimum ver=" + minVer + (propMinVer != null ? " via prop" : " via default"));
      }

      return clientVersion >= minVer;
   }

   public MessageStatusEventIce toIceObject() {
      MessageStatusEventIce ice = new MessageStatusEventIce();
      ice.messageType = this.messageType.value();
      ice.messageSource = this.messageSource;
      ice.messageDestinationType = this.getMessageDestinationType().value();
      ice.messageDestination = this.messageDestination;
      ice.messageGUID = this.messageGUID;
      ice.messageStatus = this.messageStatus.value();
      ice.serverGenerated = this.serverGenerated;
      ice.messageTimestamp = this.messageTimestamp;
      return ice;
   }

   public boolean shouldStore() {
      return this.messageStatus.equals(Enums.MessageStatusEventTypeEnum.RECEIVED) || this.messageStatus.equals(Enums.MessageStatusEventTypeEnum.READ);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageStatusEvent.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
