package com.projectgoth.fusion.gateway.packet.chatsync;

import Ice.LocalException;
import com.projectgoth.fusion.chatsync.ChatSyncStats;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionPktInternalServerError;
import com.projectgoth.fusion.gateway.packet.FusionPktOk;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktGetMessageStatusEvents extends FusionPktGetMessagesBase {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetMessageStatusEvents.class));
   private static final short MESSAGE_GUIDS_FIELD = 6;
   private static final short MESSAGE_TIMESTAMPS_FIELD = 7;

   public FusionPktGetMessageStatusEvents() {
      super((short)565);
   }

   public FusionPktGetMessageStatusEvents(short transactionId) {
      super((short)565, transactionId);
   }

   public FusionPktGetMessageStatusEvents(FusionPacket packet) {
      super(packet);
   }

   public String[] getMessageGUIDs() {
      return super.getStringArrayField((short)6);
   }

   public void setMessageGUIDs(String[] guids) {
      this.setField((short)6, guids);
   }

   public long[] getMessageTimestamps() {
      return this.getLongArrayField((short)7);
   }

   public void setMessageTimestamps(long[] timestamps) {
      this.setField((short)7, timestamps);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         if (!(Boolean)SystemPropertyEntities.MessageStatusEventSettings.Cache.enabled.getValue()) {
            return new FusionPacket[]{new FusionPktError(this.transactionId)};
         } else {
            if (!MemCachedRateLimiter.bypassRateLimit(connection.getUsername())) {
               int maxPerMin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MessageStatusEventSettings.MAX_GET_MESSAGE_STATUS_EVENTS_REQUESTS_PER_MINUTE);
               if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxGetMessageStatusEventsRequestsPerMin", (long)maxPerMin, 60000L)) {
                  return new FusionPacket[]{new FusionPktError(this.transactionId)};
               }
            }

            if (log.isDebugEnabled()) {
               log.debug("GET_MESSAGE_STATUS_EVENTS request received with chatID=" + this.getChatIdentifier());
            }

            ChatSyncStats.getInstance().incrementTotalGetMessageStatusEventsReceived();
            if (this.getMessageGUIDs() != null) {
               MessageSwitchboardDispatcher.getInstance().getAndPushMessageStatusEvents(connection, connection.getUsername(), this.getChatType(), this.getChatIdentifier(), this.getMessageGUIDs(), this.getMessageTimestamps(), Integer.MAX_VALUE, connection.getConnectionPrx(), connection.getDeviceTypeAsInt(), connection.getClientVersion(), this.transactionId);
            } else {
               long lOldest = this.getOldestMessageTimestamp() != null ? this.getOldestMessageTimestamp() : Long.MIN_VALUE;
               long lLatest = this.getLatestMessageTimestamp() != null ? this.getLatestMessageTimestamp() : Long.MIN_VALUE;
               MessageSwitchboardDispatcher.getInstance().getAndPushMessageStatusEvents(connection, connection.getUsername(), this.getChatType(), this.getChatIdentifier(), lOldest, lLatest, Integer.MAX_VALUE, connection.getConnectionPrx(), connection.getDeviceTypeAsInt(), connection.getClientVersion(), this.transactionId);
            }

            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }
      } catch (LocalException var6) {
         return (new FusionPktInternalServerError(this.transactionId, var6, "Failed to get messages")).toArray();
      } catch (Exception var7) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get messages - " + var7.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
