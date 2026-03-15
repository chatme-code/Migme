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

public abstract class _BotServiceDisp extends ObjectImpl implements BotService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BotService"};
   private static final String[] __all = new String[]{"addBotToChannel", "ice_id", "ice_ids", "ice_isA", "ice_ping", "removeBot", "sendMessageToBot", "sendMessageToBotsInChannel", "sendNotificationToBotsInChannel"};

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

   public final BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle) throws FusionException {
      return this.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, (Current)null);
   }

   public final void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress) throws FusionException {
      this.removeBot(botInstanceID, stopEvenIfGameInProgress, (Current)null);
   }

   public final void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, (Current)null);
   }

   public final void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp) throws FusionException {
      this.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, (Current)null);
   }

   public final void sendNotificationToBotsInChannel(String channelID, String username, int notification) throws FusionException {
      this.sendNotificationToBotsInChannel(channelID, username, notification, (Current)null);
   }

   public static DispatchStatus ___addBotToChannel(BotService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      BotChannelPrx channelProxy = BotChannelPrxHelper.__read(__is);
      String botCommandName = __is.readString();
      String starterUsername = __is.readString();
      boolean purgeIfIdle = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         BotInstance __ret = __obj.addBotToChannel(channelProxy, botCommandName, starterUsername, purgeIfIdle, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeBot(BotService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      boolean stopEvenIfGameInProgress = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.removeBot(botInstanceID, stopEvenIfGameInProgress, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendMessageToBot(BotService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String botInstanceID = __is.readString();
      String username = __is.readString();
      String message = __is.readString();
      long receivedTimestamp = __is.readLong();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendMessageToBot(botInstanceID, username, message, receivedTimestamp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendMessageToBotsInChannel(BotService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String channelID = __is.readString();
      String username = __is.readString();
      String message = __is.readString();
      long receivedTimestamp = __is.readLong();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendMessageToBotsInChannel(channelID, username, message, receivedTimestamp, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendNotificationToBotsInChannel(BotService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String channelID = __is.readString();
      String username = __is.readString();
      int notification = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendNotificationToBotsInChannel(channelID, username, notification, __current);
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
            return ___addBotToChannel(this, in, __current);
         case 1:
            return ___ice_id(this, in, __current);
         case 2:
            return ___ice_ids(this, in, __current);
         case 3:
            return ___ice_isA(this, in, __current);
         case 4:
            return ___ice_ping(this, in, __current);
         case 5:
            return ___removeBot(this, in, __current);
         case 6:
            return ___sendMessageToBot(this, in, __current);
         case 7:
            return ___sendMessageToBotsInChannel(this, in, __current);
         case 8:
            return ___sendNotificationToBotsInChannel(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::BotService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotService was not generated with stream support";
      throw ex;
   }
}
