package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.exception.GroupChatNoLongerInChatSyncException;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class FusionPktGroupChatInvite extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGroupChatInvite.class));
   private static final short FIELD_OTHER_PARTY_LIST = 4;
   private static final String INVALID_PARAMS_ERROR = "Invalid combination of parameters specified";
   private static final String WEB_PREFIX = "web:";
   private static final Pattern COMMA_SPLITTER = Pattern.compile(",");

   public FusionPktGroupChatInvite() {
      super((short)752);
   }

   public FusionPktGroupChatInvite(short transactionId) {
      super((short)752, transactionId);
   }

   public FusionPktGroupChatInvite(FusionPacket packet) {
      super(packet);
   }

   public String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public String getUsername() {
      return this.getStringField((short)2);
   }

   public void setUsername(String username) {
      this.setField((short)2, username);
   }

   public Byte getIMType() {
      return this.getByteField((short)3);
   }

   public void setIMType(byte imType) {
      this.setField((short)3, imType);
   }

   public String[] getUsernames() {
      String asString = this.getStringField((short)4);
      if (asString != null && asString.startsWith("web:")) {
         String remainder = asString.substring("web:".length());
         String[] tokens = COMMA_SPLITTER.split(remainder);
         return tokens;
      } else {
         return this.getStringArrayField((short)4);
      }
   }

   public void setUsernames(String[] usernames) {
      this.setField((short)4, usernames);
   }

   public void setUsernames(String usernames) {
      this.setField((short)4, usernames);
   }

   public boolean sessionRequired() {
      return true;
   }

   private FusionPacket[] inviteFusionUser(ConnectionI connection) throws Exception {
      RegistryPrx registryPrx = connection.findRegistry();
      if (registryPrx == null) {
         throw new Exception("Unable to locate registry");
      } else {
         String groupChatId = this.getGroupChatId();
         if (groupChatId == null) {
            throw new Exception("You must specify a group chat ID");
         } else {
            GroupChatPrx groupChatPrx = null;

            try {
               groupChatPrx = registryPrx.findGroupChatObject(groupChatId);
            } catch (ObjectNotFoundException var7) {
               groupChatPrx = this.restoreGroupChat(connection, groupChatId);
            }

            String invitee = this.getUsername();
            String[] invitees = this.getUsernames();
            if (invitee != null && invitees != null || invitee == null && invitees == null) {
               return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invalid combination of parameters specified")).toArray();
            } else {
               if (invitee != null) {
                  groupChatPrx.addParticipant(connection.getUsername(), invitee);
                  connection.getSessionPrx().groupChatJoined(groupChatId);
               } else {
                  groupChatPrx.addParticipants(connection.getUsername(), invitees);
                  connection.getSessionPrx().groupChatJoinedMultiple(groupChatId, invitees.length);
               }

               return (new FusionPktOk(this.transactionId)).toArray();
            }
         }
      }
   }

   private GroupChatPrx restoreGroupChat(ConnectionI connection, String groupChatId) throws Exception {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.ENABLED)) {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktGroupChatInvite: not restoring group chatID=" + groupChatId + " as persistent group chats disabled");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("FusionPktGroupChatInvite: restoring group chatID=" + groupChatId);
         }

         try {
            return connection.getSessionPrx().findGroupChatObject(groupChatId);
         } catch (Exception var4) {
            if (log.isDebugEnabled()) {
               log.debug("FusionPktGroupChatInvite: failed to restore group chatID=" + this.getGroupChatId() + " exception=" + var4);
            }

            throw new GroupChatNoLongerInChatSyncException(groupChatId);
         }
      }
   }

   private FusionPacket[] inviteOtherIMUser(ConnectionI connection, ImType imType) throws Exception {
      String conferenceId = this.getGroupChatId();
      if (conferenceId == null) {
         throw new Exception("You must specify a conference ID");
      } else {
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new Exception("You are no longer logged in");
         } else {
            userPrx.otherIMInviteToConference(imType.value(), conferenceId, this.getUsername());
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
            return this.inviteFusionUser(connection);
         case MSN:
         case YAHOO:
            return this.inviteOtherIMUser(connection, imType);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (ObjectNotFoundException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to invite user to group chat - Unable to find group chat object")).toArray();
      } catch (FusionException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to invite user to group chat - " + var5.message)).toArray();
      } catch (LocalException var6) {
         return (new FusionPktInternalServerError(this.transactionId, var6, "Failed to invite user to group chat")).toArray();
      } catch (Exception var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to invite user to group chat - " + var7.getMessage())).toArray();
      }
   }
}
