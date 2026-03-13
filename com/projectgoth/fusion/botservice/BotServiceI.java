package com.projectgoth.fusion.botservice;

import Ice.Current;
import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.slice.BotChannelPrx;
import com.projectgoth.fusion.slice.BotInstance;
import com.projectgoth.fusion.slice.BotServicePrx;
import com.projectgoth.fusion.slice.BotServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._BotServiceDisp;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class BotServiceI extends _BotServiceDisp implements InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotServiceI.class));
   private BotServicePrx proxy;
   private ScheduledExecutorService executor;
   private RequestCounter requestCounter = new RequestCounter();
   private BotDAO botDAO;
   private Map<String, Bot> botMappings = new ConcurrentHashMap();
   private Map<String, Set<Bot>> channelBotMappings = new ConcurrentHashMap();
   private long startTime = System.currentTimeMillis();
   private int maxBotIntances;
   private int maxChannels;

   public void afterPropertiesSet() throws Exception {
   }

   public void shutdown() {
   }

   public BotServiceStats getStats() {
      BotServiceStats stats = ServiceStatsFactory.getBotServiceStats(this.startTime);
      stats.numBotObjects = this.botMappings.size();
      stats.maxBotObjects = this.maxBotIntances;
      stats.numBotChannelObjects = this.channelBotMappings.size();
      stats.maxBotChannelObjects = this.maxChannels;
      stats.requestsPerSecond = this.requestCounter.getRequestsPerSecond();
      stats.maxRequestsPerSecond = this.requestCounter.getMaxRequestsPerSecond();
      if (this.executor instanceof ScheduledThreadPoolExecutor) {
         ScheduledThreadPoolExecutor e = (ScheduledThreadPoolExecutor)this.executor;
         stats.threadPoolSize = e.getActiveCount();
         stats.maxThreadPoolSize = e.getCorePoolSize();
         stats.threadPoolQueueSize = e.getQueue().size();
      }

      return stats;
   }

   public void setBotDAO(BotDAO botDAO) {
      this.botDAO = botDAO;
   }

   public void setProxy(BotServicePrx proxy) {
      this.proxy = proxy;
   }

   public void setExecutor(ScheduledExecutorService executor) {
      this.executor = executor;
   }

   public BotInstance addBotToChannel(BotChannelPrx channelProxy, String botCommandName, String starterUsername, boolean purgeIfIdle, Current __current) throws FusionException {
      String channelID = channelProxy.ice_getIdentity().name;
      Set<Bot> botsInChannel = (Set)this.channelBotMappings.get(channelID);
      if (botsInChannel == null) {
         botsInChannel = new CopyOnWriteArraySet();
         this.channelBotMappings.put(channelID, botsInChannel);
         int mapSize = this.channelBotMappings.size();
         if (mapSize > this.maxChannels) {
            this.maxChannels = mapSize;
         }
      }

      Iterator i$ = ((Set)botsInChannel).iterator();

      Bot bot;
      do {
         if (!i$.hasNext()) {
            try {
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               BotData botData = messageEJB.getBotFromCommandName(botCommandName);
               if (botData == null) {
                  throw new FusionException(botCommandName + " is not a valid bot command");
               }

               Bot bot = BotLoader.addBotToChannel(this.executor, botData, channelProxy, this.botDAO, starterUsername, "ENG");
               bot.setPurgeIfIdle(purgeIfIdle);
               ((Set)botsInChannel).add(bot);
               this.botMappings.put(bot.getInstanceID(), bot);
               int mapSize = this.botMappings.size();
               if (mapSize > this.maxBotIntances) {
                  this.maxBotIntances = mapSize;
               }

               log.info(starterUsername + " started " + bot.getBotData().getDisplayName() + " [" + bot.getInstanceID() + "] in channel [" + channelID + "]");
               BotInstance botInstance = bot.getInstance();
               botInstance.botServiceProxy = this.proxy;
               this.requestCounter.add();
               return botInstance;
            } catch (FusionException var13) {
               throw var13;
            } catch (Exception var14) {
               log.error("Error adding bot to channel :" + var14.getMessage(), var14);
               throw new FusionException("Error adding bot to channel. " + var14.getMessage());
            }
         }

         bot = (Bot)i$.next();
      } while(!bot.getBotData().getCommandName().equals(botCommandName) || !bot.getChannelProxy().equals(channelProxy));

      BotInstance botInstance = bot.getInstance();
      botInstance.botServiceProxy = this.proxy;
      return botInstance;
   }

   public void removeBot(String botInstanceID, boolean stopEvenIfGameInProgress, Current __current) throws FusionException {
      Bot bot = (Bot)this.botMappings.get(botInstanceID);
      if (bot != null) {
         if (!stopEvenIfGameInProgress && !bot.canBeStoppedNow()) {
            throw new FusionException("Unable to remove '" + bot.getBotData().getDisplayName() + "'. There is a game currently in progress");
         } else {
            this.botMappings.remove(botInstanceID);
            Set<Bot> botsInChannel = (Set)this.channelBotMappings.get(bot.getChannelProxy().ice_getIdentity().name);
            if (botsInChannel != null) {
               botsInChannel.remove(bot);
               if (botsInChannel.size() == 0) {
                  this.channelBotMappings.remove(bot.getChannelProxy().ice_getIdentity().name);
               }
            }

            bot.stopBot();
            log.info(bot.getBotData().getDisplayName() + " [" + bot.getInstanceID() + "] stopped channel [" + bot.getChannelProxy().ice_getIdentity().name + "]");
            this.requestCounter.add();
         }
      }
   }

   public void sendMessageToBot(String botInstanceID, String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      Bot bot = (Bot)this.botMappings.get(botInstanceID);
      if (bot != null) {
         bot.queueIncomingMessage(message, username, receivedTimestamp);
      }

      this.requestCounter.add();
   }

   public void sendMessageToBotsInChannel(String channelID, String username, String message, long receivedTimestamp, Current __current) throws FusionException {
      Set<Bot> botsInChannel = (Set)this.channelBotMappings.get(channelID);
      if (botsInChannel != null) {
         Iterator i$ = botsInChannel.iterator();

         while(i$.hasNext()) {
            Bot bot = (Bot)i$.next();
            bot.queueIncomingMessage(message, username, receivedTimestamp);
         }
      }

      this.requestCounter.add();
   }

   public void sendNotificationToBotsInChannel(String channelID, String username, int notification, Current __current) throws FusionException {
      Set<Bot> botsInChannel = (Set)this.channelBotMappings.get(channelID);
      if (botsInChannel != null) {
         BotData.BotCommandEnum botCommand = BotData.BotCommandEnum.fromValue(notification);
         Iterator i$ = botsInChannel.iterator();

         while(i$.hasNext()) {
            Bot bot = (Bot)i$.next();
            bot.queueIncomingNotification(botCommand, username);
         }
      }

      this.requestCounter.add();
   }

   public void purgeIdleBots() {
      Iterator i$ = this.botMappings.values().iterator();

      while(i$.hasNext()) {
         Bot bot = (Bot)i$.next();
         if (bot.purgeIfIdle() && bot.isIdle()) {
            try {
               if (log.isDebugEnabled()) {
                  log.debug("Removing idle bot instanceID [" + bot.getInstanceID() + "]");
               }

               this.removeBot(bot.getInstanceID(), false);
               bot.getChannelProxy().botKilled(bot.getInstanceID());
            } catch (Exception var4) {
               log.warn("Error removing idle bot: " + bot.getInstanceID(), var4);
            }
         }
      }

   }
}
