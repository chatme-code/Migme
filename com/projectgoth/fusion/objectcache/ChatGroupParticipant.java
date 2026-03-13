package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.sql.Connection;
import org.apache.log4j.Logger;

public class ChatGroupParticipant extends ChatParticipant {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatGroup.class));
   private int userID;
   private UserData userData;
   private UserPrx userProxy;
   private MerchantDetailsData merchantDetailsData;
   private boolean initallyOffline = false;

   public ChatGroupParticipant(String username) throws FusionException {
      super(username);
      this.initallyOffline = true;

      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         this.userID = userEJB.getUserID(username, (Connection)null);
      } catch (Exception var3) {
         throw new FusionException("Unable to create InitiallyOfflineParticipant: " + var3);
      }
   }

   public ChatGroupParticipant(UserData userData, UserPrx userProxy) {
      super(userData.username);
      this.userData = userData;
      this.userProxy = userProxy;
      this.userID = userData.userID;
   }

   public int getUserID() {
      return this.userID;
   }

   public boolean isInitallyOffline() {
      return this.initallyOffline;
   }

   private UserPrx getUserProxy() {
      return this.userProxy;
   }

   private UserData getUserData() {
      return this.userData;
   }

   public boolean hasUserProxy() {
      return this.userProxy != null;
   }

   public boolean isAdmin() {
      return this.userData.chatRoomAdmin;
   }

   private boolean isTopMerchant() {
      return this.userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT;
   }

   public int getCountryID(ChatObjectManagerGroup objectManager) {
      this.updateLiveness(objectManager);
      return this.userData != null ? this.userData.countryID : -1;
   }

   public boolean updateLiveness(ChatObjectManagerGroup objectManager) {
      if (this.userProxy != null) {
         try {
            this.userProxy.ice_ping();
            return true;
         } catch (Exception var4) {
            if (log.isDebugEnabled()) {
               log.debug("Participant " + this.getUsername() + ": proxy failed ping, trying to reload");
            }
         }
      }

      try {
         this.userProxy = objectManager.findUserPrx(this.getUsername());
         UserDataIce userDataIce = this.userProxy.getUserData();
         this.userData = new UserData(userDataIce);
         return true;
      } catch (Exception var3) {
         this.userProxy = null;
         return false;
      }
   }

   public void loadMerchantDetailsData() throws FusionException {
      if (this.isTopMerchant()) {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            this.setMerchantDetails(userEJB.getBasicMerchantDetails(this.getUsername()));
         } catch (Exception var2) {
            throw new FusionException(var2.getMessage());
         }
      }

   }

   private MerchantDetailsData getMerchantDetailsData() {
      return this.merchantDetailsData;
   }

   private void setMerchantDetails(MerchantDetailsData data) {
      this.merchantDetailsData = data;
   }

   public void applyMessageColor(MessageDataIce messageIce) {
      if (this.isTopMerchant()) {
         MerchantDetailsData merchantDetailsData = this.getMerchantDetailsData();
         if (merchantDetailsData != null) {
            if (log.isDebugEnabled()) {
               log.debug("Username [" + this.getUsername() + "] Color: " + merchantDetailsData.usernameColorType.name());
            }

            messageIce.sourceColour = merchantDetailsData.getChatColorHex();
         } else {
            if (log.isDebugEnabled()) {
               log.debug("Top Merchant [" + this.getUsername() + "] has no merchantdetails attached. Using default color " + MerchantDetailsData.UserNameColorTypeEnum.DEFAULT.name());
            }

            messageIce.sourceColour = MerchantDetailsData.UserNameColorTypeEnum.DEFAULT.hex();
         }
      } else if (this.isAdmin()) {
         messageIce.sourceColour = MessageData.SourceTypeEnum.GLOBAL_ADMIN.colorHex();
      }

   }

   public boolean isAnyoneOnBlockList(String[] usernames) throws FusionException {
      try {
         return this.getUserProxy().getBlockListFromUsernames(usernames).length > 0;
      } catch (Exception var3) {
         if (log.isDebugEnabled()) {
            log.debug("isAnyoneOnBlockList for user " + this.getUsername() + ": exception=" + var3 + ", trying via EJB");
         }

         return this.isAnyoneOnBlockList_ParticipantOffline(usernames);
      }
   }

   private boolean isAnyoneOnBlockList_ParticipantOffline(String[] usernames) throws FusionException {
      try {
         Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
         String[] arr$ = usernames;
         int len$ = usernames.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String u = arr$[i$];
            if (contactEJB.isBlocking(this.getUsername(), u)) {
               return true;
            }
         }

         return false;
      } catch (Exception var7) {
         throw new FusionException(var7.getMessage());
      }
   }

   public void putPrivateChatNowGroupChat(String id, String creator) throws FusionException {
      this.getUserProxy().privateChatNowAGroupChat(id, creator);
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      this.getUserProxy().putMessage(message);
   }

   public void putAlertMessage(String messageText) throws FusionException {
      this.getUserProxy().putAlertMessage(messageText, "Group Chat", (short)5);
   }

   public void putFileReceived(MessageDataIce message) throws FusionException {
      this.getUserProxy().putFileReceived(message);
   }

   public void leavingGroupChat() {
      this.getUserProxy().leavingGroupChat();
   }

   public String[] getBlockListFromUsernames(String[] participantArray) {
      return this.getUserProxy().getBlockListFromUsernames(participantArray);
   }

   public void sendGroupChatParticipantArrays(String id, ImType imType, String[] participants, String[] mutedUsers) {
      SessionPrx[] sessions = this.getUserProxy().getSessions();
      SessionPrx[] arr$ = sessions;
      int len$ = sessions.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SessionPrx session = arr$[i$];

         try {
            session.sendGroupChatParticipantArrays(id, imType.value(), participants, mutedUsers);
         } catch (Exception var11) {
            if (log.isDebugEnabled()) {
               log.debug("Failed to sendGroupChatParticipants to:" + this.getUsername(), var11);
            }
         }
      }

   }

   public void sendGroupChatParticipants(String id, ImType imType, String participants, String mutedUsers) {
      SessionPrx[] sessions = this.getUserProxy().getSessions();
      SessionPrx[] arr$ = sessions;
      int len$ = sessions.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SessionPrx session = arr$[i$];

         try {
            session.sendGroupChatParticipants(id, imType.value(), participants, mutedUsers);
         } catch (Exception var11) {
            if (log.isDebugEnabled()) {
               log.debug("Failed to sendGroupChatParticipants to:" + this.getUsername(), var11);
            }
         }
      }

   }

   public boolean isAuthenticated(AuthenticatedAccessControlTypeEnum accessControlType) {
      return AuthenticatedAccessControl.hasAccess(accessControlType, this.getUserData());
   }

   public void chatSyncNotifyUserLeft(ChatObjectManagerGroup objectManager, String chatRoomId) {
      try {
         this.updateLiveness(objectManager);
         MessageSwitchboardDispatcher.getInstance().onLeaveGroupChat(objectManager.getRegistryPrx(), this.getUsername(), this.getUserID(), chatRoomId, this.getUserProxy());
      } catch (Exception var4) {
         log.warn("Exception in MessageSwitchboardDispatcher.onLeaveGroupChat for user=" + this.getUsername() + ": e=" + var4, var4);
      }

   }

   public void chatSyncNotifyUserJoined(ChatObjectManagerGroup objectManager, String chatRoomId, boolean debug) {
      try {
         MessageSwitchboardDispatcher.getInstance().onJoinGroupChat(objectManager.getRegistryPrx(), this.getUsername(), this.getUserID(), chatRoomId, debug, this.getUserProxy());
      } catch (Exception var5) {
         log.warn("Unable to add participant=" + this.getUsername() + " to stored group chat=" + chatRoomId + " in chatsync");
      }

   }

   public void notifyUserJoinedGroupChat(String chatRoomId, String username) {
      boolean isBlocked = this.getUserProxy().isOnBlockList(this.getUsername());
      this.getUserProxy().notifyUserJoinedGroupChat(chatRoomId, username, isBlocked);
   }

   public void notifyUserLeftGroupChat(String chatRoomId, String username) {
      this.getUserProxy().notifyUserLeftGroupChat(chatRoomId, username);
   }

   public boolean supportsBinaryMessage() {
      return this.getUserProxy().supportsBinaryMessage();
   }
}
