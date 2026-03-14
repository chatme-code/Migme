package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _RegistryDel extends _ObjectDel {
   UserPrx findUserObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, ObjectNotFoundException;

   UserPrx[] findUserObjects(String[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   Map<String, UserPrx> findUserObjectsMap(String[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void registerUserObject(String var1, UserPrx var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, ObjectExistsException;

   void deregisterUserObject(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   ConnectionPrx findConnectionObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, ObjectNotFoundException;

   void registerConnectionObject(String var1, ConnectionPrx var2, Map<String, String> var3) throws LocalExceptionWrapper, ObjectExistsException;

   void deregisterConnectionObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   ChatRoomPrx findChatRoomObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, ObjectNotFoundException;

   ChatRoomPrx[] findChatRoomObjects(String[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void registerChatRoomObject(String var1, ChatRoomPrx var2, Map<String, String> var3) throws LocalExceptionWrapper, ObjectExistsException;

   void deregisterChatRoomObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   GroupChatPrx findGroupChatObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, ObjectNotFoundException;

   void registerGroupChatObject(String var1, GroupChatPrx var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void deregisterGroupChatObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   ObjectCachePrx getLowestLoadedObjectCache(Map<String, String> var1) throws LocalExceptionWrapper, ObjectNotFoundException;

   void registerObjectCache(String var1, ObjectCachePrx var2, ObjectCacheAdminPrx var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void deregisterObjectCache(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   BotServicePrx getLowestLoadedBotService(Map<String, String> var1) throws LocalExceptionWrapper, ObjectNotFoundException;

   void registerBotService(String var1, int var2, BotServicePrx var3, BotServiceAdminPrx var4, Map<String, String> var5) throws LocalExceptionWrapper;

   void deregisterBotService(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   int newGatewayID(Map<String, String> var1) throws LocalExceptionWrapper;

   void registerObjectCacheStats(String var1, ObjectCacheStats var2, Map<String, String> var3) throws LocalExceptionWrapper, ObjectNotFoundException;

   int getUserCount(Map<String, String> var1) throws LocalExceptionWrapper;

   void registerMessageSwitchboard(String var1, MessageSwitchboardPrx var2, MessageSwitchboardAdminPrx var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void deregisterMessageSwitchboard(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
