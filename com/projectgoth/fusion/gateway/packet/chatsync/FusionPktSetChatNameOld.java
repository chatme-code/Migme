package com.projectgoth.fusion.gateway.packet.chatsync;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionPktInternalServerError;
import com.projectgoth.fusion.gateway.packet.FusionPktOk;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktSetChatNameOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktSetChatNameOld.class));

   public FusionPktSetChatNameOld() {
      super((short)564);
   }

   public FusionPktSetChatNameOld(String chatID, byte chatType, String chatName) {
      super((short)564);
      this.setChatIdentifier(chatID);
      this.setChatType(chatType);
      this.setChatName(chatName);
   }

   public FusionPktSetChatNameOld(String chatID, byte chatType, String chatName, short transactionId) {
      super((short)564, transactionId);
      this.setChatIdentifier(chatID);
      this.setChatType(chatType);
      this.setChatName(chatName);
   }

   public FusionPktSetChatNameOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatIdentifier() {
      return this.getStringField((short)1);
   }

   private void setChatIdentifier(String id) {
      this.setField((short)1, id);
   }

   public Byte getChatType() {
      return this.getByteField((short)2);
   }

   private void setChatType(byte chatType) {
      this.setField((short)2, chatType);
   }

   public String getChatName() {
      return this.getStringField((short)3);
   }

   private void setChatName(String name) {
      this.setField((short)3, name);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI cxn) {
      try {
         if (!MessageSwitchboardDispatcher.getInstance().isFeatureEnabled()) {
            return new FusionPacket[]{new FusionPktError(this.transactionId)};
         } else {
            MessageSwitchboardDispatcher.getInstance().setChatName(cxn, this.getChatIdentifier(), this.getChatType(), this.getChatName());
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
