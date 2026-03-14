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
import java.util.Map;

public abstract class _RegistryDisp extends ObjectImpl implements Registry {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::Registry"};
   private static final String[] __all = new String[]{"deregisterBotService", "deregisterChatRoomObject", "deregisterConnectionObject", "deregisterGroupChatObject", "deregisterMessageSwitchboard", "deregisterObjectCache", "deregisterUserObject", "findChatRoomObject", "findChatRoomObjects", "findConnectionObject", "findGroupChatObject", "findUserObject", "findUserObjects", "findUserObjectsMap", "getLowestLoadedBotService", "getLowestLoadedObjectCache", "getMessageSwitchboard", "getUserCount", "ice_id", "ice_ids", "ice_isA", "ice_ping", "newGatewayID", "registerBotService", "registerChatRoomObject", "registerConnectionObject", "registerGroupChatObject", "registerMessageSwitchboard", "registerObjectCache", "registerObjectCacheStats", "registerUserObject", "sendAlertMessageToAllUsers"};

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

   public final void deregisterBotService(String hostName) {
      this.deregisterBotService(hostName, (Current)null);
   }

   public final void deregisterChatRoomObject(String name) {
      this.deregisterChatRoomObject(name, (Current)null);
   }

   public final void deregisterConnectionObject(String sessionID) {
      this.deregisterConnectionObject(sessionID, (Current)null);
   }

   public final void deregisterGroupChatObject(String id) {
      this.deregisterGroupChatObject(id, (Current)null);
   }

   public final void deregisterMessageSwitchboard(String hostName) {
      this.deregisterMessageSwitchboard(hostName, (Current)null);
   }

   public final void deregisterObjectCache(String hostName) {
      this.deregisterObjectCache(hostName, (Current)null);
   }

   public final void deregisterUserObject(String username, String objectCacheHostname) {
      this.deregisterUserObject(username, objectCacheHostname, (Current)null);
   }

   public final ChatRoomPrx findChatRoomObject(String name) throws ObjectNotFoundException {
      return this.findChatRoomObject(name, (Current)null);
   }

