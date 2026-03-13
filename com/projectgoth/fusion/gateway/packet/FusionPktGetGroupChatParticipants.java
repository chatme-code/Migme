package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.exception.GroupChatNoLongerInChatSyncException;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetGroupChatParticipants;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktGetGroupChatParticipants extends FusionPktDataGetGroupChatParticipants {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetGroupChatParticipants.class));
   private static final String GROUP_CHAT_TIMED_OUT_OF_CHATSYNC_ERROR = "Unable to get group chat participants - this group chat was inactive for too many days and is no longer stored";

   public FusionPktGetGroupChatParticipants() {
   }

   public FusionPktGetGroupChatParticipants(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetGroupChatParticipants(FusionPacket packet) {
      super(packet);
   }

   private FusionPacket[] getFusionGroupChatParticipants(ConnectionI connection) throws Exception {
      RegistryPrx registryPrx = connection.findRegistry();
      if (registryPrx == null) {
         throw new Exception("Unable to locate registry");
      } else {
         String groupChatId = this.getGroupChatId();
         if (groupChatId == null) {
            throw new Exception("You must specify a group chat ID");
         } else {
            GroupChatPrx groupChatPrx;
            try {
               groupChatPrx = registryPrx.findGroupChatObject(groupChatId);
            } catch (ObjectNotFoundException var9) {
               if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.ENABLED)) {
                  throw var9;
               }

               groupChatPrx = this.restoreGroupChat(connection, groupChatId);
            }

            FusionPktGroupChatParticipants reply = new FusionPktGroupChatParticipants(this.transactionId);
            reply.setGroupChatId(groupChatId);
            reply.setImType(ImType.FUSION);
            String[] participantArray = groupChatPrx.getParticipants(connection.getUsername());
            if (participantArray != null && participantArray.length > 0) {
               reply.setParticipantList(participantArray);
            }

            UserPrx userPrx = connection.getUserPrx();
            if (userPrx == null) {
               throw new Exception("You are no longer logged in");
            } else {
               String[] mutedUsers = userPrx.getBlockListFromUsernames(participantArray);
               if (mutedUsers != null && mutedUsers.length > 0) {
                  reply.setMutedList(mutedUsers);
               }

               return reply.toArray();
            }
         }
      }
   }

   private FusionPacket[] getOtherIMConferenceParticipants(ConnectionI connection, ImType imType) throws Exception {
      String conferenceId = this.getGroupChatId();
      if (conferenceId == null) {
         throw new Exception("You must specify a conference ID");
      } else {
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new Exception("You are no longer logged in");
         } else {
            String[] participants = userPrx.getOtherIMConferenceParticipants(imType.value(), conferenceId);
            FusionPktGroupChatParticipants reply = new FusionPktGroupChatParticipants(this.transactionId);
            reply.setGroupChatId(conferenceId);
            reply.setParticipantList(participants);
            reply.setImType(imType);
            return reply.toArray();
         }
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      ImType imType = null;

      try {
         imType = this.getImType();
         switch(imType != null ? imType : ImType.FUSION) {
         case FUSION:
            return this.getFusionGroupChatParticipants(connection);
         case MSN:
         case YAHOO:
            return this.getOtherIMConferenceParticipants(connection, imType);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (GroupChatNoLongerInChatSyncException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.CHAT_SYNC_ENTITY_NOT_FOUND, "Unable to get group chat participants - this group chat was inactive for too many days and is no longer stored")).toArray();
      } catch (ObjectNotFoundException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get group chat participants - group chat not found")).toArray();
      } catch (LocalException var6) {
         return (new FusionPktInternalServerError(this.transactionId, var6, "Failed to get group chat participants")).toArray();
      } catch (Exception var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get group chat participants - " + var7.getMessage())).toArray();
      }
   }

   private GroupChatPrx restoreGroupChat(ConnectionI connection, String groupChatId) throws Exception {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.ENABLED)) {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktGetGroupChatParticipants: not restoring group chatID=" + groupChatId + " as persistent group chats disabled");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktGetGroupChatParticipants: restoring group chatID=" + groupChatId);
         }

         try {
            return connection.getSessionPrx().findGroupChatObject(groupChatId);
         } catch (Exception var4) {
            if (log.isDebugEnabled()) {
               log.debug("FusionPktGetGroupChatParticipants: failed to restore group chatID=" + this.getGroupChatId() + " exception=" + var4);
            }

            throw new GroupChatNoLongerInChatSyncException(groupChatId);
         }
      }
   }
}
