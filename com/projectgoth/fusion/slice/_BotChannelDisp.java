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

public abstract class _BotChannelDisp extends ObjectImpl implements BotChannel {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BotChannel"};
   private static final String[] __all = new String[]{"botKilled", "getParticipants", "ice_id", "ice_ids", "ice_isA", "ice_ping", "isParticipant", "putBotMessage", "putBotMessageToAllUsers", "putBotMessageToUsers", "sendGamesHelpToUser", "sendMessageToBots", "startBot", "stopAllBots", "stopBot"};

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

   public static DispatchStatus ___startBot(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String botCommandName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.startBot(username, botCommandName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___stopBot(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String botCommandName = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.stopBot(username, botCommandName, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___stopAllBots(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int timeout = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.stopAllBots(username, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___botKilled(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.botKilled(botInstanceID, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendMessageToBots(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String message = __is.readString();
      long receivedTimestamp = __is.readLong();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendMessageToBots(username, message, receivedTimestamp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putBotMessage(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      String username = __is.readString();
      String message = __is.readString();
      String[] emoticonHotKeys = StringArrayHelper.read(__is);
      boolean displayPopUp = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putBotMessageToUsers(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      String[] usernames = StringArrayHelper.read(__is);
      String message = __is.readString();
      String[] emoticonHotKeys = StringArrayHelper.read(__is);
      boolean displayPopUp = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putBotMessageToUsers(botInstanceID, usernames, message, emoticonHotKeys, displayPopUp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putBotMessageToAllUsers(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      String message = __is.readString();
      String[] emoticonHotKeys = StringArrayHelper.read(__is);
      boolean displayPopUp = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putBotMessageToAllUsers(botInstanceID, message, emoticonHotKeys, displayPopUp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendGamesHelpToUser(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendGamesHelpToUser(username, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___isParticipant(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.isParticipant(username, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getParticipants(BotChannel __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getParticipants(requestingUsername, __current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___botKilled(this, in, __current);
         case 1:
            return ___getParticipants(this, in, __current);
         case 2:
            return ___ice_id(this, in, __current);
         case 3:
            return ___ice_ids(this, in, __current);
         case 4:
            return ___ice_isA(this, in, __current);
         case 5:
            return ___ice_ping(this, in, __current);
         case 6:
            return ___isParticipant(this, in, __current);
         case 7:
            return ___putBotMessage(this, in, __current);
         case 8:
            return ___putBotMessageToAllUsers(this, in, __current);
         case 9:
            return ___putBotMessageToUsers(this, in, __current);
         case 10:
            return ___sendGamesHelpToUser(this, in, __current);
         case 11:
            return ___sendMessageToBots(this, in, __current);
         case 12:
            return ___startBot(this, in, __current);
         case 13:
            return ___stopAllBots(this, in, __current);
         case 14:
            return ___stopBot(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::BotChannel was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotChannel was not generated with stream support";
      throw ex;
   }
}
