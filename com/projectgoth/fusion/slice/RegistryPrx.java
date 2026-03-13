package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RegistryPrx extends ObjectPrx {
   UserPrx findUserObject(String var1) throws ObjectNotFoundException;

   UserPrx findUserObject(String var1, Map<String, String> var2) throws ObjectNotFoundException;

   UserPrx[] findUserObjects(String[] var1);

   UserPrx[] findUserObjects(String[] var1, Map<String, String> var2);

   Map<String, UserPrx> findUserObjectsMap(String[] var1);

   Map<String, UserPrx> findUserObjectsMap(String[] var1, Map<String, String> var2);

   void registerUserObject(String var1, UserPrx var2, String var3) throws ObjectExistsException;

   void registerUserObject(String var1, UserPrx var2, String var3, Map<String, String> var4) throws ObjectExistsException;

   void deregisterUserObject(String var1, String var2);

   void deregisterUserObject(String var1, String var2, Map<String, String> var3);

   ConnectionPrx findConnectionObject(String var1) throws ObjectNotFoundException;

   ConnectionPrx findConnectionObject(String var1, Map<String, String> var2) throws ObjectNotFoundException;

   void registerConnectionObject(String var1, ConnectionPrx var2) throws ObjectExistsException;

   void registerConnectionObject(String var1, ConnectionPrx var2, Map<String, String> var3) throws ObjectExistsException;

   void deregisterConnectionObject(String var1);

   void deregisterConnectionObject(String var1, Map<String, String> var2);

   ChatRoomPrx findChatRoomObject(String var1) throws ObjectNotFoundException;

   ChatRoomPrx findChatRoomObject(String var1, Map<String, String> var2) throws ObjectNotFoundException;

   ChatRoomPrx[] findChatRoomObjects(String[] var1);

   ChatRoomPrx[] findChatRoomObjects(String[] var1, Map<String, String> var2);

   void registerChatRoomObject(String var1, ChatRoomPrx var2) throws ObjectExistsException;

   void registerChatRoomObject(String var1, ChatRoomPrx var2, Map<String, String> var3) throws ObjectExistsException;

   void deregisterChatRoomObject(String var1);

   void deregisterChatRoomObject(String var1, Map<String, String> var2);

   GroupChatPrx findGroupChatObject(String var1) throws ObjectNotFoundException;

   GroupChatPrx findGroupChatObject(String var1, Map<String, String> var2) throws ObjectNotFoundException;

   void registerGroupChatObject(String var1, GroupChatPrx var2);

   void registerGroupChatObject(String var1, GroupChatPrx var2, Map<String, String> var3);

   void deregisterGroupChatObject(String var1);

   void deregisterGroupChatObject(String var1, Map<String, String> var2);

   ObjectCachePrx getLowestLoadedObjectCache() throws ObjectNotFoundException;

   ObjectCachePrx getLowestLoadedObjectCache(Map<String, String> var1) throws ObjectNotFoundException;

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3);

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3, Map<String, String> var4);

   void deregisterObjectCache(String var1);

   void deregisterObjectCache(String var1, Map<String, String> var2);

   BotServicePrx getLowestLoadedBotService() throws ObjectNotFoundException;

   BotServicePrx getLowestLoadedBotService(Map<String, String> var1) throws ObjectNotFoundException;

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4);

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4, Map<String, String> var5);

   void deregisterBotService(String var1);

   void deregisterBotService(String var1, Map<String, String> var2);

   void sendAlertMessageToAllUsers(String var1, String var2, short var3) throws FusionException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Map<String, String> var4) throws FusionException;

   int newGatewayID();

   int newGatewayID(Map<String, String> var1);

   void registerObjectCacheStats(String var1, ObjectCacheStats var2) throws ObjectNotFoundException;

   void registerObjectCacheStats(String var1, ObjectCacheStats var2, Map<String, String> var3) throws ObjectNotFoundException;

   int getUserCount();

   int getUserCount(Map<String, String> var1);

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3);

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3, Map<String, String> var4);

   void deregisterMessageSwitchboard(String var1);

   void deregisterMessageSwitchboard(String var1, Map<String, String> var2);

   MessageSwitchboardPrx getMessageSwitchboard() throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws FusionException;
}
