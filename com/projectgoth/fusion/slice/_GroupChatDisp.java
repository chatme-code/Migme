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

public abstract class _GroupChatDisp extends ObjectImpl implements GroupChat {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BotChannel", "::com::projectgoth::fusion::slice::GroupChat"};
   private static final String[] __all = new String[]{"addParticipant", "addParticipantInner", "addParticipants", "addUserToGroupChatDebug", "botKilled", "executeEmoteCommandWithState", "getCreatorUserID", "getCreatorUsername", "getId", "getNumParticipants", "getParticipantUserIDs", "getParticipants", "getPrivateChatPartnerUserID", "ice_id", "ice_ids", "ice_isA", "ice_ping", "isParticipant", "listOfParticipants", "putBotMessage", "putBotMessageToAllUsers", "putBotMessageToUsers", "putFileReceived", "putMessage", "removeParticipant", "sendGamesHelpToUser", "sendInitialMessages", "sendMessageToBots", "startBot", "stopAllBots", "stopBot", "supportsBinaryMessage"};

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
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public final void botKilled(String botInstanceID) throws FusionException {
      this.botKilled(botInstanceID, (Current)null);
   }

   public final String[] getParticipants(String requestingUsername) {
      return this.getParticipants(requestingUsername, (Current)null);
   }

   public final boolean isParticipant(String username) throws FusionException {
      return this.isParticipant(username, (Current)null);
   }

