package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.packets.FusionPktDataKickChatroomParticipant;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktKickChatroomParticipant extends FusionPktDataKickChatroomParticipant {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktKickChatroomParticipant.class));

   public FusionPktKickChatroomParticipant() {
   }

   public FusionPktKickChatroomParticipant(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktKickChatroomParticipant(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatroomName();
   }

   public void preValidate(ConnectionI connection) throws FusionRequestException {
      if (connection.isBannedFromChatrooms()) {
         throw new FusionRequestException(FusionRequestException.ExceptionType.PREVALIDATION, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BAN_ERROR_MESSAGE));
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         String chatRoomName = this.getChatroomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         String userToKick = this.getUsername();
         if (userToKick == null) {
            throw new Exception("You must specify a user to kick");
         } else {
            RegistryPrx registryPrx = connection.findRegistry();
            if (registryPrx == null) {
               throw new Exception("Unable to locate registry");
            } else {
               ChatRoomPrx chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
               chatRoomPrx.voteToKickUser(connection.getUsername(), userToKick);
               return new FusionPacket[]{new FusionPktOk(this.transactionId)};
            }
         }
      } catch (ChatRoomValidationException var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to kick user - " + var6.getMessage())).toArray();
      } catch (ObjectNotFoundException var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to kick user - Unable to locate chat room");
         return new FusionPacket[]{pktError};
      } catch (FusionException var8) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var8.message);
         return new FusionPacket[]{pktError};
      } catch (Exception var9) {
         log.error("Caught exception while trying to kick user ", var9);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to kick user - Internal Error");
         return new FusionPacket[]{pktError};
      }
   }
}
