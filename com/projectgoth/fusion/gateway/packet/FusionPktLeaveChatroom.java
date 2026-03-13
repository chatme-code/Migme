package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.packets.FusionPktDataLeaveChatroom;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktLeaveChatroom extends FusionPktDataLeaveChatroom {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLeaveChatroom.class));

   public FusionPktLeaveChatroom() {
   }

   public FusionPktLeaveChatroom(short transactionId) {
      super(transactionId);
   }

   public FusionPktLeaveChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktLeaveChatroom(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatroomName();
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         RegistryPrx reg = connection.findRegistry();
         MessageSwitchboardDispatcher.getInstance().onLeaveChatRoom(reg, connection.getUsername(), connection.getUserID(), this.getChatroomName(), connection.getUserPrx());
         if (log.isDebugEnabled()) {
            log.debug("Successfully called MSB.onLeaveChatRoom for user=" + connection.getUsername());
         }
      } catch (Exception var5) {
         log.error("Exception calling MessageSwitchboard.onLeaveChatRoom: " + var5, var5);
      }

      FusionPktError pktError;
      try {
         String chatRoomName = this.getChatroomName();
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
