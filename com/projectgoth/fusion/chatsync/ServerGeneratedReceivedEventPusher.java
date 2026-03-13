package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import org.apache.log4j.Logger;

public class ServerGeneratedReceivedEventPusher extends MessageStatusEventPersistable {
   private static final LogFilter log;
   final FusionPktMessage sentMessage;
   final RegistryPrx registryPrx;

   public ServerGeneratedReceivedEventPusher(FusionPktMessage msg, Enums.MessageStatusEventTypeEnum status, boolean serverGenerated, RegistryPrx regy) throws FusionException {
      super(msg, status, serverGenerated);
      this.sentMessage = msg;
      this.registryPrx = regy;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Pushing server-generated RECEIVED event to " + this.sentMessage.getSource() + " with message type=" + this.messageType + ", guid=" + this.messageGUID + ", timestamp=" + this.messageTimestamp);
      }

      super.store(stores);

      try {
         UserPrx messageSender = this.registryPrx.findUserObject(this.sentMessage.getSource());
         messageSender.putMessageStatusEvent(this.toIceObject());
         if (log.isDebugEnabled()) {
            log.debug("Pushed server-generated RECEIVED event to " + this.sentMessage.getSource() + ", guid=" + this.messageGUID);
         }
      } catch (ObjectNotFoundException var3) {
         log.warn("Message sender went offline before server-generated RECEIVED event could be pushed (typical bot behaviour), message sender=" + this.sentMessage.getSource());
      }

   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ServerGeneratedReceivedEventPusher.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
