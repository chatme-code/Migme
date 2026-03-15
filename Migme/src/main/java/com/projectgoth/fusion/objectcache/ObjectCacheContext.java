package com.projectgoth.fusion.objectcache;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import com.projectgoth.fusion.slice.MessageLoggerPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionCachePrx;

public interface ObjectCacheContext {
   ObjectCacheAdminPrx getAdminPrx();

   ObjectAdapter getCacheAdapter();

   Communicator getCommunicator();

   MessageLoggerPrx getMessageLoggerPrx();

   MogileFSManager getMogileFSManager();

   ObjectCacheInterface getObjectCache();

   Properties getProperties();

   RegistryPrx getRegistryPrx();

   SessionCachePrx getSessionCachePrx();

   String getUniqueID();
}
