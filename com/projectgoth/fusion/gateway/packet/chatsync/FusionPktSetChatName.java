package com.projectgoth.fusion.gateway.packet.chatsync;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ChatDestinationType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataSetChatName;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionPktInternalServerError;
import com.projectgoth.fusion.gateway.packet.FusionPktOk;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktSetChatName extends FusionPktDataSetChatName {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktSetChatName.class));

   public FusionPktSetChatName(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktSetChatName(FusionPacket packet) {
      super(packet);
   }

   public FusionPktSetChatName(String chatID, byte chatType, String chatName) {
      this.setChatId(chatID);
      this.setDestinationType(ChatDestinationType.fromValue(chatType));
      this.setChatName(chatName);
   }

   protected FusionPacket[] processRequest(ConnectionI cxn) {
      try {
         if (!MessageSwitchboardDispatcher.getInstance().isFeatureEnabled()) {
            return new FusionPacket[]{new FusionPktError(this.transactionId)};
         } else {
            MessageSwitchboardDispatcher.getInstance().setChatName(cxn, this.getChatId(), this.getDestinationType().value(), this.getChatName());
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }
      } catch (LocalException var4) {
         return (new FusionPktInternalServerError(this.transactionId, var4, "Failed to set chat name")).toArray();
      } catch (Exception var5) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set chat name - " + var5.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
