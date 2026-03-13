package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetChatroomParticipants;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.log4j.Logger;

public class FusionPktGetChatroomParticipants extends FusionPktDataGetChatroomParticipants {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetChatroomParticipants.class));

   public FusionPktGetChatroomParticipants(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetChatroomParticipants(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetChatroomParticipants(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatroomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new Exception("Unable to locate registry");
         } else {
            ChatRoomPrx chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
            FusionPktChatroomParticipants reply = new FusionPktChatroomParticipants(this.transactionId);
            reply.setChatroomName(chatRoomName);
            String[] participantArray = chatRoomPrx.getParticipants(connection.getUsername());
            if (participantArray != null && participantArray.length > 0) {
               Arrays.sort(participantArray);
               reply.setParticipantList(participantArray);
            }

            String[] administrators = chatRoomPrx.getAdministrators(connection.getUsername());
            if (administrators != null && administrators.length > 0) {
               Arrays.sort(administrators);
               reply.setAdministratorList(administrators);
            }

            String[] mutedUsers = connection.getUserPrx().getBlockListFromUsernames(participantArray);
            if (mutedUsers != null && mutedUsers.length > 0) {
               Arrays.sort(mutedUsers);
               reply.setMutedList(mutedUsers);
            }

            return new FusionPacket[]{reply};
         }
      } catch (ChatRoomValidationException var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat room participants - " + var9.getMessage())).toArray();
      } catch (ObjectNotFoundException var10) {
         FusionPktChatroomParticipants reply = new FusionPktChatroomParticipants(this.transactionId);
         reply.setChatroomName(this.getChatroomName());
         return new FusionPacket[]{reply};
      } catch (Exception var11) {
         log.error("Exception while trying to get chat room participants.", var11);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat room participants - Internal Error")).toArray();
      }
   }
}
