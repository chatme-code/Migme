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

public class FusionPktGetMessages extends FusionPktGetMessagesBase {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetMessages.class));

   public FusionPktGetMessages() {
      super((short)550);
   }

   public FusionPktGetMessages(short transactionId) {
      super((short)550, transactionId);
   }

   public FusionPktGetMessages(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         if (!MessageSwitchboardDispatcher.getInstance().isFeatureEnabled()) {
            return new FusionPacket[]{new FusionPktError(this.transactionId)};
         } else {
            int sysadminLimit;
            if (!MemCachedRateLimiter.bypassRateLimit(connection.getUsername())) {
               sysadminLimit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_GET_MESSAGE_REQUESTS_PER_MINUTE);
               if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxGetMessageRequestsPerMin", (long)sysadminLimit, 60000L)) {
                  return new FusionPacket[]{new FusionPktError(this.transactionId)};
               }
            }

            sysadminLimit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.GET_MESSAGES_SYSADMIN_LIMIT);
            Integer suppliedLimit = this.getLimit();
            int effectiveLimit;
            if (suppliedLimit != null && suppliedLimit <= sysadminLimit) {
               effectiveLimit = suppliedLimit;
            } else {
               effectiveLimit = sysadminLimit;
            }

            if (log.isDebugEnabled()) {
               log.debug("GET_MESSAGES request received with chatID=" + this.getChatIdentifier() + " start=" + this.getOldestMessageTimestamp() + " end=" + this.getLatestMessageTimestamp() + " supplied limit=" + suppliedLimit + " sysadmin limit=" + sysadminLimit + " effective limit=" + effectiveLimit);
            }

            ChatSyncStats.getInstance().incrementTotalGetMessagesReceived();
            long lOldest = this.getOldestMessageTimestamp() != null ? this.getOldestMessageTimestamp() : Long.MIN_VALUE;
            long lLatest = this.getLatestMessageTimestamp() != null ? this.getLatestMessageTimestamp() : Long.MIN_VALUE;
            MessageSwitchboardDispatcher.getInstance().getAndPushMessages(connection, connection.getUsername(), this.getChatType(), this.getChatIdentifier(), lOldest, lLatest, effectiveLimit, connection.getConnectionPrx(), connection.getDeviceTypeAsInt(), connection.getClientVersion(), this.transactionId);
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }
      } catch (LocalException var9) {
         return (new FusionPktInternalServerError(this.transactionId, var9, "Failed to get messages")).toArray();
      } catch (Exception var10) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get messages - " + var10.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
