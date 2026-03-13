package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _RegistryNodeTie extends _RegistryNodeDisp implements TieBase {
   private _RegistryNodeOperations _ice_delegate;

   public _RegistryNodeTie() {
   }

   public _RegistryNodeTie(_RegistryNodeOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_RegistryNodeOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _RegistryNodeTie) ? false : this._ice_delegate.equals(((_RegistryNodeTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void deregisterBotService(String hostName, Current __current) {
      this._ice_delegate.deregisterBotService(hostName, __current);
   }

   public void deregisterChatRoomObject(String name, Current __current) {
      this._ice_delegate.deregisterChatRoomObject(name, __current);
   }

   public void deregisterConnectionObject(String sessionID, Current __current) {
      this._ice_delegate.deregisterConnectionObject(sessionID, __current);
   }

   public void deregisterGroupChatObject(String id, Current __current) {
      this._ice_delegate.deregisterGroupChatObject(id, __current);
   }

   public void deregisterMessageSwitchboard(String hostName, Current __current) {
      this._ice_delegate.deregisterMessageSwitchboard(hostName, __current);
   }

   public void deregisterObjectCache(String hostName, Current __current) {
      this._ice_delegate.deregisterObjectCache(hostName, __current);
   }

   public void deregisterUserObject(String username, String objectCacheHostname, Current __current) {
      this._ice_delegate.deregisterUserObject(username, objectCacheHostname, __current);
   }

   public void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy, Current __current) {
      this._ice_delegate.registerBotService(hostName, load, serviceProxy, adminProxy, __current);
   }

   public void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy, Current __current) throws ObjectExistsException {
      this._ice_delegate.registerChatRoomObject(name, chatRoomProxy, __current);
   }

   public void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy, Current __current) throws ObjectExistsException {
      this._ice_delegate.registerConnectionObject(sessionID, connectionProxy, __current);
   }

   public void registerGroupChatObject(String id, GroupChatPrx groupChatProxy, Current __current) {
      this._ice_delegate.registerGroupChatObject(id, groupChatProxy, __current);
   }

   public void registerMessageSwitchboard(String hostName, MessageSwitchboardPrx cacheProxy, MessageSwitchboardAdminPrx adminProxy, Current __current) {
      this._ice_delegate.registerMessageSwitchboard(hostName, cacheProxy, adminProxy, __current);
   }

   public String registerNewNode(RegistryNodePrx newNodeProxy, String hostName, boolean replicate, Current __current) throws FusionException {
      return this._ice_delegate.registerNewNode(newNodeProxy, hostName, replicate, __current);
   }

   public void registerObjectCache(String hostName, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy, Current __current) {
      this._ice_delegate.registerObjectCache(hostName, cacheProxy, adminProxy, __current);
   }

   public void registerObjectCacheStats(String objectCacheHostName, ObjectCacheStats stats, Current __current) throws ObjectNotFoundException {
      this._ice_delegate.registerObjectCacheStats(objectCacheHostName, stats, __current);
   }

   public void registerUserObject(String username, UserPrx userProxy, String objectCacheHostname, Current __current) throws ObjectExistsException {
      this._ice_delegate.registerUserObject(username, userProxy, objectCacheHostname, __current);
   }
}
