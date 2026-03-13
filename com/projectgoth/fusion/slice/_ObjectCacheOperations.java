package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _ObjectCacheOperations {
   void createUserObject_async(AMD_ObjectCache_createUserObject var1, String var2, Current var3) throws FusionException, ObjectExistsException;

   UserPrx createUserObjectNonAsync(String var1, Current var2) throws FusionException, ObjectExistsException;

   ChatRoomPrx createChatRoomObject(String var1, Current var2) throws FusionException, ObjectExistsException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4, Current var5) throws FusionException, ObjectExistsException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Current var4) throws FusionException;

   GroupChatPrx[] getAllGroupChats(Current var1) throws FusionException;

   void purgeUserObject(String var1, Current var2);

   void purgeGroupChatObject(String var1, Current var2);

   MessageSwitchboardPrx getMessageSwitchboard(Current var1) throws FusionException;
}