   public final ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames) {
      return this.findChatRoomObjects(chatRoomNames, (Current)null);
   }

   public final ConnectionPrx findConnectionObject(String sessionID) throws ObjectNotFoundException {
      return this.findConnectionObject(sessionID, (Current)null);
   }

   public final GroupChatPrx findGroupChatObject(String id) throws ObjectNotFoundException {
      return this.findGroupChatObject(id, (Current)null);
   }

   public final UserPrx findUserObject(String username) throws ObjectNotFoundException {
      return this.findUserObject(username, (Current)null);
   }

   public final UserPrx[] findUserObjects(String[] usernames) {
      return this.findUserObjects(usernames, (Current)null);
   }

   public final Map<String, UserPrx> findUserObjectsMap(String[] usernames) {
      return this.findUserObjectsMap(usernames, (Current)null);
   }

   public final BotServicePrx getLowestLoadedBotService() throws ObjectNotFoundException {
      return this.getLowestLoadedBotService((Current)null);
   }

   public final ObjectCachePrx getLowestLoadedObjectCache() throws ObjectNotFoundException {
      return this.getLowestLoadedObjectCache((Current)null);
   }

   public final void getMessageSwitchboard_async(AMD_Registry_getMessageSwitchboard __cb) throws FusionException {
      this.getMessageSwitchboard_async(__cb, (Current)null);
   }

   public final int getUserCount() {
      return this.getUserCount((Current)null);
   }

   public final int newGatewayID() {
      return this.newGatewayID((Current)null);
   }

   public final void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy) {
      this.registerBotService(hostName, load, serviceProxy, adminProxy, (Current)null);
   }

   public final void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy) throws ObjectExistsException {
      this.registerChatRoomObject(name, chatRoomProxy, (Current)null);
   }

   public final void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy) throws ObjectExistsException {
      this.registerConnectionObject(sessionID, connectionProxy, (Current)null);
   }

   public final void registerGroupChatObject(String id, GroupChatPrx groupChatProxy) {
      this.registerGroupChatObject(id, groupChatProxy, (Current)null);
   }

   public final void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy) {
      this.registerMessageSwitchboard(hostName, msbProxy, adminProxy, (Current)null);
   }

   public final void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy) {
      this.registerObjectCache(hostName, cacheProxy, adminProxy, (Current)null);
   }

   public final void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats) throws ObjectNotFoundException {
      this.registerObjectCacheStats(objectCacheHostName, stats, (Current)null);
   }

   public final void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname) throws ObjectExistsException {
      this.registerUserObject(username, userProxy, objectCacheHostname, (Current)null);
   }

   public final void sendAlertMessageToAllUsers(String message, String title, short timeout) throws FusionException {
      this.sendAlertMessageToAllUsers(message, title, timeout, (Current)null);
   }

   public static DispatchStatus ___findUserObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         UserPrx __ret = __obj.findUserObject(username, __current);
         UserPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___findUserObjects(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] usernames = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      UserPrx[] __ret = __obj.findUserObjects(usernames, __current);
      UserProxyArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___findUserObjectsMap(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] usernames = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      Map<String, UserPrx> __ret = __obj.findUserObjectsMap(usernames, __current);
      UsernameToProxyMapHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___registerUserObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      UserPrx userProxy = UserPrxHelper.__read(__is);
      String objectCacheHostname = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.registerUserObject(username, userProxy, objectCacheHostname, __current);
         return DispatchStatus.DispatchOK;
      } catch (ObjectExistsException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___deregisterUserObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String objectCacheHostname = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterUserObject(username, objectCacheHostname, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___findConnectionObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sessionID = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         ConnectionPrx __ret = __obj.findConnectionObject(sessionID, __current);
         ConnectionPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___registerConnectionObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sessionID = __is.readString();
      ConnectionPrx connectionProxy = ConnectionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.registerConnectionObject(sessionID, connectionProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (ObjectExistsException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___deregisterConnectionObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sessionID = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterConnectionObject(sessionID, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___findChatRoomObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String name = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         ChatRoomPrx __ret = __obj.findChatRoomObject(name, __current);
         ChatRoomPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___findChatRoomObjects(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] chatRoomNames = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      ChatRoomPrx[] __ret = __obj.findChatRoomObjects(chatRoomNames, __current);
      ChatRoomProxyArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___registerChatRoomObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String name = __is.readString();
      ChatRoomPrx chatRoomProxy = ChatRoomPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.registerChatRoomObject(name, chatRoomProxy, __current);
         return DispatchStatus.DispatchOK;
      } catch (ObjectExistsException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___deregisterChatRoomObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String name = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterChatRoomObject(name, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___findGroupChatObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String id = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         GroupChatPrx __ret = __obj.findGroupChatObject(id, __current);
         GroupChatPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___registerGroupChatObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String id = __is.readString();
      GroupChatPrx groupChatProxy = GroupChatPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.registerGroupChatObject(id, groupChatProxy, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___deregisterGroupChatObject(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String id = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterGroupChatObject(id, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getLowestLoadedObjectCache(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         ObjectCachePrx __ret = __obj.getLowestLoadedObjectCache(__current);
         ObjectCachePrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___registerObjectCache(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      ObjectCachePrx cacheProxy = ObjectCachePrxHelper.__read(__is);
      ObjectCacheAdminPrx adminProxy = ObjectCacheAdminPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.registerObjectCache(hostName, cacheProxy, adminProxy, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___deregisterObjectCache(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterObjectCache(hostName, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getLowestLoadedBotService(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         BotServicePrx __ret = __obj.getLowestLoadedBotService(__current);
         BotServicePrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___registerBotService(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      int load = __is.readInt();
      BotServicePrx serviceProxy = BotServicePrxHelper.__read(__is);
      BotServiceAdminPrx adminProxy = BotServiceAdminPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.registerBotService(hostName, load, serviceProxy, adminProxy, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___deregisterBotService(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterBotService(hostName, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___sendAlertMessageToAllUsers(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String title = __is.readString();
      short timeout = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendAlertMessageToAllUsers(message, title, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___newGatewayID(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.newGatewayID(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___registerObjectCacheStats(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String objectCacheHostName = __is.readString();
      ObjectCacheStatsHolder stats = new ObjectCacheStatsHolder();
      __is.readObject(stats.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.registerObjectCacheStats(objectCacheHostName, stats.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (ObjectNotFoundException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUserCount(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getUserCount(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___registerMessageSwitchboard(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      MessageSwitchboardPrx msbProxy = MessageSwitchboardPrxHelper.__read(__is);
      MessageSwitchboardAdminPrx adminProxy = MessageSwitchboardAdminPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.registerMessageSwitchboard(hostName, msbProxy, adminProxy, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___deregisterMessageSwitchboard(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String hostName = __is.readString();
      __is.endReadEncaps();
      __obj.deregisterMessageSwitchboard(hostName, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getMessageSwitchboard(Registry __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      _AMD_Registry_getMessageSwitchboard __cb = new _AMD_Registry_getMessageSwitchboard(__inS);

      try {
         __obj.getMessageSwitchboard_async(__cb, __current);
      } catch (Exception var5) {
         __cb.ice_exception(var5);
      }

      return DispatchStatus.DispatchAsync;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___deregisterBotService(this, in, __current);
         case 1:
            return ___deregisterChatRoomObject(this, in, __current);
         case 2:
            return ___deregisterConnectionObject(this, in, __current);
         case 3:
            return ___deregisterGroupChatObject(this, in, __current);
         case 4:
            return ___deregisterMessageSwitchboard(this, in, __current);
         case 5:
            return ___deregisterObjectCache(this, in, __current);
         case 6:
            return ___deregisterUserObject(this, in, __current);
         case 7:
            return ___findChatRoomObject(this, in, __current);
         case 8:
            return ___findChatRoomObjects(this, in, __current);
         case 9:
            return ___findConnectionObject(this, in, __current);
         case 10:
            return ___findGroupChatObject(this, in, __current);
         case 11:
            return ___findUserObject(this, in, __current);
         case 12:
            return ___findUserObjects(this, in, __current);
         case 13:
            return ___findUserObjectsMap(this, in, __current);
         case 14:
            return ___getLowestLoadedBotService(this, in, __current);
         case 15:
            return ___getLowestLoadedObjectCache(this, in, __current);
         case 16:
            return ___getMessageSwitchboard(this, in, __current);
         case 17:
            return ___getUserCount(this, in, __current);
         case 18:
            return ___ice_id(this, in, __current);
         case 19:
            return ___ice_ids(this, in, __current);
         case 20:
            return ___ice_isA(this, in, __current);
         case 21:
            return ___ice_ping(this, in, __current);
         case 22:
            return ___newGatewayID(this, in, __current);
         case 23:
            return ___registerBotService(this, in, __current);
         case 24:
            return ___registerChatRoomObject(this, in, __current);
         case 25:
            return ___registerConnectionObject(this, in, __current);
         case 26:
            return ___registerGroupChatObject(this, in, __current);
         case 27:
            return ___registerMessageSwitchboard(this, in, __current);
         case 28:
            return ___registerObjectCache(this, in, __current);
         case 29:
            return ___registerObjectCacheStats(this, in, __current);
         case 30:
            return ___registerUserObject(this, in, __current);
         case 31:
            return ___sendAlertMessageToAllUsers(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::Registry was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::Registry was not generated with stream support";
      throw ex;
   }
}
