package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RegistryNodePrx extends ObjectPrx {
   void registerUserObject(String var1, UserPrx var2, String var3) throws ObjectExistsException;

   void registerUserObject(String var1, UserPrx var2, String var3, Map<String, String> var4) throws ObjectExistsException;

   void deregisterUserObject(String var1, String var2);

   void deregisterUserObject(String var1, String var2, Map<String, String> var3);

   void registerConnectionObject(String var1, ConnectionPrx var2) throws ObjectExistsException;

   void registerConnectionObject(String var1, ConnectionPrx var2, Map<String, String> var3) throws ObjectExistsException;

   void deregisterConnectionObject(String var1);

   void deregisterConnectionObject(String var1, Map<String, String> var2);

   void registerChatRoomObject(String var1, ChatRoomPrx var2) throws ObjectExistsException;

   void registerChatRoomObject(String var1, ChatRoomPrx var2, Map<String, String> var3) throws ObjectExistsException;

   void deregisterChatRoomObject(String var1);

   void deregisterChatRoomObject(String var1, Map<String, String> var2);

   void registerGroupChatObject(String var1, GroupChatPrx var2);

   void registerGroupChatObject(String var1, GroupChatPrx var2, Map<String, String> var3);

   void deregisterGroupChatObject(String var1);

   void deregisterGroupChatObject(String var1, Map<String, String> var2);

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3);

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3, Map<String, String> var4);

   void deregisterObjectCache(String var1);

   void deregisterObjectCache(String var1, Map<String, String> var2);

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4);

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4, Map<String, String> var5);

   void deregisterBotService(String var1);

   void deregisterBotService(String var1, Map<String, String> var2);

   String registerNewNode(RegistryNodePrx var1, String var2, boolean var3) throws FusionException;

   String registerNewNode(RegistryNodePrx var1, String var2, boolean var3, Map<String, String> var4) throws FusionException;

   void registerObjectCacheStats(String var1, ObjectCacheStats var2) throws ObjectNotFoundException;

   void registerObjectCacheStats(String var1, ObjectCacheStats var2, Map<String, String> var3) throws ObjectNotFoundException;

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3);

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3, Map<String, String> var4);

   void deregisterMessageSwitchboard(String var1);

   void deregisterMessageSwitchboard(String var1, Map<String, String> var2);
}
