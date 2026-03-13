package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class MessageSwitchboardPrxHelper extends ObjectPrxHelperBase implements MessageSwitchboardPrx {
   public GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID) throws FusionException {
      return this.ensureGroupChatExists(currentSession, groupChatID, (Map)null, false);
   }

   public GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID, Map<String, String> __ctx) throws FusionException {
      return this.ensureGroupChatExists(currentSession, groupChatID, __ctx, true);
   }

   private GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("ensureGroupChatExists");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.ensureGroupChatExists(currentSession, groupChatID, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn) throws FusionException {
      this.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, (Map)null, false);
   }

   public void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, Map<String, String> __ctx) throws FusionException {
      this.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, __ctx, true);
   }

   private void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAndPushMessages");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, __ctx);
            return;
         } catch (LocalExceptionWrapper var15) {
            this.__handleExceptionWrapper(__delBase, var15, (OutgoingAsync)null);
         } catch (LocalException var16) {
            __cnt = this.__handleException(__delBase, var16, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId) throws FusionException {
      this.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, (Map)null, false);
   }

   public void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId, Map<String, String> __ctx) throws FusionException {
      this.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, __ctx, true);
   }

   private void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAndPushMessages2");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, __ctx);
            return;
         } catch (LocalExceptionWrapper var18) {
            this.__handleExceptionWrapper(__delBase, var18, (OutgoingAsync)null);
         } catch (LocalException var19) {
            __cnt = this.__handleException(__delBase, var19, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType) throws FusionException {
      return this.getChats(userID, chatListVersion, limit, chatType, (Map)null, false);
   }

   public ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType, Map<String, String> __ctx) throws FusionException {
      return this.getChats(userID, chatListVersion, limit, chatType, __ctx, true);
   }

   private ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getChats");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.getChats(userID, chatListVersion, limit, chatType, __ctx);
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn) throws FusionException {
      return this.getChats2(userID, chatListVersion, limit, chatType, cxn, (Map)null, false);
   }

   public ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn, Map<String, String> __ctx) throws FusionException {
      return this.getChats2(userID, chatListVersion, limit, chatType, cxn, __ctx, true);
   }

   private ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getChats2");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.getChats2(userID, chatListVersion, limit, chatType, cxn, __ctx);
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID) throws FusionException {
      return this.isUserChatSyncEnabled(cxn, username, userID, (Map)null, false);
   }

   public boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID, Map<String, String> __ctx) throws FusionException {
      return this.isUserChatSyncEnabled(cxn, username, userID, __ctx, true);
   }

   private boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("isUserChatSyncEnabled");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.isUserChatSyncEnabled(cxn, username, userID, __ctx);
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote) throws FusionException {
      this.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, (Map)null, false);
   }

   public void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote, Map<String, String> __ctx) throws FusionException {
      this.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, __ctx, true);
   }

   private void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onCreateGroupChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture) throws FusionException {
      this.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, (Map)null, false);
   }

   public void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx) throws FusionException {
      this.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __ctx, true);
   }

   private void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onCreatePrivateChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __ctx);
            return;
         } catch (LocalExceptionWrapper var13) {
            this.__handleExceptionWrapper(__delBase, var13, (OutgoingAsync)null);
         } catch (LocalException var14) {
            __cnt = this.__handleException(__delBase, var14, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername) throws FusionException {
      this.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, (Map)null, false);
   }

   public void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername, Map<String, String> __ctx) throws FusionException {
      this.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, __ctx, true);
   }

   private void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onGetChats");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, __ctx);
            return;
         } catch (LocalExceptionWrapper var13) {
            this.__handleExceptionWrapper(__delBase, var13, (OutgoingAsync)null);
         } catch (LocalException var14) {
            __cnt = this.__handleException(__delBase, var14, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onJoinChatRoom(String username, int userID, String chatRoomName) throws FusionException {
      this.onJoinChatRoom(username, userID, chatRoomName, (Map)null, false);
   }

   public void onJoinChatRoom(String username, int userID, String chatRoomName, Map<String, String> __ctx) throws FusionException {
      this.onJoinChatRoom(username, userID, chatRoomName, __ctx, true);
   }

   private void onJoinChatRoom(String username, int userID, String chatRoomName, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onJoinChatRoom");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onJoinChatRoom(username, userID, chatRoomName, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy) throws FusionException {
      this.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, (Map)null, false);
   }

   public void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy, Map<String, String> __ctx) throws FusionException {
      this.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, __ctx, true);
   }

   private void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onJoinGroupChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy) throws FusionException {
      this.onLeaveChatRoom(username, userID, chatRoomName, userProxy, (Map)null, false);
   }

   public void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy, Map<String, String> __ctx) throws FusionException {
      this.onLeaveChatRoom(username, userID, chatRoomName, userProxy, __ctx, true);
   }

   private void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onLeaveChatRoom");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onLeaveChatRoom(username, userID, chatRoomName, userProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy) throws FusionException {
      this.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, (Map)null, false);
   }

   public void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy, Map<String, String> __ctx) throws FusionException {
      this.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, __ctx, true);
   }

   private void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onLeaveGroupChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion) throws FusionException {
      this.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, (Map)null, false);
   }

   public void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, Map<String, String> __ctx) throws FusionException {
      this.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, __ctx, true);
   }

   private void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onLeavePrivateChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername) throws FusionException {
      this.onLogon(userID, sess, transactionID, parentUsername, (Map)null, false);
   }

   public void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername, Map<String, String> __ctx) throws FusionException {
      this.onLogon(userID, sess, transactionID, parentUsername, __ctx, true);
   }

   private void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onLogon");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onLogon(userID, sess, transactionID, parentUsername, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion) throws FusionException {
      this.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, (Map)null, false);
   }

   public void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion, Map<String, String> __ctx) throws FusionException {
      this.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, __ctx, true);
   }

   private void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onSendFusionMessageToChatRoom");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, __ctx);
            return;
         } catch (LocalExceptionWrapper var12) {
            this.__handleExceptionWrapper(__delBase, var12, (OutgoingAsync)null);
         } catch (LocalException var13) {
            __cnt = this.__handleException(__delBase, var13, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion) throws FusionException {
      this.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, (Map)null, false);
   }

   public void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion, Map<String, String> __ctx) throws FusionException {
      this.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, __ctx, true);
   }

   private void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onSendFusionMessageToGroupChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, __ctx);
            return;
         } catch (LocalExceptionWrapper var12) {
            this.__handleExceptionWrapper(__delBase, var12, (OutgoingAsync)null);
         } catch (LocalException var13) {
            __cnt = this.__handleException(__delBase, var13, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture) throws FusionException {
      return this.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, (Map)null, false);
   }

   public boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx) throws FusionException {
      return this.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __ctx, true);
   }

   private boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onSendFusionMessageToIndividual");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __ctx);
         } catch (LocalExceptionWrapper var15) {
            this.__handleExceptionWrapper(__delBase, var15, (OutgoingAsync)null);
         } catch (LocalException var16) {
            __cnt = this.__handleException(__delBase, var16, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData) throws FusionException {
      return this.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, (Map)null, false);
   }

   public boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData, Map<String, String> __ctx) throws FusionException {
      return this.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, __ctx, true);
   }

   private boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("onSendMessageToAllUsersInChat");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            return __del.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, __ctx);
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy) throws FusionException {
      this.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, (Map)null, false);
   }

   public void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy, Map<String, String> __ctx) throws FusionException {
      this.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, __ctx, true);
   }

   private void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setChatName");
            __delBase = this.__getDelegate(false);
            _MessageSwitchboardDel __del = (_MessageSwitchboardDel)__delBase;
            __del.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, __ctx);
            return;
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static MessageSwitchboardPrx checkedCast(ObjectPrx __obj) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboard")) {
               MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageSwitchboardPrx)__d;
   }

   public static MessageSwitchboardPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboard", __ctx)) {
               MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageSwitchboardPrx)__d;
   }

   public static MessageSwitchboardPrx checkedCast(ObjectPrx __obj, String __facet) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboard")) {
               MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static MessageSwitchboardPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageSwitchboard", __ctx)) {
               MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static MessageSwitchboardPrx uncheckedCast(ObjectPrx __obj) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageSwitchboardPrx)__obj;
         } catch (ClassCastException var4) {
            MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (MessageSwitchboardPrx)__d;
   }

   public static MessageSwitchboardPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      MessageSwitchboardPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         MessageSwitchboardPrxHelper __h = new MessageSwitchboardPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _MessageSwitchboardDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _MessageSwitchboardDelD();
   }

   public static void __write(BasicStream __os, MessageSwitchboardPrx v) {
      __os.writeProxy(v);
   }

   public static MessageSwitchboardPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         MessageSwitchboardPrxHelper result = new MessageSwitchboardPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
