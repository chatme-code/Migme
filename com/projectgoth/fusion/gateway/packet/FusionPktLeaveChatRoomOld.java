package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import org.apache.log4j.Logger;

public class FusionPktLeaveChatRoomOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLeaveChatRoomOld.class));

   public FusionPktLeaveChatRoomOld() {
      super((short)704);
   }

   public FusionPktLeaveChatRoomOld(short transactionId) {
      super((short)704, transactionId);
   }

   public FusionPktLeaveChatRoomOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public boolean sessionRequired() {
      return true;
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatRoomName();
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         RegistryPrx reg = connection.findRegistry();
         MessageSwitchboardDispatcher.getInstance().onLeaveChatRoom(reg, connection.getUsername(), connection.getUserID(), this.getChatRoomName(), connection.getUserPrx());
         if (log.isDebugEnabled()) {
            log.debug("Successfully called MSB.onLeaveChatRoom for user=" + connection.getUsername());
         }
      } catch (Exception var5) {
         log.error("Exception calling MessageSwitchboard.onLeaveChatRoom: " + var5, var5);
      }

      FusionPktError pktError;
      try {
         String chatRoomName = this.getChatRoomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx != null) {
            ChatRoomPrx chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
            chatRoomPrx.removeParticipant(connection.getUsername());
         }
      } catch (ChatRoomValidationException var6) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave chat room - " + var6.getMessage());
         return new FusionPacket[]{pktError};
      } catch (FusionException var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave chat room - " + var7.getMessage());
         return new FusionPacket[]{pktError};
      } catch (Exception var8) {
         log.error("Caught exception while trying to leave chatroom", var8);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave chat room - Internal Error");
         return new FusionPacket[]{pktError};
      }

      return new FusionPacket[]{new FusionPktOk(this.transactionId)};
   }
}
