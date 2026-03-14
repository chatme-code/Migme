package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _ObjectCacheTie extends _ObjectCacheDisp implements TieBase {
   private _ObjectCacheOperations _ice_delegate;

   public _ObjectCacheTie() {
   }

   public _ObjectCacheTie(_ObjectCacheOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_ObjectCacheOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _ObjectCacheTie) ? false : this._ice_delegate.equals(((_ObjectCacheTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public ChatRoomPrx createChatRoomObject(String name, Current __current) throws FusionException, ObjectExistsException {
      return this._ice_delegate.createChatRoomObject(name, __current);
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatPartner, String[] otherPartyList, Current __current) throws FusionException, ObjectExistsException {
      return this._ice_delegate.createGroupChatObject(id, creator, privateChatPartner, otherPartyList, __current);
   }

   public void createUserObject_async(AMD_ObjectCache_createUserObject __cb, String username, Current __current) throws FusionException, ObjectExistsException {
      this._ice_delegate.createUserObject_async(__cb, username, __current);
   }

   public UserPrx createUserObjectNonAsync(String username, Current __current) throws FusionException, ObjectExistsException {
      return this._ice_delegate.createUserObjectNonAsync(username, __current);
   }

   public GroupChatPrx[] getAllGroupChats(Current __current) throws FusionException {
      return this._ice_delegate.getAllGroupChats(__current);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Current __current) throws FusionException {
      return this._ice_delegate.getMessageSwitchboard(__current);
   }

   public void purgeGroupChatObject(String id, Current __current) {
      this._ice_delegate.purgeGroupChatObject(id, __current);
   }

   public void purgeUserObject(String username, Current __current) {
      this._ice_delegate.purgeUserObject(username, __current);
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Current __current) throws FusionException {
      this._ice_delegate.sendAlertMessageToAllUsers(message, title, timeout, __current);
   }
}
