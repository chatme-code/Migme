package com.projectgoth.fusion.slice;

import Ice.Current;
import java.util.Map;

public interface _RegistryOperations {
   UserPrx findUserObject(String var1, Current var2) throws ObjectNotFoundException;

   UserPrx[] findUserObjects(String[] var1, Current var2);

   Map<String, UserPrx> findUserObjectsMap(String[] var1, Current var2);

   void registerUserObject(String var1, UserPrx var2, String var3, Current var4) throws ObjectExistsException;

   void deregisterUserObject(String var1, String var2, Current var3);

   ConnectionPrx findConnectionObject(String var1, Current var2) throws ObjectNotFoundException;

   void registerConnectionObject(String var1, ConnectionPrx var2, Current var3) throws ObjectExistsException;

   void deregisterConnectionObject(String var1, Current var2);

   ChatRoomPrx findChatRoomObject(String var1, Current var2) throws ObjectNotFoundException;

   ChatRoomPrx[] findChatRoomObjects(String[] var1, Current var2);

   void registerChatRoomObject(String var1, ChatRoomPrx var2, Current var3) throws ObjectExistsException;

   void deregisterChatRoomObject(String var1, Current var2);

   GroupChatPrx findGroupChatObject(String var1, Current var2) throws ObjectNotFoundException;

   void registerGroupChatObject(String var1, GroupChatPrx var2, Current var3);

   void deregisterGroupChatObject(String var1, Current var2);

   ObjectCachePrx getLowestLoadedObjectCache(Current var1) throws ObjectNotFoundException;

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3, Current var4);

   void deregisterObjectCache(String var1, Current var2);

   BotServicePrx getLowestLoadedBotService(Current var1) throws ObjectNotFoundException;

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4, Current var5);

   void deregisterBotService(String var1, Current var2);

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Current var4) throws FusionException;

   int newGatewayID(Current var1);

   void registerObjectCacheStats(String var1, ObjectCacheStats var2, Current var3) throws ObjectNotFoundException;

   int getUserCount(Current var1);

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3, Current var4);

   void deregisterMessageSwitchboard(String var1, Current var2);

   void getMessageSwitchboard_async(AMD_Registry_getMessageSwitchboard var1, Current var2) throws FusionException;
}
