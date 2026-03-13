package com.projectgoth.fusion.gateway.packet.chatsync;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionPktInternalServerError;
import com.projectgoth.fusion.gateway.packet.FusionPktOk;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktHaveLatestChatList extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktHaveLatestChatList.class));

   public FusionPktHaveLatestChatList() {
      super((short)552);
   }

   public FusionPktHaveLatestChatList(short transactionId) {
      super((short)552, transactionId);
   }

   public FusionPktHaveLatestChatList(FusionPacket packet) {
      super(packet);
   }

   public Integer getVersion() {
      return this.getIntField((short)1);
   }

   public void setVersion(int ver) {
      this.setField((short)1, ver);
   }

   public Long getTimestamp() {
      return this.getLongField((short)2);
   }

   public void setTimestamp(long ts) {
      this.setField((short)2, ts);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_SYNC_ENABLED)) {
            log.debug("Chat sync disabled via kill switch... dropping out");
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         } else {
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }
      } catch (LocalException var4) {
         return (new FusionPktInternalServerError(this.transactionId, var4, "Failed to check chat list")).toArray();
      } catch (Exception var5) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to check chat list - " + var5.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
