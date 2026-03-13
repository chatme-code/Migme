package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;

public abstract class _MessageSwitchboardDisp extends ObjectImpl implements MessageSwitchboard {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::MessageSwitchboard"};
   private static final String[] __all = new String[]{"ensureGroupChatExists", "getAndPushMessages", "getAndPushMessages2", "getChats", "getChats2", "ice_id", "ice_ids", "ice_isA", "ice_ping", "isUserChatSyncEnabled", "onCreateGroupChat", "onCreatePrivateChat", "onGetChats", "onJoinChatRoom", "onJoinGroupChat", "onLeaveChatRoom", "onLeaveGroupChat", "onLeavePrivateChat", "onLogon", "onSendFusionMessageToChatRoom", "onSendFusionMessageToGroupChat", "onSendFusionMessageToIndividual", "onSendMessageToAllUsersInChat", "setChatName"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID) throws FusionException {
      return this.ensureGroupChatExists(currentSession, groupChatID, (Current)null);
   }

   public final void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn) throws FusionException {
      this.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, (Current)null);
   }

   public final void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId) throws FusionException {
      this.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, (Current)null);
   }

   public final ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType) throws FusionException {
      return this.getChats(userID, chatListVersion, limit, chatType, (Current)null);
   }

   public final ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn) throws FusionException {
      return this.getChats2(userID, chatListVersion, limit, chatType, cxn, (Current)null);
   }

   public final boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID) throws FusionException {
      return this.isUserChatSyncEnabled(cxn, username, userID, (Current)null);
   }

   public final void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote) throws FusionException {
      this.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, (Current)null);
   }

   public final void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture) throws FusionException {
      this.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, (Current)null);
   }

   public final void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername) throws FusionException {
      this.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, (Current)null);
   }

   public final void onJoinChatRoom(String username, int userID, String chatRoomName) throws FusionException {
      this.onJoinChatRoom(username, userID, chatRoomName, (Current)null);
   }

   public final void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy) throws FusionException {
      this.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, (Current)null);
   }

   public final void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy) throws FusionException {
      this.onLeaveChatRoom(username, userID, chatRoomName, userProxy, (Current)null);
   }

   public final void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy) throws FusionException {
      this.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, (Current)null);
   }

   public final void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion) throws FusionException {
      this.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, (Current)null);
   }

   public final void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername) throws FusionException {
      this.onLogon(userID, sess, transactionID, parentUsername, (Current)null);
   }

   public final void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion) throws FusionException {
      this.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, (Current)null);
   }

   public final void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion) throws FusionException {
      this.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, (Current)null);
   }

   public final boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture) throws FusionException {
      return this.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, (Current)null);
   }

   public final boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData) throws FusionException {
      return this.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, (Current)null);
   }

   public final void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy) throws FusionException {
      this.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, (Current)null);
   }

   public static DispatchStatus ___isUserChatSyncEnabled(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ConnectionPrx cxn = ConnectionPrxHelper.__read(__is);
      String username = __is.readString();
      int userID = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.isUserChatSyncEnabled(cxn, username, userID, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getChats(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userID = __is.readInt();
      int chatListVersion = __is.readInt();
      int limit = __is.readInt();
      byte chatType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         ChatDefinitionIce[] __ret = __obj.getChats(userID, chatListVersion, limit, chatType, __current);
         ChatDefinitionIceArrayHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getChats2(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userID = __is.readInt();
      int chatListVersion = __is.readInt();
      int limit = __is.readInt();
      byte chatType = __is.readByte();
      ConnectionPrx cxn = ConnectionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         ChatDefinitionIce[] __ret = __obj.getChats2(userID, chatListVersion, limit, chatType, cxn, __current);
         ChatDefinitionIceArrayHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onGetChats(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ConnectionPrx cxn = ConnectionPrxHelper.__read(__is);
      int userID = __is.readInt();
      int chatListVersion = __is.readInt();
      int limit = __is.readInt();
      byte chatType = __is.readByte();
      short transactionId = __is.readShort();
      String parentUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var13) {
         __os.writeUserException(var13);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getAndPushMessages(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      byte chatType = __is.readByte();
      String suppliedChatID = __is.readString();
      long oldestMessageTimestamp = __is.readLong();
      long newestMessageTimestamp = __is.readLong();
      int limit = __is.readInt();
      ConnectionPrx cxn = ConnectionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var15) {
         __os.writeUserException(var15);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getAndPushMessages2(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      byte chatType = __is.readByte();
      String suppliedChatID = __is.readString();
      long oldestMessageTimestamp = __is.readLong();
      long newestMessageTimestamp = __is.readLong();
      int limit = __is.readInt();
      ConnectionPrx cxn = ConnectionPrxHelper.__read(__is);
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      short fusionPktTransactionId = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var18) {
         __os.writeUserException(var18);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onCreateGroupChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ChatDefinitionIce storedGroupChat = new ChatDefinitionIce();
      storedGroupChat.__read(__is);
      String creatorUsername = __is.readString();
      String privateChatPartnerUsername = __is.readString();
      GroupChatPrx groupChatRemote = GroupChatPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onJoinGroupChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int userID = __is.readInt();
      String groupChatGUID = __is.readString();
      boolean debug = __is.readBool();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onLeaveGroupChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int userID = __is.readInt();
      String groupChatGUID = __is.readString();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onJoinChatRoom(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int userID = __is.readInt();
      String chatRoomName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onJoinChatRoom(username, userID, chatRoomName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onLeaveChatRoom(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int userID = __is.readInt();
      String chatRoomName = __is.readString();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onLeaveChatRoom(username, userID, chatRoomName, userProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onSendFusionMessageToIndividual(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      SessionPrx currentSession = SessionPrxHelper.__read(__is);
      UserPrx parentUser = UserPrxHelper.__read(__is);
      MessageDataIce messageData = new MessageDataIce();
      messageData.__read(__is);
      String destinationUsername = __is.readString();
      String[] uniqueUsersPrivateChattedWith = StringArrayHelper.read(__is);
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      UserDataIce senderUserData = new UserDataIce();
      senderUserData.__read(__is);
      String recipientDisplayPicture = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var15) {
         __os.writeUserException(var15);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onSendFusionMessageToGroupChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      SessionPrx currentSession = SessionPrxHelper.__read(__is);
      UserPrx parentUser = UserPrxHelper.__read(__is);
      MessageDataIce messageData = new MessageDataIce();
      messageData.__read(__is);
      String groupChatID = __is.readString();
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var12) {
         __os.writeUserException(var12);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onSendFusionMessageToChatRoom(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      SessionPrx currentSession = SessionPrxHelper.__read(__is);
      UserPrx parentUser = UserPrxHelper.__read(__is);
      MessageDataIce messageData = new MessageDataIce();
      messageData.__read(__is);
      String chatRoomName = __is.readString();
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var12) {
         __os.writeUserException(var12);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onSendMessageToAllUsersInChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      SessionPrx currentSession = SessionPrxHelper.__read(__is);
      UserPrx parentUser = UserPrxHelper.__read(__is);
      MessageDataIce messageData = new MessageDataIce();
      messageData.__read(__is);
      UserDataIce senderUserData = new UserDataIce();
      senderUserData.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onCreatePrivateChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userID = __is.readInt();
      String username = __is.readString();
      String otherUser = __is.readString();
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      UserDataIce senderUserData = new UserDataIce();
      senderUserData.__read(__is);
      String recipientDisplayPicture = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var13) {
         __os.writeUserException(var13);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onLeavePrivateChat(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userID = __is.readInt();
      String username = __is.readString();
      String otherUser = __is.readString();
      int deviceType = __is.readInt();
      short clientVersion = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___ensureGroupChatExists(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      SessionPrx currentSession = SessionPrxHelper.__read(__is);
      String groupChatID = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         GroupChatPrx __ret = __obj.ensureGroupChatExists(currentSession, groupChatID, __current);
         GroupChatPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___onLogon(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userID = __is.readInt();
      SessionPrx sess = SessionPrxHelper.__read(__is);
      short transactionID = __is.readShort();
      String parentUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.onLogon(userID, sess, transactionID, parentUsername, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___setChatName(MessageSwitchboard __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String parentUsername = __is.readString();
      String suppliedChatID = __is.readString();
      byte chatType = __is.readByte();
      String chatName = __is.readString();
      RegistryPrx regy = RegistryPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___ensureGroupChatExists(this, in, __current);
         case 1:
            return ___getAndPushMessages(this, in, __current);
         case 2:
            return ___getAndPushMessages2(this, in, __current);
         case 3:
            return ___getChats(this, in, __current);
         case 4:
            return ___getChats2(this, in, __current);
         case 5:
            return ___ice_id(this, in, __current);
         case 6:
            return ___ice_ids(this, in, __current);
         case 7:
            return ___ice_isA(this, in, __current);
         case 8:
            return ___ice_ping(this, in, __current);
         case 9:
            return ___isUserChatSyncEnabled(this, in, __current);
         case 10:
            return ___onCreateGroupChat(this, in, __current);
         case 11:
            return ___onCreatePrivateChat(this, in, __current);
         case 12:
            return ___onGetChats(this, in, __current);
         case 13:
            return ___onJoinChatRoom(this, in, __current);
         case 14:
            return ___onJoinGroupChat(this, in, __current);
         case 15:
            return ___onLeaveChatRoom(this, in, __current);
         case 16:
            return ___onLeaveGroupChat(this, in, __current);
         case 17:
            return ___onLeavePrivateChat(this, in, __current);
         case 18:
            return ___onLogon(this, in, __current);
         case 19:
            return ___onSendFusionMessageToChatRoom(this, in, __current);
         case 20:
            return ___onSendFusionMessageToGroupChat(this, in, __current);
         case 21:
            return ___onSendFusionMessageToIndividual(this, in, __current);
         case 22:
            return ___onSendMessageToAllUsersInChat(this, in, __current);
         case 23:
            return ___setChatName(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::MessageSwitchboard was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::MessageSwitchboard was not generated with stream support";
      throw ex;
   }
}
