package com.projectgoth.fusion.objectcache;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.Properties;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FusionApplicationContext;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import com.projectgoth.fusion.slice.MessageLoggerPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionCachePrx;
import java.lang.ref.WeakReference;
import org.apache.log4j.Logger;

public class ObjectCacheContextBuilder extends FusionApplicationContext implements ObjectCacheContext {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ObjectCacheContextBuilder.class));
   private Properties properties;
   private Communicator communicator;
   private WeakReference<ObjectAdapter> cacheAdapter;
   private WeakReference<MessageLoggerPrx> messageLoggerPrx;
   private WeakReference<ObjectCacheInterface> objectCache;
   private WeakReference<ObjectCacheAdminPrx> adminPrx;
   private WeakReference<RegistryPrx> registryPrx;
   private String uniqueID;
   private WeakReference<SessionCachePrx> sessionCachePrx;
   private WeakReference<MogileFSManager> mogileFSManager;

   public ObjectCacheAdminPrx getAdminPrx() {
      return (ObjectCacheAdminPrx)this.extractProperty(this.adminPrx);
   }

   public ObjectAdapter getCacheAdapter() {
      return (ObjectAdapter)this.extractProperty(this.cacheAdapter);
   }

   public Communicator getCommunicator() {
      return this.communicator;
   }

   public MessageLoggerPrx getMessageLoggerPrx() {
      return (MessageLoggerPrx)this.extractProperty(this.messageLoggerPrx);
   }

   public MogileFSManager getMogileFSManager() {
      return (MogileFSManager)this.extractProperty(this.mogileFSManager);
   }

   public ObjectCacheInterface getObjectCache() {
      return (ObjectCacheInterface)this.extractProperty(this.objectCache);
   }

   public Properties getProperties() {
      return this.properties;
   }

   public RegistryPrx getRegistryPrx() {
      return (RegistryPrx)this.extractProperty(this.registryPrx);
   }

   public SessionCachePrx getSessionCachePrx() {
      return (SessionCachePrx)this.extractProperty(this.sessionCachePrx);
   }

   public String getUniqueID() {
      return this.uniqueID;
   }

   public ObjectCacheContextBuilder setAdminPrx(ObjectCacheAdminPrx adminPrx) {
      this.adminPrx = new WeakReference(adminPrx);
      return this;
   }

   public ObjectCacheContextBuilder setCacheAdapter(ObjectAdapter cacheAdapter) {
      this.cacheAdapter = new WeakReference(cacheAdapter);
      return this;
   }

   public ObjectCacheContextBuilder setCommunicator(Communicator communicator) {
      this.communicator = communicator;
      return this;
   }

   public ObjectCacheContextBuilder setMessageLoggerPrx(MessageLoggerPrx messageLoggerPrx) {
      this.messageLoggerPrx = new WeakReference(messageLoggerPrx);
      return this;
   }

   public ObjectCacheContextBuilder setMogileFSManager(MogileFSManager mogileFSManager) {
      this.mogileFSManager = new WeakReference(mogileFSManager);
      return this;
   }

   public ObjectCacheContextBuilder setObjectCache(ObjectCacheInterface objectCache) {
      this.objectCache = new WeakReference(objectCache);
      return this;
   }

   public ObjectCacheContextBuilder setProperties(Properties properties) {
      this.properties = properties;
      return this;
   }

   public ObjectCacheContextBuilder setRegistryPrx(RegistryPrx registryPrx) {
      this.registryPrx = new WeakReference(registryPrx);
      return this;
   }

   public ObjectCacheContextBuilder setSessionCachePrx(SessionCachePrx sessionCachePrx) {
      this.sessionCachePrx = new WeakReference(sessionCachePrx);
      return this;
   }

   public ObjectCacheContextBuilder setUniqueID(String uniqueID) {
      this.uniqueID = uniqueID;
      return this;
   }
}
