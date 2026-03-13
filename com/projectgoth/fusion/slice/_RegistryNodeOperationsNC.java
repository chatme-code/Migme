package com.projectgoth.fusion.slice;

public interface _RegistryNodeOperationsNC {
   void registerUserObject(String var1, UserPrx var2, String var3) throws ObjectExistsException;

   void deregisterUserObject(String var1, String var2);

   void registerConnectionObject(String var1, ConnectionPrx var2) throws ObjectExistsException;

   void deregisterConnectionObject(String var1);

   void registerChatRoomObject(String var1, ChatRoomPrx var2) throws ObjectExistsException;

   void deregisterChatRoomObject(String var1);

   void registerGroupChatObject(String var1, GroupChatPrx var2);

   void deregisterGroupChatObject(String var1);

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3);

   void deregisterObjectCache(String var1);

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4);

   void deregisterBotService(String var1);

   String registerNewNode(RegistryNodePrx var1, String var2, boolean var3) throws FusionException;

   void registerObjectCacheStats(String var1, ObjectCacheStats var2) throws ObjectNotFoundException;

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3);

   void deregisterMessageSwitchboard(String var1);
}
