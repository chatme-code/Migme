package com.projectgoth.fusion.slice;

public interface _ObjectCacheOperationsNC {
   void createUserObject_async(AMD_ObjectCache_createUserObject var1, String var2) throws FusionException, ObjectExistsException;

   UserPrx createUserObjectNonAsync(String var1) throws FusionException, ObjectExistsException;

   ChatRoomPrx createChatRoomObject(String var1) throws FusionException, ObjectExistsException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4) throws FusionException, ObjectExistsException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3) throws FusionException;

   GroupChatPrx[] getAllGroupChats() throws FusionException;

   void purgeUserObject(String var1);

   void purgeGroupChatObject(String var1);

   MessageSwitchboardPrx getMessageSwitchboard() throws FusionException;
}
