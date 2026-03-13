package com.projectgoth.fusion.gateway;

import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MultiSampler;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.ConnectionPrxHelper;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GatewayStats;
import com.projectgoth.fusion.slice.GatewayThreadPoolStats;
import com.projectgoth.fusion.stats.ConcurrentCountsMap;
import com.projectgoth.fusion.stats.FusionPktOpenURLStats;
import com.projectgoth.fusion.userevent.EventTextTranslator;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class Gateway implements Runnable {
   protected GatewayContext applicationContext;
   private ServerSocketChannel serverChannel;
   private Map<ConnectionI, Integer> pendingRegistrations = new HashMap();
   private Set<ConnectionI> connectionSet = Collections.newSetFromMap(new ConcurrentHashMap());
   protected Gateway.ServerType serverType;
   private String host;
   protected int port;
   private int backlog;
   private String offlineMessage;
   private static EventTextTranslator translator = new EventTextTranslator();
   AtomicInteger connectionCount = new AtomicInteger(0);
   private boolean tooBusy;
   private int id;
   private int maxThreadPoolQueueSize;
   private int maxConnections;
   private int minProtocolVersion;
   private int minMidletVersion;
   private int maxMidletVersion;
   private int maxAllowableThreadPoolQueueSize;
   private int maxAllowableTimeInQueue;
   private int keepAliveInterval;
   int timeoutInterval;
   private int generalCoolDown;
   private int maxFailedLoginAttempts;
   private int loginReplyDelay;
   private int socketCloseDelay;
   private int selectorTimeout;
   private int selectorPauseInterval;
   private int tooBusyCheckInterval;
   private int timesTooBusy;
   private int imLoginTimeout;
   private int minIntervalBetweenPackets;
   private int maxPacketsToBuffer;
   private int numberOfEventsToKeep;
   private int chatRoomNotificationInterval;
   private boolean enableOfflineMessage;
   private Date lastTimeTooBusy = new Date();
   private String imFilePath;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Gateway.class));
   private static volatile ConcurrentCountsMap<String> remoteIpCxnCounts = new ConcurrentCountsMap();
   private Runnable processAcceptsThread = new Runnable() {
      public void run() {
         try {
            Gateway.this.doAccepts();
         } catch (FusionException var2) {
            Gateway.log.error("Exception when accepting new connections ", var2);
         }

      }
   };
   private static LazyLoader<Boolean> trackCxnsPerRemoteIPEnabled = new LazyLoader<Boolean>("TRACK_CONNECTIONS_PER_REMOTE_IP_ENABLED", 60000L) {
      private Boolean previousValue;

      protected Boolean fetchValue() throws Exception {
         return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.TRACK_CONNECTIONS_PER_REMOTE_IP_ENABLED);
      }

      protected void onValueChanged(Boolean lastValue, Boolean currentValue) {
         Gateway.remoteIpCxnCounts = new ConcurrentCountsMap();
      }
   };

   public Gateway(GatewayContext context) {
      this.applicationContext = context;
      this.initializeSettings();
   }

   private void initializeSettings() {
      String strServerType = this.applicationContext.getProperties().getPropertyWithDefault("ServerType", "TCP");
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
         if (strServerType.equalsIgnoreCase("HTTP")) {
            this.serverType = Gateway.ServerType.HTTP;
         } else if (strServerType.equalsIgnoreCase("TCP")) {
            this.serverType = Gateway.ServerType.TCP;
         } else {
            this.serverType = Gateway.ServerType.WS;
         }
      } else {
         this.serverType = strServerType.equalsIgnoreCase("HTTP") ? Gateway.ServerType.HTTP : Gateway.ServerType.TCP;
      }

      this.host = this.applicationContext.getProperties().getPropertyWithDefault("Server", "0.0.0.0");
      this.port = this.applicationContext.getProperties().getPropertyAsIntWithDefault("ServerPort", 8888);
      this.backlog = this.applicationContext.getProperties().getPropertyAsIntWithDefault("Backlog", 100);
      this.offlineMessage = this.applicationContext.getProperties().getPropertyWithDefault("OfflineMessage", "");
      this.minProtocolVersion = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MinProtocolVersion", 0);
      this.minMidletVersion = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MinMidletVersion", 0);
      this.maxMidletVersion = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MaxMidletVersion", Integer.MAX_VALUE);
      this.generalCoolDown = this.applicationContext.getProperties().getPropertyAsIntWithDefault("GeneralCoolDown", 15) * 1000;
      this.maxFailedLoginAttempts = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MaxFailedLoginAttempts", 10);
      this.loginReplyDelay = this.applicationContext.getProperties().getPropertyAsIntWithDefault("LoginReplyDelay", 10) * 1000;
      this.socketCloseDelay = this.applicationContext.getProperties().getPropertyAsIntWithDefault("SocketCloseDelay", 10);
      this.keepAliveInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("KeepAliveInterval", 20) * 1000;
      this.timeoutInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("IdleConnectionPurgeInterval", 0) * 1000;
      this.selectorTimeout = this.applicationContext.getProperties().getPropertyAsIntWithDefault("SelectorTimeout", 100);
      this.selectorPauseInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("SelectorPauseInterval", 10);
      this.tooBusyCheckInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("TooBusyCheckInterval", 500);
      this.maxAllowableThreadPoolQueueSize = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MaxAllowableThreadPoolQueueSize", Integer.MAX_VALUE);
      this.maxAllowableTimeInQueue = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MaxAllowableTimeInQueue", Integer.MAX_VALUE);
      if (this.maxAllowableTimeInQueue < Integer.MAX_VALUE) {
         this.maxAllowableTimeInQueue *= 1000;
      }

      this.minIntervalBetweenPackets = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MinIntervalBetweenPackets", 2000);
      this.maxPacketsToBuffer = this.applicationContext.getProperties().getPropertyAsIntWithDefault("MaxPacketsToBuffer", 20);
      this.enableOfflineMessage = this.applicationContext.getProperties().getPropertyAsIntWithDefault("EnableOfflineMessage", 1) == 1;
      this.imFilePath = this.applicationContext.getProperties().getPropertyWithDefault("IMFilePath", "/usr/fusion/im");
      this.imLoginTimeout = this.applicationContext.getProperties().getPropertyAsIntWithDefault("IMLoginTimeout", 30) * 1000;
      this.numberOfEventsToKeep = this.applicationContext.getProperties().getPropertyAsIntWithDefault("EventsToKeep", 10);
      this.chatRoomNotificationInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("ChatRoomNotificationInterval", 60) * 1000;
   }

   public GatewayStats getStats() {
      GatewayStats stats = ServiceStatsFactory.getGatewayStats(this.applicationContext.getStartTime());
      stats.serverType = this.serverType.toString();
      stats.port = this.port;
      stats.numConnectionObjects = this.connectionCount.get();
      stats.maxConnectionObjects = this.maxConnections;
      stats.threadPoolStats = new GatewayThreadPoolStats[this.applicationContext.getGatewayThreadPool().size()];
      stats.tooBusy = this.tooBusy;
      stats.timesTooBusy = this.timesTooBusy;
      stats.lastTimeTooBusy = this.lastTimeTooBusy.getTime();
      int i = 0;

      GatewayThreadPoolStats poolStats;
      for(Iterator i$ = this.applicationContext.getGatewayThreadPool().keySet().iterator(); i$.hasNext(); stats.threadPoolStats[i++] = poolStats) {
         Gateway.ThreadPoolName name = (Gateway.ThreadPoolName)i$.next();
         InstrumentedThreadPool pool = (InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(name);
         poolStats = new GatewayThreadPoolStats();
         poolStats.name = name.toString();
         poolStats.requestsPerSecond = pool.getRequestsPerSecond();
         poolStats.maxRequestsPerSecond = pool.getMaxRequestsPerSecond();
         poolStats.threadPoolSize = pool.getActiveCount();
         poolStats.maxThreadPoolSize = pool.getLargestPoolSize();
         poolStats.threadPoolQueueSize = pool.getQueueSize();
         poolStats.maxThreadPoolQueueSize = 0;
      }

      if ((Boolean)trackCxnsPerRemoteIPEnabled.getValue()) {
         int remoteIPs = remoteIpCxnCounts.size();
         if (remoteIPs != 0) {
            stats.connectionsPerRemoteIP = (float)this.connectionCount.get() / (float)remoteIPs;
         }
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.OPEN_URL_STATS_ENABLED)) {
         FusionPktOpenURLStats instance = FusionPktOpenURLStats.getInstance();
         stats.openUrlAttempts = instance.getTotalAttempts();
         stats.openUrlFailures = instance.getTotalFailures();
         stats.openUrlFailurePercent = stats.openUrlAttempts != 0 ? (float)stats.openUrlFailures / (float)stats.openUrlAttempts * 100.0F : 0.0F;
         stats.openUrlFailuresByUrl = instance.getFailuresByURL();
         int successes = stats.openUrlAttempts - stats.openUrlFailures;
         stats.averageSuccessfulProcessingTimeSeconds = successes != 0 ? (float)instance.getTotalSuccessesProcessingTime() / (float)successes / 1000.0F : 0.0F;
      }

      return stats;
   }

   public ConnectionPrx addConnection(String objectId, ConnectionI connection) {
      ObjectPrx basePrx = this.applicationContext.getConnectionAdapter().add(connection, Util.stringToIdentity(objectId));
      int count = this.connectionCount.incrementAndGet();
      if (count > this.maxConnections) {
         this.maxConnections = count;
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.PT66915380_ENABLE_CONNECTION_TRACKING)) {
         this.connectionSet.add(connection);
      }

      if ((Boolean)trackCxnsPerRemoteIPEnabled.getValue()) {
         remoteIpCxnCounts.increment(connection.remoteAddress);
      }

      if (log.isDebugEnabled()) {
         log.debug("Connection " + objectId + " (" + connection.getDisplayName() + ") added to Ice adaptor");
      }

      return ConnectionPrxHelper.uncheckedCast(basePrx);
   }

   public void registerConnection(ConnectionI connection, int interest) throws FusionException {
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

   protected void registerPendingConnections() throws FusionException {
      synchronized(this.pendingRegistrations) {
         if (this.pendingRegistrations.size() > 0) {
            Iterator i$ = this.pendingRegistrations.keySet().iterator();

            while(i$.hasNext()) {
               ConnectionI connection = (ConnectionI)i$.next();

               try {
                  int interest = (Integer)this.pendingRegistrations.get(connection);
                  SelectionKey key = connection.getSelectionKey(this.applicationContext.getSelector());
                  if (key == null) {
                     connection.channel.register(this.applicationContext.getSelector(), interest, connection);
                  } else {
                     key.interestOps(key.interestOps() | interest);
                  }
               } catch (ClosedChannelException var7) {
                  log.debug("Failed to register connection - channel closed");
               } catch (CancelledKeyException var8) {
                  log.debug("CancelledKeyException caught in registerPendingConnections()");
               } catch (Exception var9) {
                  log.warn(var9.getClass().getName() + " caught in registerPendingConnections() - " + var9.getMessage());
               }
            }

            this.pendingRegistrations.clear();
         }

      }
   }

   public ConnectionI findConnection(String objectId) {
      try {
         return (ConnectionI)this.applicationContext.getConnectionAdapter().find(Util.stringToIdentity(objectId));
      } catch (Exception var3) {
         return null;
      }
   }

   public void removeConnection(String objectId, String remoteAddress) {
      if (log.isDebugEnabled()) {
         log.debug("Removing connection " + objectId + " from Ice adaptor");
      }

      ConnectionI connection = (ConnectionI)this.applicationContext.getConnectionAdapter().remove(Util.stringToIdentity(objectId));
      this.connectionCount.decrementAndGet();
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.PT66915380_ENABLE_CONNECTION_TRACKING)) {
         this.connectionSet.remove(connection);
      }

      if ((Boolean)trackCxnsPerRemoteIPEnabled.getValue()) {
         remoteIpCxnCounts.decrement(remoteAddress);
      }

   }

   public void onConnectionAccessed(ConnectionI connection) {
      this.applicationContext.getPurger().monitor(connection);
   }

   public void onConnectionDisconnected(ConnectionI connection) {
      if (log.isDebugEnabled()) {
         log.debug("Connection " + connection.getDisplayName() + " disconnected");
      }

      if (connection.getConnectionPrx() == null) {
         this.applicationContext.getPurger().remove(connection);
      }

      SelectionKey key = connection.getSelectionKey(this.applicationContext.getSelector());
      if (key != null) {
         key.cancel();
      }

   }

   public boolean verifyProtocolVersion(int version) {
      return version >= this.minProtocolVersion;
   }

   public boolean verifyMidletVersion(int version) {
      return version >= this.minMidletVersion && version <= this.maxMidletVersion;
   }

   private void onTooBusy() {
      this.tooBusy = true;
      this.lastTimeTooBusy.setTime(System.currentTimeMillis());
      ++this.timesTooBusy;
      log.warn("Server too busy, [" + ((InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).getQueueSize() + "] tasks in the queue");
   }

   private void onNoLongerTooBusy() throws IOException {
      log.warn("Server recovered from too busy");
      this.bindServerSocket();
      this.tooBusy = false;
   }

   public boolean isTooBusy() throws IOException {
      if (this.maxAllowableTimeInQueue == Integer.MAX_VALUE) {
         return false;
      } else {
         if (this.tooBusy) {
            int size = ((InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).getQueueSize();
            if (size > this.maxThreadPoolQueueSize) {
               this.maxThreadPoolQueueSize = size;
            }

            if (size <= this.maxAllowableThreadPoolQueueSize) {
               this.onNoLongerTooBusy();
            }
         } else {
            InstrumentedThreadPool.Task eldestTask = ((InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(Gateway.ThreadPoolName.PRIMARY)).getEldestTaskOnQueue();
            if (eldestTask != null && System.currentTimeMillis() - eldestTask.getTimeCreated() > (long)this.maxAllowableTimeInQueue) {
               this.onTooBusy();
            }
         }

         return this.tooBusy;
      }
   }

   public int getPort() {
      return this.port;
   }

   public String getOfflineMessage() {
      return this.offlineMessage;
   }

   public int getKeepAliveInterval() {
      return this.keepAliveInterval;
   }

   public int getGeneralCoolDown() {
      return this.generalCoolDown;
   }

   public int getMaxFailedLoginAttempts() {
      return this.maxFailedLoginAttempts;
   }

   public int getLoginReplyDelay() {
      return this.loginReplyDelay;
   }

   public int getSocketCloseDelay() {
      return this.socketCloseDelay;
   }

   public boolean getEnableOfflineMessage() {
      return this.enableOfflineMessage;
   }

   public int getMinIntervalBetweenPackets() {
      return this.minIntervalBetweenPackets;
   }

   public int getMaxPacketsToBuffer() {
      return this.maxPacketsToBuffer;
   }

   public MultiSampler getSampler() {
      return this.applicationContext.getSamplingTask().sampler;
   }

   public int getID() {
      return this.id;
   }

   public int getImLoginTimeout() {
      return this.imLoginTimeout;
   }

   public static EventTextTranslator getTranslator() {
      return translator;
   }

   public int getNumberOfEventsToKeep() {
      return this.numberOfEventsToKeep;
   }

   public int getChatRoomNotificationInterval() {
      return this.chatRoomNotificationInterval;
   }

   public String getIMFilePath() {
      return this.imFilePath;
   }

   protected void processSelections(Set<SelectionKey> selectedKeys) throws FusionException {
      Iterator i$ = selectedKeys.iterator();

      while(i$.hasNext()) {
         SelectionKey key = (SelectionKey)i$.next();

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
         } catch (CancelledKeyException var5) {
            log.debug("CancelledKeyException caught in processSelections()");
            key.cancel();
         } catch (Exception var6) {
            log.warn(var6.getClass().getName() + " caught in processSelections() - " + var6.getMessage());
            key.cancel();
         }
      }

      selectedKeys.clear();
   }

   protected void onKeyAcceptable(SelectionKey key) throws FusionException {
      this.executeTask(Gateway.ThreadPoolName.PRIMARY, this.processAcceptsThread);
   }

   protected void doAccepts() throws FusionException {
      SocketChannel channel = null;

      try {
         synchronized(this.serverChannel) {
            channel = this.serverChannel.accept();
         }

         while(channel != null) {
            channel.configureBlocking(false);
            Object connection;
            if (this.serverType == Gateway.ServerType.HTTP) {
               connection = new ConnectionHTTP(this, channel, this.applicationContext);
            } else {
               connection = new ConnectionTCP(this, channel, this.applicationContext);
            }

            if (log.isDebugEnabled()) {
               log.debug("Accepted connection " + ((ConnectionI)connection).getDisplayName());
            }

            this.applicationContext.getPurger().add((ConnectionI)connection);
            channel.register(this.applicationContext.getSelector(), 1, connection);
            synchronized(this.serverChannel) {
               channel = this.serverChannel.accept();
            }
         }
      } catch (Exception var8) {
         log.warn(var8.getClass().getName() + " caught in onKeyAcceptable()", var8);
         if (channel != null) {
            try {
               channel.close();
            } catch (Exception var5) {
            }
         }
      }

   }

   protected void onKeyReadable(SelectionKey key) throws FusionException {
      try {
         ConnectionI conn = (ConnectionI)key.attachment();
         key.interestOps(key.interestOps() ^ 1);
         this.executeTask(Gateway.ThreadPoolName.PRIMARY, conn.getReadCallBack());
      } catch (Exception var3) {
         log.warn(var3.getClass().getName() + " caught in onKeyReadable()", var3);
         key.cancel();
      }

   }

   protected void onKeyWritable(SelectionKey key) throws FusionException {
      try {
         ConnectionI conn = (ConnectionI)key.attachment();
         key.interestOps(key.interestOps() ^ 4);
         this.executeTask(Gateway.ThreadPoolName.PRIMARY, conn.getWriteCallBack());
      } catch (Exception var3) {
         log.warn(var3.getClass().getName() + " caught in onKeyWritable()", var3);
         key.cancel();
      }

   }

   public void executeTask(Gateway.ThreadPoolName name, Runnable task) {
      ((InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(name)).execute(task);
   }

   public void scheduleTask(Gateway.ThreadPoolName name, Runnable task, long delay, TimeUnit unit) {
      if (delay == 0L) {
         this.executeTask(name, task);
      } else {
         ((InstrumentedThreadPool)this.applicationContext.getGatewayThreadPool().get(name)).schedule(task, delay, unit);
      }

   }

   public void bindServerSocket() throws IOException {
      this.serverChannel = ServerSocketChannel.open();
      this.serverChannel.configureBlocking(false);
      this.serverChannel.socket().bind(new InetSocketAddress(this.host, this.port), this.backlog);
      this.serverChannel.register(this.applicationContext.getSelector(), 16);
   }

   public void run() {
      long lastChecked = 0L;

      while(true) {
         while(true) {
            try {
               if (System.currentTimeMillis() - lastChecked > (long)this.tooBusyCheckInterval) {
                  lastChecked = System.currentTimeMillis();
                  if (this.isTooBusy() && this.serverChannel.isOpen()) {
                     log.warn("Closing server socket");
                     this.serverChannel.close();
                  }
               }

               if (this.applicationContext.getSelector().select((long)this.selectorTimeout) > 0) {
                  this.processSelections(this.applicationContext.getSelector().selectedKeys());
               }

               this.registerPendingConnections();
               if (this.selectorPauseInterval > 0) {
                  Thread.sleep((long)this.selectorPauseInterval);
               }
            } catch (Exception var4) {
               log.fatal(var4.getClass().getName() + " caught in selection loop", var4);
               System.exit(-1);
            }
         }
      }
   }

   public void closeServerChannel() throws IOException {
      this.serverChannel.close();
   }

   public int getLocalPort() {
      return this.serverChannel.socket().getLocalPort();
   }

   public Gateway.ServerType getServerType() {
      return this.serverType;
   }

   public Selector getSelector() {
      return this.applicationContext.getSelector();
   }

   public void logDebugPurgingCompleted() {
      log.debug("Purging task completed. " + this.connectionCount.get() + " " + this.getServerType() + " connection(s) left on the adaptor. " + this.getSelector().keys().size() + " key(s) left on selector");
   }

   public long getTimeoutInterval() {
      return (long)this.timeoutInterval;
   }

   public static void main(String[] args) {
      GatewayMain.main(args);
   }

   public List<ConnectionI> getConnections() {
      return new ArrayList(this.connectionSet);
   }

   public static enum ThreadPoolName {
      PRIMARY,
      HTTP_TUNNEL,
      DATA_UPLOAD,
      DELAYED_LOGIN_RESPONSE_RESULT;
   }

   public static enum ServerType {
      HTTP,
      TCP,
      WS;
   }
}
