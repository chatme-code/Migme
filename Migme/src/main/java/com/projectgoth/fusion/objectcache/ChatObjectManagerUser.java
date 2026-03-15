package com.projectgoth.fusion.objectcache;

import Ice.Properties;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionCachePrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;

public interface ChatObjectManagerUser {
   ChatSession createSession(ChatUser var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8, String var9, String var10, String var11, short var12, String var13, ConnectionPrx var14);

   RequestCounter getRequestCounter();

   Properties getProperties();

   IcePrxFinder getIcePrxFinder();

   void removeSession(String var1, boolean var2);

   SessionPrx onSessionCreated(ChatSession var1);

   void onSessionRemoved(ChatUser var1);

   void removeUser(String var1);

   UserPrx findUserPrx(String var1) throws FusionException;

   UserPrx makeUserPrx(String var1);

   GroupChatPrx findGroupChatPrx(String var1) throws FusionException;

   SessionPrx findSessionPrx(String var1);

   SessionPrx[] findSessionsPrx(String[] var1);

   UserPrx findUserPrxFromRegistry(String var1) throws ObjectNotFoundException;

   void logMessage(MessageToLog.TypeEnum var1, int var2, String var3, String var4, int var5, String var6);

   RegistryPrx getRegistryPrx();

   boolean isLogMessagesToFile();

   Credential[] getUserCredentials(int var1, byte[] var2) throws FusionException;

   ChatContentStore getFileStore();

   SessionCachePrx getSessionCachePrx();

   ObjectCacheContext getApplicationContext();
}
