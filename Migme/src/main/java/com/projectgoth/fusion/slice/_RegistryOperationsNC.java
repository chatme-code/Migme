package com.projectgoth.fusion.slice;

import java.util.Map;

public interface _RegistryOperationsNC {
   UserPrx findUserObject(String var1) throws ObjectNotFoundException;

   UserPrx[] findUserObjects(String[] var1);

   Map<String, UserPrx> findUserObjectsMap(String[] var1);

   void registerUserObject(String var1, UserPrx var2, String var3) throws ObjectExistsException;

   void deregisterUserObject(String var1, String var2);

   ConnectionPrx findConnectionObject(String var1) throws ObjectNotFoundException;

   void registerConnectionObject(String var1, ConnectionPrx var2) throws ObjectExistsException;

   void deregisterConnectionObject(String var1);

   ChatRoomPrx findChatRoomObject(String var1) throws ObjectNotFoundException;

   ChatRoomPrx[] findChatRoomObjects(String[] var1);

   void registerChatRoomObject(String var1, ChatRoomPrx var2) throws ObjectExistsException;

   void deregisterChatRoomObject(String var1);

   GroupChatPrx findGroupChatObject(String var1) throws ObjectNotFoundException;

   void registerGroupChatObject(String var1, GroupChatPrx var2);

   void deregisterGroupChatObject(String var1);

   ObjectCachePrx getLowestLoadedObjectCache() throws ObjectNotFoundException;

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3);

   void deregisterObjectCache(String var1);

   BotServicePrx getLowestLoadedBotService() throws ObjectNotFoundException;

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4);

   void deregisterBotService(String var1);

   void sendAlertMessageToAllUsers(String var1, String var2, short var3) throws FusionException;

   int newGatewayID();

   void registerObjectCacheStats(String var1, ObjectCacheStats var2) throws ObjectNotFoundException;

   int getUserCount();

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3);

   void deregisterMessageSwitchboard(String var1);

   void getMessageSwitchboard_async(AMD_Registry_getMessageSwitchboard var1) throws FusionException;
}
