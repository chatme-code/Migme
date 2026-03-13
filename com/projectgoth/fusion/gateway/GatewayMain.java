package com.projectgoth.fusion.gateway;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ice.IceThreadMonitor;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.stats.IceStats;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class GatewayMain extends Application {
   protected static final String APP_NAME = "Gateway";
   protected static final String CONFIG_FILE = "Gateway.cfg";
   static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Gateway.class));
   Map<Gateway.ThreadPoolName, InstrumentedThreadPool> pools = new EnumMap(Gateway.ThreadPoolName.class);
   Selector selector;
   private PurgeConnectionTask purger;
   Gateway.ServerType serverType;
   private ObjectAdapter connectionAdaptor;
   private ObjectAdapter adminAdaptor;
   private RegistryPrx registryPrx;
   private SamplingTask sampler;
   private MogileFSManager mogileFSManager;
   private CaptchaService captchaService;
   private MemCachedClient captchaMemcache;
   int timeoutInterval;
   private Gateway gatewayObject;
   private GatewayContextBuilder gatewayContext;
   private IcePrxFinder icePrxFinder;
   private Properties properties;
   private int id;
   private static IceThreadMonitor iceThreadMonitor;

   protected Gateway createGateway(GatewayContextBuilder gatewayContext) {
      String strServerType = gatewayContext.getProperties().getPropertyWithDefault("ServerType", "TCP");
      return (Gateway)(strServerType.equalsIgnoreCase("WS") ? new GatewayWS(gatewayContext) : new Gateway(gatewayContext));
   }

   public int run(String[] arg0) {
      try {
         log.info("Gateway version @version@");
         log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
         this.gatewayContext = new GatewayContextBuilder();
         this.icePrxFinder = new IcePrxFinder(communicator(), communicator().getProperties());
         this.gatewayContext.setStartTime(System.currentTimeMillis());
         this.properties = communicator().getProperties();
         this.gatewayContext.setProperties(this.properties);
         this.gatewayContext.setCommunicator(communicator());
         this.gatewayContext.setGatewayThreadPool(this.pools);
         this.gatewayContext.setIcePrxFinder(this.icePrxFinder);
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
            this.gatewayObject = this.createGateway(this.gatewayContext);
         } else {
            this.gatewayObject = new Gateway(this.gatewayContext);
         }

         this.registryPrx = this.icePrxFinder.getRegistry(true);
         this.gatewayContext.setRegistryPrx(this.registryPrx);
         if (this.registryPrx == null) {
            throw new Exception("Unable to locate Registry");
         } else {
            this.id = this.registryPrx.newGatewayID();
            this.mogileFSManager = new MogileFSManager(this.properties.getPropertyWithDefault("MogileFSDomain", ""), this.properties.getPropertyWithDefault("MogileFSTrackers", "").split("[,;]"), this.properties.getPropertyAsIntWithDefault("MogileFSConnectionsPerTracker", 1), log);
            this.gatewayContext.setMogileFSManager(this.mogileFSManager);
            this.captchaMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.captcha);
            if (this.captchaMemcache == null) {
               log.warn("Failed to initialize memcached for CAPTCHA service. Local cache will be used instead");
            }

            this.captchaService = new CaptchaService(this.captchaMemcache, true);
            this.gatewayContext.setCaptchaService(this.captchaService);
            Gateway.ThreadPoolName[] arr$ = Gateway.ThreadPoolName.values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Gateway.ThreadPoolName name = arr$[i$];
               int maxSize = communicator().getProperties().getPropertyAsIntWithDefault("ThreadPool." + name + ".MaxSize", 40);
               int maxQueueSize = communicator().getProperties().getPropertyAsIntWithDefault("ThreadPool." + name + ".MaxQueueSize", Integer.MAX_VALUE);
               if (maxSize > 0 && maxSize != Integer.MAX_VALUE) {
                  this.pools.put(name, new InstrumentedThreadPool(maxSize, maxQueueSize));
               } else {
                  this.pools.put(name, new InstrumentedThreadPool());
               }
            }

            this.selector = Selector.open();
            this.gatewayContext.setSelector(this.selector);
            this.gatewayObject.bindServerSocket();
            this.connectionAdaptor = communicator().createObjectAdapter("ConnectionAdapter");
            this.connectionAdaptor.activate();
            this.gatewayContext.setConnectionAdapter(this.connectionAdaptor);
            IceStats.getInstance().setIceObjects(communicator(), this.connectionAdaptor, (ConfigurableExecutor)null);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IceThreadMonitorSettings.GWAY_TPOOL_MONITOR_STARTUP_ENABLED)) {
               iceThreadMonitor = new IceThreadMonitor(this.connectionAdaptor, SystemPropertyEntities.IceThreadMonitorSettings.GWAY_TPOOL_MONITOR_THREAD_COUNT, SystemPropertyEntities.IceThreadMonitorSettings.GWAY_TPOOL_MONITOR_MINIMUM_INTER_DUMP_DURATION);
               iceThreadMonitor.start(60);
            }

            this.adminAdaptor = communicator().createObjectAdapter("AdminAdapter");
            this.adminAdaptor.add(new GatewayAdminI(this.gatewayObject), Util.stringToIdentity("GatewayAdmin"));
            this.adminAdaptor.activate();
            int scanInterval = communicator().getProperties().getPropertyAsIntWithDefault("IdleConnetionScanIntrval", 60) * 1000;
            this.purger = new PurgeConnectionTask(this.gatewayObject, this.gatewayContext);
            this.gatewayContext.setPurger(this.purger);
            (new Timer()).scheduleAtFixedRate(this.purger, (long)scanInterval, (long)scanInterval);
            this.sampler = new SamplingTask(this.gatewayContext);
            this.gatewayContext.setSamplingTask(this.sampler);
            this.gatewayContext.build();
            ((InstrumentedThreadPool)this.pools.get(Gateway.ThreadPoolName.PRIMARY)).execute(this.sampler);
            log.info("Server started - ID: " + this.id + " Type: " + this.serverType + ", Port: " + this.gatewayObject.getLocalPort() + "\n");
            ((InstrumentedThreadPool)this.pools.get(Gateway.ThreadPoolName.PRIMARY)).execute(this.gatewayObject);
            communicator().waitForShutdown();
            log.info("Application is shutting down. Interrupted = " + interrupted());

            try {
               this.gatewayObject.closeServerChannel();
            } catch (IOException var8) {
            }

            Iterator i$ = this.pools.values().iterator();

            while(i$.hasNext()) {
               InstrumentedThreadPool pool = (InstrumentedThreadPool)i$.next();
               pool.shutdown();
            }

            log.info("Server terminated");
            return 0;
         }
      } catch (Exception var9) {
         log.fatal("Exception occured. Server terminated", var9);
         return -1;
      }
   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      GatewayMain gateway = new GatewayMain();
      String configFile = args.length > 0 ? args[0] : "Gateway.cfg";
      int status = gateway.main(gateway.getClass().getName(), args, configFile);
      System.exit(status);
   }
}
