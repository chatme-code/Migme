package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.util.Arrays;
import org.apache.log4j.Logger;

public class FusionPktGetChatRoomParticipantsOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetChatRoomParticipantsOld.class));

   public FusionPktGetChatRoomParticipantsOld() {
      super((short)707);
   }

   public FusionPktGetChatRoomParticipantsOld(short transactionId) {
      super((short)707, transactionId);
   }

   public FusionPktGetChatRoomParticipantsOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   private String concat(String[] stringArray, String seperator) {
      StringBuilder builder = new StringBuilder();
      if (stringArray != null) {
         Arrays.sort(stringArray);
         String[] arr$ = stringArray;
         int len$ = stringArray.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            builder.append(s).append(seperator);
         }
      }

      return builder.toString();
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatRoomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new Exception("Unable to locate registry");
         } else {
            ChatRoomPrx chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
            FusionPktChatRoomParticipantsOld reply = new FusionPktChatRoomParticipantsOld(this.transactionId);
            reply.setChatRoomName(chatRoomName);
            String[] participantArray = chatRoomPrx.getParticipants(connection.getUsername());
            String participants = this.concat(participantArray, ";");
            if (participants != null && participants.length() > 0) {
               reply.setParticipants(participants);
            }

            String administrators = this.concat(chatRoomPrx.getAdministrators(connection.getUsername()), ";");
            if (administrators != null && administrators.length() > 0) {
               reply.setAdministrators(administrators);
            }

            String mutedUsers = this.concat(connection.getUserPrx().getBlockListFromUsernames(participantArray), ";");
            if (mutedUsers != null && mutedUsers.length() > 0) {
               reply.setMutedParticipants(mutedUsers);
            }

            return new FusionPacket[]{reply};
         }
      } catch (ChatRoomValidationException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat room participants - " + var10.getMessage())).toArray();
      } catch (ObjectNotFoundException var11) {
         FusionPktChatRoomParticipantsOld reply = new FusionPktChatRoomParticipantsOld(this.transactionId);
         reply.setChatRoomName(this.getChatRoomName());
         return new FusionPacket[]{reply};
      } catch (Exception var12) {
         log.error("Exception while trying to get chat room participants.", var12);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat room participants - Internal Error")).toArray();
      }
   }
}
