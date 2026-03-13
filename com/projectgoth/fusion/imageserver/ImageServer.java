package com.projectgoth.fusion.imageserver;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.mogilefs.MogileFSManager;
import com.projectgoth.fusion.slice.ImageServerStats;
import com.projectgoth.fusion.stats.IceStats;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ImageServer extends Application implements Runnable {
   private static final String APP_NAME = "ImageServer";
   private static final String CONFIG_FILE = "ImageServer.cfg";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ImageServer.class));
   private ObjectAdapter adminAdaptor;
   private ThreadPoolExecutor pool;
   private Selector selector;
   private ServerSocketChannel serverChannel;
   private ConnectionPurger purger;
   private ImageCache imageCache;
   private String host;
   private int port;
   private Map<Connection, Integer> pendingRegistrations = new HashMap();
   private RequestCounter requestCounter;
   private long startTime;
   private int selectorTimeout;
   private int selectorPauseInterval;
   private int maxThreadPoolSize;
   private int timeoutInterval;
   private String mogileFSDomain;
   private String[] mogileFSTrackers;
   private int mogileFSConnectionsPerTracker;
   private int maxConcurrentImageIOAccess;
   private int maxImageCacheSize;
   private boolean cacheOriginalImage;
   private boolean cacheScaledImage;

   private boolean getSettings(String[] args) {
      Properties properties = communicator().getProperties();
      this.host = properties.getPropertyWithDefault("Server", "0.0.0.0");
      this.port = properties.getPropertyAsIntWithDefault("ServerPort", 80);
      this.selectorTimeout = properties.getPropertyAsIntWithDefault("SelectorTimeout", 100);
      this.selectorPauseInterval = properties.getPropertyAsIntWithDefault("SelectorPauseInterval", 10);
      this.timeoutInterval = properties.getPropertyAsIntWithDefault("IdleConnectionPurgeInterval", 0) * 1000;
      this.maxThreadPoolSize = properties.getPropertyAsIntWithDefault("MaxThreadPoolSize", Integer.MAX_VALUE);
      this.mogileFSDomain = properties.getProperty("MogileFSDomain");
      this.mogileFSTrackers = properties.getProperty("MogileFSTrackers").split("[,;]");
      this.mogileFSConnectionsPerTracker = properties.getPropertyAsIntWithDefault("MogileFSConnectionsPerTracker", 1);
      this.maxConcurrentImageIOAccess = properties.getPropertyAsIntWithDefault("MaxConcurrentImageIOAccess", 1);
      this.maxImageCacheSize = properties.getPropertyAsIntWithDefault("MaxImageCacheSize", 0);
      this.cacheOriginalImage = properties.getPropertyAsIntWithDefault("CacheOriginalImage", 1) == 1;
      this.cacheScaledImage = properties.getPropertyAsIntWithDefault("CacheScaledImage", 1) == 1;
      return true;
   }

   public ImageServerStats getStats() {
      ImageServerStats stats = ServiceStatsFactory.getImageServerStats(this.startTime);
      stats.port = this.port;
      stats.numConnectionObjects = 0;
      stats.maxConnectionObjects = 0;
      stats.requestsPerSecond = this.requestCounter.getRequestsPerSecond();
      stats.maxRequestsPerSecond = this.requestCounter.getMaxRequestsPerSecond();
      stats.cacheInfo = this.imageCache.getPerformanceSummary();
      stats.threadPoolSize = this.pool.getActiveCount();
      stats.maxThreadPoolSize = this.pool.getLargestPoolSize();
      stats.threadPoolQueueSize = this.pool.getQueue().size();
      return stats;
   }

   public ImageCache getImageCache() {
      return this.imageCache;
   }

   public void registerConnection(Connection connection, int interest) {
      synchronized(this.pendingRegistrations) {
         Integer oldInterest = (Integer)this.pendingRegistrations.get(connection);
         if (oldInterest != null) {
            if (oldInterest == interest) {
               return;
            }

            interest |= oldInterest;
         }

         this.pendingRegistrations.put(connection, interest);
      }
   }

   private void registerPendingConnecitons() {
      synchronized(this.pendingRegistrations) {
         Iterator i$ = this.pendingRegistrations.keySet().iterator();

         while(i$.hasNext()) {
            Connection connection = (Connection)i$.next();

            try {
               int interest = (Integer)this.pendingRegistrations.get(connection);
               SelectionKey key = connection.channel.keyFor(this.selector);
               if (key != null) {
                  interest |= key.interestOps();
               }

               connection.channel.register(this.selector, interest, connection);
            } catch (CancelledKeyException var7) {
               log.debug("Failed to register connection - key cancelled");
            } catch (ClosedChannelException var8) {
               log.debug("Failed to register connection - channel closed");
            } catch (Exception var9) {
               log.warn(var9.getClass().getName() + " caught in registerPendingConnecitons() - " + var9.getMessage());
            }
         }

         this.pendingRegistrations.clear();
      }
   }

   public void onConnectionDisconnected(Connection connection) {
      this.purger.remove(connection);
      SelectionKey key = connection.channel.keyFor(this.selector);
      if (key != null) {
         key.cancel();
      }

   }

   private void processSelections(Set<SelectionKey> selectedKeys) {
      Iterator i = selectedKeys.iterator();

      while(i.hasNext()) {
         SelectionKey key = (SelectionKey)i.next();

         try {
            if (key.isValid()) {
               if (key.isAcceptable()) {
                  this.onKeyAcceptable(key);
               }

               if (key.isWritable()) {
                  this.onKeyWritable(key);
               }

               if (key.isReadable()) {
                  this.onKeyReadable(key);
               }
            }
         } catch (CancelledKeyException var10) {
            log.debug("CancelledKeyException caught in processSelections()");
            key.cancel();
         } catch (Exception var11) {
            log.warn(var11.getClass().getName() + " caught in processSelections() - " + var11.getMessage());
            key.cancel();
         } finally {
            i.remove();
         }
      }

   }

   private void onKeyAcceptable(SelectionKey key) {
      SocketChannel channel = null;

      try {
         for(channel = this.serverChannel.accept(); channel != null; channel = this.serverChannel.accept()) {
            channel.configureBlocking(false);
            Connection connection = new Connection(this, channel);
            this.purger.add(connection);
            this.executeTask(connection);
         }
      } catch (Exception var6) {
         log.warn(var6.getClass().getName() + " caught in onKeyAcceptable() - " + var6.getMessage());
         if (channel != null) {
            try {
               channel.close();
            } catch (Exception var5) {
            }
         }
      }

   }

   private void onKeyReadable(SelectionKey key) {
      try {
         key.interestOps(key.interestOps() ^ 1);
         this.executeTask((Connection)key.attachment());
      } catch (Exception var3) {
         log.warn(var3.getClass().getName() + " caught in onKeyReadable() - " + var3.getMessage());
         key.cancel();
      }

   }

   private void onKeyWritable(SelectionKey key) {
      try {
         key.interestOps(key.interestOps() ^ 4);
         this.executeTask((Connection)key.attachment());
      } catch (Exception var3) {
         log.warn(var3.getClass().getName() + " caught in onKeyWritable() - " + var3.getMessage());
         key.cancel();
      }

   }

   private void executeTask(Runnable r) {
      this.requestCounter.add();
      this.pool.execute(r);
   }

   public int run(String[] arg0) {
      if (!this.getSettings(arg0)) {
         return -1;
      } else {
         this.requestCounter = new RequestCounter();
         this.startTime = System.currentTimeMillis();

         MogileFSManager mogileFSManager;
         try {
            mogileFSManager = new MogileFSManager(this.mogileFSDomain, this.mogileFSTrackers, this.mogileFSConnectionsPerTracker, log);
            log.info("Connected to " + this.mogileFSTrackers.length + " MogileFS trackers. " + this.mogileFSConnectionsPerTracker + " connection(s) per tracker");
         } catch (Exception var8) {
            log.fatal("Unable to initialize MogileFS - " + var8.getMessage());
            return -1;
         }

         this.imageCache = new ImageCache(mogileFSManager, this.maxImageCacheSize, this.cacheOriginalImage, this.cacheScaledImage);
         ImageItem.setImageIOSemaphore(new Semaphore(this.maxConcurrentImageIOAccess));
         if (this.maxThreadPoolSize > 0 && this.maxThreadPoolSize != Integer.MAX_VALUE) {
            this.pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(this.maxThreadPoolSize);
         } else {
            this.pool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
         }

         try {
            this.selector = Selector.open();
         } catch (IOException var7) {
            log.fatal("Unable to open selector");
            return -1;
         }

         try {
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.configureBlocking(false);
            this.serverChannel.socket().bind(new InetSocketAddress(this.host, this.port));
            this.serverChannel.register(this.selector, 16);
         } catch (IOException var6) {
            log.fatal("Failed to create server socket on port " + this.port + " - " + var6.getMessage());
            return -1;
         }

         IceStats.getInstance().setIceObjects(communicator(), (ObjectAdapter)null, (ConfigurableExecutor)null);
         this.adminAdaptor = communicator().createObjectAdapter("AdminAdapter");
         this.adminAdaptor.add(new ImageServerAdminI(this), Util.stringToIdentity("ImageServerAdmin"));
         this.adminAdaptor.activate();
         int scanInterval = communicator().getProperties().getPropertyAsIntWithDefault("IdleConnetionScanIntrval", 60) * 1000;
         this.purger = new ConnectionPurger(this.timeoutInterval, log);
         (new Timer()).schedule(this.purger, (long)scanInterval, (long)scanInterval);
         log.info("Image Server started - Port: " + this.serverChannel.socket().getLocalPort() + " Cache Size: " + (new DecimalFormat()).format((double)this.maxImageCacheSize / 1024.0D) + " KB\n");
         this.pool.execute(this);
         communicator().waitForShutdown();
         log.info("Terminating server");

         try {
            this.serverChannel.close();
         } catch (IOException var5) {
         }

         this.pool.shutdownNow();
         log.info("Server terminated");
         return 0;
      }
   }

   public void run() {
      while(true) {
         try {
            if (this.selector.select((long)this.selectorTimeout) > 0) {
               this.processSelections(this.selector.selectedKeys());
            }

            this.registerPendingConnecitons();
            if (this.selectorPauseInterval > 0) {
               Thread.sleep((long)this.selectorPauseInterval);
            }
         } catch (Exception var4) {
            log.warn(var4.getClass().getName() + " caught in selection loop", var4);
            log.info("Reinitializing selector");

            try {
               this.selector.close();
               this.selector = Selector.open();
               this.serverChannel.register(this.selector, 16);
            } catch (Exception var3) {
               log.fatal(var3.getClass().getName() + " caught in while reinitializing selector", var3);
               System.exit(-1);
            }

            log.info("Selector reinitialized");
         }
      }
   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("ImageServer version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      ImageServer imageServer = new ImageServer();
      String configFile = args.length > 0 ? args[0] : "ImageServer.cfg";
      int status = imageServer.main(imageServer.getClass().getName(), args, configFile);
      System.exit(status);
   }
}
