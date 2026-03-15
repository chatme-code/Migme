package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ObjectCachePrx extends ObjectPrx {
   UserPrx createUserObject(String var1) throws FusionException, ObjectExistsException;

   UserPrx createUserObject(String var1, Map<String, String> var2) throws FusionException, ObjectExistsException;

   UserPrx createUserObjectNonAsync(String var1) throws FusionException, ObjectExistsException;

   UserPrx createUserObjectNonAsync(String var1, Map<String, String> var2) throws FusionException, ObjectExistsException;

   ChatRoomPrx createChatRoomObject(String var1) throws FusionException, ObjectExistsException;

   ChatRoomPrx createChatRoomObject(String var1, Map<String, String> var2) throws FusionException, ObjectExistsException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4) throws FusionException, ObjectExistsException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4, Map<String, String> var5) throws FusionException, ObjectExistsException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3) throws FusionException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Map<String, String> var4) throws FusionException;

   GroupChatPrx[] getAllGroupChats() throws FusionException;

   GroupChatPrx[] getAllGroupChats(Map<String, String> var1) throws FusionException;

   void purgeUserObject(String var1);

   void purgeUserObject(String var1, Map<String, String> var2);

   void purgeGroupChatObject(String var1);

   void purgeGroupChatObject(String var1, Map<String, String> var2);

   MessageSwitchboardPrx getMessageSwitchboard() throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws FusionException;

   boolean getMessageSwitchboard_async(AMI_ObjectCache_getMessageSwitchboard var1);

   boolean getMessageSwitchboard_async(AMI_ObjectCache_getMessageSwitchboard var1, Map<String, String> var2);
}
