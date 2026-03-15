package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ObjectCacheDel extends _ObjectDel {
   UserPrx createUserObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException, ObjectExistsException;

   UserPrx createUserObjectNonAsync(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException, ObjectExistsException;

   ChatRoomPrx createChatRoomObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException, ObjectExistsException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException, ObjectExistsException;

   void sendAlertMessageToAllUsers(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   GroupChatPrx[] getAllGroupChats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void purgeUserObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void purgeGroupChatObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;
}
