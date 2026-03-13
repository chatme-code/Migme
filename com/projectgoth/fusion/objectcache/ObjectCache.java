package com.projectgoth.fusion.objectcache;

import Ice.Application;
import Ice.Communicator;
import Ice.LocalException;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.datagrid.DataGridFactory;
import com.projectgoth.fusion.ice.IceThreadMonitor;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import com.projectgoth.fusion.slice.MessageLoggerPrx;
import com.projectgoth.fusion.slice.MessageLoggerPrxHelper;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrxHelper;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.ObjectCachePrxHelper;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.slice.SessionCachePrx;
import com.projectgoth.fusion.slice.SessionCachePrxHelper;
import com.projectgoth.fusion.stats.FusionStatsIceDispatchInterceptor;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ObjectCache extends Application {
   private static final String APP_NAME = "ObjectCache";
   private static final String CONFIG_FILE = "ObjectCache.cfg";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ObjectCache.class));
   private static ObjectAdapter cacheAdapter;
   private static RegistryPrx registryPrx;
   private static MessageSwitchboardPrx messageSwitchboardPrx;
   private static SessionCachePrx sessionCachePrx;
   private static MessageLoggerPrx messageLoggerPrx;
   private static ObjectCachePrx objectCachePrx;
   private static Communicator communicator;
   private static Properties properties;
   private static ObjectCacheInterface objectCache;
   public static String hostName;
   public static long startTime = System.currentTimeMillis();
   public static boolean logMessagesToDB;
   public static boolean logMessagesToFile;
   private static MogileFSManager mogileFSManager;
   public static final int BOT_MESSAGE_COLOUR = 34734;
   public static final int GIFT_ALL_MESSAGE_COLOUR = 16711935;
   public static final int GIFT_ALL_MESSAGE_COLOUR_LOW_PRICE = 0;
   public static final String GIFT_ALL_EMOTE_HOTKEY = "(shower)";
   public static final int CHATROOM_ANNOUNCE_MESSAGE_COLOUR = 7798784;
   public static final String CHATROOM_ANNOUNCE_EMOTE_HOTKEY = "(announce)";
   public static final int CHATROOM_BROADCAST_MESSAGE_COLOUR = 7798784;
   private static String uniqueID;
   private static IceThreadMonitor iceThreadMonitor;
   private ObjectCacheAdminPrx adminPrx;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("ObjectCache version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      ObjectCache app = new ObjectCache();
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("ObjectCache", args, args[0]);
      } else {
         status = app.main("ObjectCache", args, "ObjectCache.cfg");
      }

      System.exit(status);
   }

   public int run(String[] arg0) {
      ObjectCacheContextBuilder ctx = new ObjectCacheContextBuilder();
      communicator = communicator();
      properties = communicator().getProperties();
      ctx.setProperties(properties);
      ctx.setCommunicator(communicator);

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var13) {
         hostName = "UNKNOWN";
      }

      logMessagesToDB = communicator().getProperties().getPropertyAsIntWithDefault("LogMessagesToDB", 1) == 1;
      logMessagesToFile = communicator().getProperties().getPropertyAsIntWithDefault("LogMessagesToFile", 0) == 1;

      String registryStringifiedProxy;
      try {
         registryStringifiedProxy = communicator().getProperties().getProperty("MogileFSDomain");
         String[] trackers = communicator().getProperties().getProperty("MogileFSTrackers").split("[,;]");
         int connectionsPerTracker = communicator().getProperties().getPropertyAsIntWithDefault("MogileFSConnectionsPerTracker", 1);
         mogileFSManager = new MogileFSManager(registryStringifiedProxy, trackers, connectionsPerTracker, log);
         ctx.setMogileFSManager(mogileFSManager);
      } catch (Exception var12) {
         log.fatal("Object Cache " + hostName + ": Failed to initialize MogileFS. Exception: " + var12.toString());
         return 1;
      }

      registryStringifiedProxy = communicator().getProperties().getProperty("RegistryProxy");
      ObjectPrx basePrx = communicator().stringToProxy(registryStringifiedProxy);
      log.info("Connecting to [" + basePrx + "]");

      try {
         registryPrx = RegistryPrxHelper.checkedCast(basePrx);
         ctx.setRegistryPrx(registryPrx);
      } catch (LocalException var11) {
         log.fatal("Object Cache " + hostName + ": Connection to [" + registryPrx + "] failed. ", var11);
         return 1;
      }

      if (registryPrx == null) {
         log.fatal("Object Cache " + hostName + ": Connection to [" + registryPrx + "] failed");
         return 1;
      } else {
         String sessionCacheStringifiedProxy = communicator().getProperties().getProperty("SessionCacheProxy");
         log.info("Connecting to a SessionCache [" + sessionCacheStringifiedProxy + "]");
         basePrx = communicator().stringToProxy(sessionCacheStringifiedProxy);

         try {
            sessionCachePrx = SessionCachePrxHelper.uncheckedCast(basePrx.ice_oneway());
            sessionCachePrx = (SessionCachePrx)sessionCachePrx.ice_connectionId("OneWayProxyGroup");
            ctx.setSessionCachePrx(sessionCachePrx);
         } catch (LocalException var10) {
            log.fatal("Object Cache " + hostName + ": Connection to a SessionCache failed.", var10);
            return 1;
         }

         if (sessionCachePrx == null) {
            log.fatal("Object Cache " + hostName + ": Connection to a SessionCache failed, the session cache proxy is null.");
            return 1;
         } else {
            if (logMessagesToFile) {
               String messageLoggerStringifiedProxy = communicator().getProperties().getProperty("MessageLoggerProxy");
               log.info("Connecting to a MessageLogger");
               basePrx = communicator().stringToProxy(messageLoggerStringifiedProxy);

               try {
                  messageLoggerPrx = MessageLoggerPrxHelper.uncheckedCast(basePrx.ice_oneway());
                  messageLoggerPrx = (MessageLoggerPrx)messageLoggerPrx.ice_connectionId("OneWayProxyGroup");
                  ctx.setMessageLoggerPrx(messageLoggerPrx);
               } catch (LocalException var9) {
                  log.fatal("Object Cache " + hostName + ": Connection to a MessageLogger failed. Exception: " + var9.toString());
                  return 1;
               }

               if (messageLoggerPrx == null) {
                  log.fatal("Object Cache " + hostName + ": Connection to a MessageLogger failed");
                  return 1;
               }
            }

            log.debug("Initialising ObjectCacheAdmin interface");
            ObjectAdapter adminAdapter = communicator().createObjectAdapter("ObjectCacheAdminAdapter");
            ObjectCacheAdminI admin = new ObjectCacheAdminI(ctx);
            basePrx = adminAdapter.add(admin, Util.stringToIdentity("ObjectCacheAdmin"));
            this.adminPrx = ObjectCacheAdminPrxHelper.uncheckedCast(basePrx);
            ctx.setAdminPrx(this.adminPrx);
            adminAdapter.activate();
            log.debug("Initialising ObjectCache interface");
            cacheAdapter = communicator().createObjectAdapter("ObjectCacheAdapter");
            ctx.setCacheAdapter(cacheAdapter);
            objectCache = new ObjectCacheRpcI(ctx);
            ctx.setObjectCache(objectCache);
            IceStats.getInstance().setIceObjects(communicator(), cacheAdapter, ObjectCacheIceAmdInvoker.getExecutor());
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IceThreadMonitorSettings.OBJC_TPOOL_MONITOR_STARTUP_ENABLED)) {
               iceThreadMonitor = new IceThreadMonitor(cacheAdapter, SystemPropertyEntities.IceThreadMonitorSettings.OBJC_TPOOL_MONITOR_THREAD_COUNT, SystemPropertyEntities.IceThreadMonitorSettings.OBJC_TPOOL_MONITOR_MINIMUM_INTER_DUMP_DURATION);
               iceThreadMonitor.start(60);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.ICE_CONNECTION_STATS)) {
               log.info("Enabling FusionStatsIceDispatchInterceptor for ObjectCache itself");
               FusionStatsIceDispatchInterceptor interceptor = new FusionStatsIceDispatchInterceptor(objectCache);
               basePrx = cacheAdapter.add(interceptor, Util.stringToIdentity("ObjectCache"));
            } else {
               basePrx = cacheAdapter.add(objectCache, Util.stringToIdentity("ObjectCache"));
            }

            objectCachePrx = ObjectCachePrxHelper.uncheckedCast(basePrx);
            cacheAdapter.activate();
            uniqueID = objectCachePrx.ice_getEndpoints()[0].toString();
            ctx.setUniqueID(uniqueID);
            ctx.build();
            registryPrx.registerObjectCache(uniqueID, objectCachePrx, this.adminPrx);

            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DataGridSettings.ENABLED)) {
                  log.info("Object cache: starting data grid");
                  DataGridFactory.getInstance().getGrid().prepare();
               }
            } catch (Exception var14) {
               log.fatal("Object Cache " + hostName + ": Failed to initialize data grid. Exception: " + var14.toString());
               return 1;
            }

            admin.startTimer();
            log.info("Service started");
            communicator().waitForShutdown();
            if (interrupted()) {
               log.fatal("Application was interrupted, shutting down");
            } else {
               log.info("Application is shutting down");
            }

            return 0;
         }
      }
   }
}
