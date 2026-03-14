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

public final class RegistryPrxHelper extends ObjectPrxHelperBase implements RegistryPrx {
   public void deregisterBotService(String hostName) {
      this.deregisterBotService(hostName, (Map)null, false);
   }

   public void deregisterBotService(String hostName, Map<String, String> __ctx) {
      this.deregisterBotService(hostName, __ctx, true);
   }

   private void deregisterBotService(String hostName, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterBotService(hostName, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterChatRoomObject(String name) {
      this.deregisterChatRoomObject(name, (Map)null, false);
   }

   public void deregisterChatRoomObject(String name, Map<String, String> __ctx) {
      this.deregisterChatRoomObject(name, __ctx, true);
   }

   private void deregisterChatRoomObject(String name, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterChatRoomObject(name, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterConnectionObject(String sessionID) {
      this.deregisterConnectionObject(sessionID, (Map)null, false);
   }

   public void deregisterConnectionObject(String sessionID, Map<String, String> __ctx) {
      this.deregisterConnectionObject(sessionID, __ctx, true);
   }

   private void deregisterConnectionObject(String sessionID, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterConnectionObject(sessionID, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterGroupChatObject(String id) {
      this.deregisterGroupChatObject(id, (Map)null, false);
   }

   public void deregisterGroupChatObject(String id, Map<String, String> __ctx) {
      this.deregisterGroupChatObject(id, __ctx, true);
   }

   private void deregisterGroupChatObject(String id, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterGroupChatObject(id, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterMessageSwitchboard(String hostName) {
      this.deregisterMessageSwitchboard(hostName, (Map)null, false);
   }

   public void deregisterMessageSwitchboard(String hostName, Map<String, String> __ctx) {
      this.deregisterMessageSwitchboard(hostName, __ctx, true);
   }

   private void deregisterMessageSwitchboard(String hostName, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterMessageSwitchboard(hostName, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterObjectCache(String hostName) {
      this.deregisterObjectCache(hostName, (Map)null, false);
   }

   public void deregisterObjectCache(String hostName, Map<String, String> __ctx) {
      this.deregisterObjectCache(hostName, __ctx, true);
   }

   private void deregisterObjectCache(String hostName, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterObjectCache(hostName, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void deregisterUserObject(String username, String objectCacheHostname) {
      this.deregisterUserObject(username, objectCacheHostname, (Map)null, false);
   }

   public void deregisterUserObject(String username, String objectCacheHostname, Map<String, String> __ctx) {
      this.deregisterUserObject(username, objectCacheHostname, __ctx, true);
   }

   private void deregisterUserObject(String username, String objectCacheHostname, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.deregisterUserObject(username, objectCacheHostname, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ChatRoomPrx findChatRoomObject(String name) throws ObjectNotFoundException {
      return this.findChatRoomObject(name, (Map)null, false);
   }

   public ChatRoomPrx findChatRoomObject(String name, Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.findChatRoomObject(name, __ctx, true);
   }

   private ChatRoomPrx findChatRoomObject(String name, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findChatRoomObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findChatRoomObject(name, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames) {
      return this.findChatRoomObjects(chatRoomNames, (Map)null, false);
   }

   public ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames, Map<String, String> __ctx) {
      return this.findChatRoomObjects(chatRoomNames, __ctx, true);
   }

   private ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findChatRoomObjects");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findChatRoomObjects(chatRoomNames, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ConnectionPrx findConnectionObject(String sessionID) throws ObjectNotFoundException {
      return this.findConnectionObject(sessionID, (Map)null, false);
   }

   public ConnectionPrx findConnectionObject(String sessionID, Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.findConnectionObject(sessionID, __ctx, true);
   }

   private ConnectionPrx findConnectionObject(String sessionID, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findConnectionObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findConnectionObject(sessionID, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public GroupChatPrx findGroupChatObject(String id) throws ObjectNotFoundException {
      return this.findGroupChatObject(id, (Map)null, false);
   }

   public GroupChatPrx findGroupChatObject(String id, Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.findGroupChatObject(id, __ctx, true);
   }

   private GroupChatPrx findGroupChatObject(String id, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findGroupChatObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findGroupChatObject(id, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserPrx findUserObject(String username) throws ObjectNotFoundException {
      return this.findUserObject(username, (Map)null, false);
   }

   public UserPrx findUserObject(String username, Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.findUserObject(username, __ctx, true);
   }

   private UserPrx findUserObject(String username, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findUserObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findUserObject(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserPrx[] findUserObjects(String[] usernames) {
      return this.findUserObjects(usernames, (Map)null, false);
   }

   public UserPrx[] findUserObjects(String[] usernames, Map<String, String> __ctx) {
      return this.findUserObjects(usernames, __ctx, true);
   }

   private UserPrx[] findUserObjects(String[] usernames, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findUserObjects");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findUserObjects(usernames, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<String, UserPrx> findUserObjectsMap(String[] usernames) {
      return this.findUserObjectsMap(usernames, (Map)null, false);
   }

   public Map<String, UserPrx> findUserObjectsMap(String[] usernames, Map<String, String> __ctx) {
      return this.findUserObjectsMap(usernames, __ctx, true);
   }

   private Map<String, UserPrx> findUserObjectsMap(String[] usernames, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findUserObjectsMap");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.findUserObjectsMap(usernames, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public BotServicePrx getLowestLoadedBotService() throws ObjectNotFoundException {
      return this.getLowestLoadedBotService((Map)null, false);
   }

   public BotServicePrx getLowestLoadedBotService(Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.getLowestLoadedBotService(__ctx, true);
   }

   private BotServicePrx getLowestLoadedBotService(Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getLowestLoadedBotService");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.getLowestLoadedBotService(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public ObjectCachePrx getLowestLoadedObjectCache() throws ObjectNotFoundException {
      return this.getLowestLoadedObjectCache((Map)null, false);
   }

   public ObjectCachePrx getLowestLoadedObjectCache(Map<String, String> __ctx) throws ObjectNotFoundException {
      return this.getLowestLoadedObjectCache(__ctx, true);
   }

   private ObjectCachePrx getLowestLoadedObjectCache(Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getLowestLoadedObjectCache");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.getLowestLoadedObjectCache(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public MessageSwitchboardPrx getMessageSwitchboard() throws FusionException {
      return this.getMessageSwitchboard((Map)null, false);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx) throws FusionException {
      return this.getMessageSwitchboard(__ctx, true);
   }

   private MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getMessageSwitchboard");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.getMessageSwitchboard(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getUserCount() {
      return this.getUserCount((Map)null, false);
   }

   public int getUserCount(Map<String, String> __ctx) {
      return this.getUserCount(__ctx, true);
   }

   private int getUserCount(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserCount");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.getUserCount(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int newGatewayID() {
      return this.newGatewayID((Map)null, false);
   }

   public int newGatewayID(Map<String, String> __ctx) {
      return this.newGatewayID(__ctx, true);
   }

   private int newGatewayID(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("newGatewayID");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            return __del.newGatewayID(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy) {
      this.registerBotService(hostName, load, serviceProxy, adminProxy, (Map)null, false);
   }

   public void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy, Map<String, String> __ctx) {
      this.registerBotService(hostName, load, serviceProxy, adminProxy, __ctx, true);
   }

   private void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerBotService(hostName, load, serviceProxy, adminProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy) throws ObjectExistsException {
      this.registerChatRoomObject(name, chatRoomProxy, (Map)null, false);
   }

   public void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy, Map<String, String> __ctx) throws ObjectExistsException {
      this.registerChatRoomObject(name, chatRoomProxy, __ctx, true);
   }

   private void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("registerChatRoomObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerChatRoomObject(name, chatRoomProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy) throws ObjectExistsException {
      this.registerConnectionObject(sessionID, connectionProxy, (Map)null, false);
   }

   public void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy, Map<String, String> __ctx) throws ObjectExistsException {
      this.registerConnectionObject(sessionID, connectionProxy, __ctx, true);
   }

   private void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("registerConnectionObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerConnectionObject(sessionID, connectionProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerGroupChatObject(String id, GroupChatPrx groupChatProxy) {
      this.registerGroupChatObject(id, groupChatProxy, (Map)null, false);
   }

   public void registerGroupChatObject(String id, GroupChatPrx groupChatProxy, Map<String, String> __ctx) {
      this.registerGroupChatObject(id, groupChatProxy, __ctx, true);
   }

   private void registerGroupChatObject(String id, GroupChatPrx groupChatProxy, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerGroupChatObject(id, groupChatProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy) {
      this.registerMessageSwitchboard(hostName, msbProxy, adminProxy, (Map)null, false);
   }

   public void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy, Map<String, String> __ctx) {
      this.registerMessageSwitchboard(hostName, msbProxy, adminProxy, __ctx, true);
   }

   private void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerMessageSwitchboard(hostName, msbProxy, adminProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy) {
      this.registerObjectCache(hostName, cacheProxy, adminProxy, (Map)null, false);
   }

   public void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy, Map<String, String> __ctx) {
      this.registerObjectCache(hostName, cacheProxy, adminProxy, __ctx, true);
   }

   private void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerObjectCache(hostName, cacheProxy, adminProxy, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats) throws ObjectNotFoundException {
      this.registerObjectCacheStats(objectCacheHostName, stats, (Map)null, false);
   }

   public void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats, Map<String, String> __ctx) throws ObjectNotFoundException {
      this.registerObjectCacheStats(objectCacheHostName, stats, __ctx, true);
   }

   private void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectNotFoundException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("registerObjectCacheStats");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerObjectCacheStats(objectCacheHostName, stats, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname) throws ObjectExistsException {
      this.registerUserObject(username, userProxy, objectCacheHostname, (Map)null, false);
   }

   public void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname, Map<String, String> __ctx) throws ObjectExistsException {
      this.registerUserObject(username, userProxy, objectCacheHostname, __ctx, true);
   }

   private void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname, Map<String, String> __ctx, boolean __explicitCtx) throws ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("registerUserObject");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.registerUserObject(username, userProxy, objectCacheHostname, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout) throws FusionException {
      this.sendAlertMessageToAllUsers(message, title, timeout, (Map)null, false);
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Map<String, String> __ctx) throws FusionException {
      this.sendAlertMessageToAllUsers(message, title, timeout, __ctx, true);
   }

   private void sendAlertMessageToAllUsers(String message, String title, short timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendAlertMessageToAllUsers");
            __delBase = this.__getDelegate(false);
            _RegistryDel __del = (_RegistryDel)__delBase;
            __del.sendAlertMessageToAllUsers(message, title, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static RegistryPrx checkedCast(ObjectPrx __obj) {
      RegistryPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Registry")) {
               RegistryPrxHelper __h = new RegistryPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RegistryPrx)__d;
   }

   public static RegistryPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      RegistryPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Registry", __ctx)) {
               RegistryPrxHelper __h = new RegistryPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (RegistryPrx)__d;
   }

   public static RegistryPrx checkedCast(ObjectPrx __obj, String __facet) {
      RegistryPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Registry")) {
               RegistryPrxHelper __h = new RegistryPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static RegistryPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      RegistryPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Registry", __ctx)) {
               RegistryPrxHelper __h = new RegistryPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static RegistryPrx uncheckedCast(ObjectPrx __obj) {
      RegistryPrx __d = null;
      if (__obj != null) {
         try {
            __d = (RegistryPrx)__obj;
         } catch (ClassCastException var4) {
            RegistryPrxHelper __h = new RegistryPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (RegistryPrx)__d;
   }

   public static RegistryPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      RegistryPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         RegistryPrxHelper __h = new RegistryPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _RegistryDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _RegistryDelD();
   }

   public static void __write(BasicStream __os, RegistryPrx v) {
      __os.writeProxy(v);
   }

   public static RegistryPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         RegistryPrxHelper result = new RegistryPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
