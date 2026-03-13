package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.exception.GroupChatNoLongerInChatSyncException;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import org.apache.log4j.Logger;

public class FusionPktLeaveGroupChatOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLeaveGroupChatOld.class));

   public FusionPktLeaveGroupChatOld() {
      super((short)753);
   }

   public FusionPktLeaveGroupChatOld(short transactionId) {
      super((short)753, transactionId);
   }

   public FusionPktLeaveGroupChatOld(FusionPacket packet) {
      super(packet);
   }

   public String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public Byte getIMType() {
      return this.getByteField((short)2);
   }

   public void setIMType(byte imType) {
      this.setField((short)2, imType);
   }

   public boolean sessionRequired() {
      return true;
   }

   private FusionPacket[] leaveFusionGroupChat(ConnectionI connection) throws Exception {
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
            } catch (ObjectNotFoundException var7) {
               groupChatPrx = this.restoreFusionGroupChat(connection, groupChatId);
            } catch (LocalException var8) {
               groupChatPrx = this.restoreFusionGroupChat(connection, groupChatId);
            }

            boolean participantFound = false;
            if (groupChatPrx != null) {
               participantFound = groupChatPrx.removeParticipant(connection.getUsername());
            }

            if (!participantFound) {
               RegistryPrx regy = connection.getGatewayContext().getRegistryPrx();
               MessageSwitchboardDispatcher.getInstance().onLeaveGroupChat(regy, connection.getUsername(), connection.getUserID(), this.getGroupChatId(), connection.getUserPrx());
            }

            return (new FusionPktOk(this.transactionId)).toArray();
         }
      }
   }

   private GroupChatPrx restoreFusionGroupChat(ConnectionI connection, String groupChatId) throws Exception {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.ENABLED)) {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktLeaveGroupChat: not restoring group chatID=" + groupChatId + " as persistent group chats disabled");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktLeaveGroupChat: restoring group chatID=" + groupChatId);
         }

         try {
            return connection.getSessionPrx().findGroupChatObject(groupChatId);
         } catch (Exception var4) {
            if (log.isDebugEnabled()) {
               log.debug("FusionPktLeaveGroupChat: failed to restore group chatID=" + this.getGroupChatId() + " exception=" + var4);
            }

            throw new GroupChatNoLongerInChatSyncException(groupChatId);
         }
      }
   }

   private FusionPacket[] leaveOtherIMConferenceChat(ConnectionI connection, ImType imType) throws Exception {
      String conferenceId = this.getGroupChatId();
      if (conferenceId == null) {
         throw new Exception("You must specify a conference ID");
      } else {
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new Exception("You are no longer logged in");
         } else {
            userPrx.otherIMLeaveConference(imType.value(), conferenceId);
            return (new FusionPktOk(this.transactionId)).toArray();
         }
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ImType imType = ImType.FUSION;
         Byte byteVal = this.getIMType();
         if (byteVal != null) {
            imType = ImType.fromValue(byteVal);
            if (imType == null) {
               throw new Exception("Invalid IM type " + byteVal);
            }
         }

         switch(imType) {
         case FUSION:
            return this.leaveFusionGroupChat(connection);
         case MSN:
         case YAHOO:
            return this.leaveOtherIMConferenceChat(connection, imType);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (FusionException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave group chat - " + var4.message)).toArray();
      } catch (LocalException var5) {
         return (new FusionPktInternalServerError(this.transactionId, var5, "Failed to leave group chat")).toArray();
      } catch (Exception var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to leave group chat - " + var6.getMessage())).toArray();
      }
   }
}