   public final void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      this.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, (Current)null);
   }

   public final void sendGamesHelpToUser(String username) throws FusionException {
      this.sendGamesHelpToUser(username, (Current)null);
   }

   public final void sendMessageToBots(String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBots(username, message, receivedTimestamp, (Current)null);
   }

   public final void startBot(String username, String botCommandName) throws FusionException {
      this.startBot(username, botCommandName, (Current)null);
   }

   public final void stopAllBots(String username, int timeout) throws FusionException {
      this.stopAllBots(username, timeout, (Current)null);
   }

   public final void stopBot(String username, String botCommandName) throws FusionException {
      this.stopBot(username, botCommandName, (Current)null);
   }

   public final void addParticipant(String inviterUsername, String inviteeUsername) throws FusionException {
      this.addParticipant(inviterUsername, inviteeUsername, (Current)null);
   }

   public final void addParticipantInner(String inviterUsername, String inviteeUsername, boolean debug) throws FusionException {
      this.addParticipantInner(inviterUsername, inviteeUsername, debug, (Current)null);
   }

   public final void addParticipants(String inviterUsername, String[] inviteeUsernames) throws FusionException {
      this.addParticipants(inviterUsername, inviteeUsernames, (Current)null);
   }

   public final void addUserToGroupChatDebug(String participant, boolean b, boolean c) throws FusionException {
      this.addUserToGroupChatDebug(participant, b, c, (Current)null);
   }

   public final int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (Current)null);
   }

   public final int getCreatorUserID() {
      return this.getCreatorUserID((Current)null);
   }

   public final String getCreatorUsername() {
      return this.getCreatorUsername((Current)null);
   }

   public final String getId() {
      return this.getId((Current)null);
   }

   public final int getNumParticipants() {
      return this.getNumParticipants((Current)null);
   }

   public final int[] getParticipantUserIDs() {
      return this.getParticipantUserIDs((Current)null);
   }

   public final int getPrivateChatPartnerUserID() {
      return this.getPrivateChatPartnerUserID((Current)null);
   }

   public final String listOfParticipants() {
      return this.listOfParticipants((Current)null);
   }

   public final void putFileReceived(MessageDataIce message) throws FusionException {
      this.putFileReceived(message, (Current)null);
   }

   public final void putMessage(MessageDataIce message) throws FusionException {
      this.putMessage(message, (Current)null);
   }

   public final boolean removeParticipant(String username) throws FusionException {
      return this.removeParticipant(username, (Current)null);
   }

   public final void sendInitialMessages() {
      this.sendInitialMessages((Current)null);
   }

   public final boolean supportsBinaryMessage(String usernameToExclude) {
      return this.supportsBinaryMessage(usernameToExclude, (Current)null);
   }

   public static DispatchStatus ___addParticipantInner(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String inviterUsername = __is.readString();
      String inviteeUsername = __is.readString();
      boolean debug = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addParticipantInner(inviterUsername, inviteeUsername, debug, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addParticipant(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String inviterUsername = __is.readString();
      String inviteeUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addParticipant(inviterUsername, inviteeUsername, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeParticipant(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.removeParticipant(username, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putMessage(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessage(message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putFileReceived(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putFileReceived(message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendInitialMessages(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.sendInitialMessages(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getNumParticipants(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getNumParticipants(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___supportsBinaryMessage(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String usernameToExclude = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.supportsBinaryMessage(usernameToExclude, __current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___executeEmoteCommandWithState(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String emoteCommand = __is.readString();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      SessionPrx sessionProxy = SessionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getId(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getId(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getCreatorUsername(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getCreatorUsername(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getCreatorUserID(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getCreatorUserID(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getPrivateChatPartnerUserID(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getPrivateChatPartnerUserID(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___listOfParticipants(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.listOfParticipants(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getParticipantUserIDs(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int[] __ret = __obj.getParticipantUserIDs(__current);
      IntArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addParticipants(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String inviterUsername = __is.readString();
      String[] inviteeUsernames = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addParticipants(inviterUsername, inviteeUsernames, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addUserToGroupChatDebug(GroupChat __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String participant = __is.readString();
      boolean b = __is.readBool();
      boolean c = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addUserToGroupChatDebug(participant, b, c, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
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
            return ___addParticipant(this, in, __current);
         case 1:
            return ___addParticipantInner(this, in, __current);
         case 2:
            return ___addParticipants(this, in, __current);
         case 3:
            return ___addUserToGroupChatDebug(this, in, __current);
         case 4:
            return _BotChannelDisp.___botKilled(this, in, __current);
         case 5:
            return ___executeEmoteCommandWithState(this, in, __current);
         case 6:
            return ___getCreatorUserID(this, in, __current);
         case 7:
            return ___getCreatorUsername(this, in, __current);
         case 8:
            return ___getId(this, in, __current);
         case 9:
            return ___getNumParticipants(this, in, __current);
         case 10:
            return ___getParticipantUserIDs(this, in, __current);
         case 11:
            return _BotChannelDisp.___getParticipants(this, in, __current);
         case 12:
            return ___getPrivateChatPartnerUserID(this, in, __current);
         case 13:
            return ___ice_id(this, in, __current);
         case 14:
            return ___ice_ids(this, in, __current);
         case 15:
            return ___ice_isA(this, in, __current);
         case 16:
            return ___ice_ping(this, in, __current);
         case 17:
            return _BotChannelDisp.___isParticipant(this, in, __current);
         case 18:
            return ___listOfParticipants(this, in, __current);
         case 19:
            return _BotChannelDisp.___putBotMessage(this, in, __current);
         case 20:
            return _BotChannelDisp.___putBotMessageToAllUsers(this, in, __current);
         case 21:
            return _BotChannelDisp.___putBotMessageToUsers(this, in, __current);
         case 22:
            return ___putFileReceived(this, in, __current);
         case 23:
            return ___putMessage(this, in, __current);
         case 24:
            return ___removeParticipant(this, in, __current);
         case 25:
            return _BotChannelDisp.___sendGamesHelpToUser(this, in, __current);
         case 26:
            return ___sendInitialMessages(this, in, __current);
         case 27:
            return _BotChannelDisp.___sendMessageToBots(this, in, __current);
         case 28:
            return _BotChannelDisp.___startBot(this, in, __current);
         case 29:
            return _BotChannelDisp.___stopAllBots(this, in, __current);
         case 30:
            return _BotChannelDisp.___stopBot(this, in, __current);
         case 31:
            return ___supportsBinaryMessage(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::GroupChat was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GroupChat was not generated with stream support";
      throw ex;
   }
}
