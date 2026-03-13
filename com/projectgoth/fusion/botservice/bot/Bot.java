package com.projectgoth.fusion.botservice.bot;

import com.projectgoth.fusion.botservice.BotService;
import com.projectgoth.fusion.botservice.bot.migbot.common.LimitTracker;
import com.projectgoth.fusion.botservice.message.GameMessage;
import com.projectgoth.fusion.botservice.message.MessageQueue;
import com.projectgoth.fusion.botservice.message.NotificationMessage;
import com.projectgoth.fusion.botservice.message.ResponseMessage;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserPotEligibilityData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.slice.BotChannelPrx;
import com.projectgoth.fusion.slice.BotInstance;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.log4j.Logger;

public abstract class Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Bot.class));
   private static final Logger logMetrics = Logger.getLogger("gamesBotMetricsLog");
   protected String instanceID = UUID.randomUUID().toString();
   protected ScheduledExecutorService executor;
   protected BotData botData;
   private BotDAO botDAO;
   private BotChannelPrx channelProxy;
   protected String channel;
   protected String languageCode;
   protected String botStarter;
   protected String gameStarter;
   protected boolean purgeIfIdle;
   private MessageQueue incomingMessageQueue;
   private MessageQueue outgoingMessageQueue;
   protected String[] emoticonHotKeys;
   protected Map<String, String> gameParameters;
   protected Map<String, String> messages;
   public static final String BASE_CURRENCY = "AUD";
   public static final String CURRENCY = "USD";
   public static final String CURRENCY_DENOMINATION = "c";
   protected static final String COMMAND_CHAR = "!";
   protected static final String COMMAND_START = "!start";
   protected static final String COMMAND_NO = "!n";
   protected static final String COMMAND_JOIN = "!j";
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSZ");
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
   protected Pot pot;

   public Bot(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      this.executor = executor;
      this.channelProxy = channelProxy;
      this.channel = channelProxy == null ? null : channelProxy.ice_getIdentity().name;
      this.languageCode = languageCode;
      this.botDAO = botDAO;
      this.botStarter = botStarter;
      this.incomingMessageQueue = new MessageQueue(this, executor);
      this.outgoingMessageQueue = new MessageQueue(this, executor);
      this.botData = botData;
      this.loadEmoticonKeys();
      this.loadConfig();
      this.loadLanguagePack();
   }

   private void loadEmoticonKeys() {
      if (this.botData != null && this.botData.getEmoticonKeyList() != null) {
         this.emoticonHotKeys = this.botData.getEmoticonKeyList().split(" ");
      } else {
         this.emoticonHotKeys = new String[0];
      }

   }

   private void loadConfig() {
      this.gameParameters = (Map)(this.botDAO == null ? new HashMap() : this.botDAO.getBotConfig(this.botData.getId()));
   }

   private void loadLanguagePack() {
      this.messages = (Map)(this.botDAO == null ? new HashMap() : this.botDAO.getBotMessages(this.botData.getId(), this.languageCode));
   }

   public abstract boolean isIdle();

   public abstract boolean canBeStoppedNow();

   public abstract void stopBot();

   public abstract void onUserJoinChannel(String var1);

   public abstract void onUserLeaveChannel(String var1);

   public abstract void onMessage(String var1, String var2, long var3);

   protected String getLogMessage(String subMessage) {
      return String.format("%s [%s] [%s] %s", this.botData.getDisplayName(), this.instanceID, this.channel, subMessage);
   }

   public void queueIncomingMessage(String message, String player, long receivedTimestamp) {
      if (log.isInfoEnabled()) {
         log.info(this.getLogMessage(String.format("[%s] [%s] [%s]", player, DATE_FORMAT.format(new Date(receivedTimestamp)), message)));
      }

      this.incomingMessageQueue.queue(new GameMessage(player, message, receivedTimestamp));
   }

   public void queueIncomingNotification(BotData.BotCommandEnum notification, String username) {
      this.incomingMessageQueue.queue(new NotificationMessage(username, notification));
   }

   public void sendMessage(String message, String player) {
      this.outgoingMessageQueue.queue(new ResponseMessage(player, message));
   }

   public void sendMessage(String message, String[] players) {
      this.outgoingMessageQueue.queue(new ResponseMessage(players, message));
   }

   public void sendChannelMessage(String message) {
      this.outgoingMessageQueue.queue(new ResponseMessage(message));
   }

   public void sendChannelMessageAndPopUp(String message) {
      this.outgoingMessageQueue.queue(new ResponseMessage(message, true));
   }

   public boolean userCanAffordItem(String username, double cost) {
      if (cost == 0.0D) {
         return true;
      } else {
         boolean canAfford = false;

         try {
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            canAfford = accountEJB.userCanAffordCost(username, cost, "USD", (Connection)null);
         } catch (Exception var7) {
            log.error(this.getLogMessage("Unexpected exception User[" + username + "] Cost[" + cost + "USD" + "] :" + var7.getMessage()), var7);
         }

         if (log.isDebugEnabled()) {
            log.debug("User [" + username + "] cost[" + cost + "USD" + "] canAfford?:" + canAfford);
         }

         return canAfford;
      }
   }

   public synchronized AccountEntryData chargeUserForItem(String username, String reference, double amount, String description) throws Exception {
      Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
      AccountEntryData data = accountEJB.chargeUserForGameItem(username, reference.toLowerCase(), description, amount, "USD", new AccountEntrySourceData(BotService.class));
      if (log.isDebugEnabled()) {
         log.debug("User [" + username + "] amount[" + amount + "USD" + "] reference[" + reference.toLowerCase() + "] description[" + description + "] ");
      }

      return data;
   }

   public boolean userCanAffordToEnterPot(String username, double cost, boolean checkUserLimit) {
      try {
         if (cost == 0.0D) {
            return true;
         } else {
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            UserPotEligibilityData data = accountEJB.userCanAffordToEnterPot(username, cost, "USD", "AUD");
            if (log.isDebugEnabled()) {
               log.debug("User [" + username + "] type[" + data.getUserType() + "] cost [" + data.getCostInBaseCurrency() + "AUD" + "] eligible?:" + data.isEligible());
            }

            if (data.isEligible() && checkUserLimit) {
               if (data.getUserType() == UserData.TypeEnum.MIG33_TOP_MERCHANT.value()) {
                  double amountLimitPerSession = SystemProperty.getDouble("MerchantLimitPerGame", 1.0D);
                  double amountLimitPerTimeSlot = SystemProperty.getDouble("MerchantLimitPerTimeSlot", 20.0D);
                  long timeSlot = SystemProperty.getLong("MerchantLimitTimeSlot", 24L) * 60L * 60L * 1000L;
                  if (log.isDebugEnabled()) {
                     log.debug(username + ": per game limit: " + amountLimitPerSession + "AUD" + ", limited to " + amountLimitPerTimeSlot + "AUD" + " every " + timeSlot / 3600000L + " hour(s)");
                  }

                  if (data.getCostInBaseCurrency() > amountLimitPerSession) {
                     this.sendMessage("You have exceeded your limit for this game", username);
                     return false;
                  } else {
                     LimitTracker limit = (LimitTracker)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username);
                     if (limit == null) {
                        log.debug("Creating memcache key for user: " + username);
                        MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, new LimitTracker(this.instanceID, Calendar.getInstance().getTimeInMillis() + timeSlot, data.getCostInBaseCurrency()));
                        return true;
                     } else {
                        log.debug("Found limit amount spent[" + limit.getTotalAmountSpent() + "], expires: " + new Date(limit.getExpires()));
                        if (limit.hasExpired(Calendar.getInstance().getTimeInMillis())) {
                           log.debug("Recreating memcache key for user: " + username);
                           MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, new LimitTracker(this.instanceID, Calendar.getInstance().getTimeInMillis() + timeSlot, data.getCostInBaseCurrency()));
                           return true;
                        } else if (limit.getTotalAmountSpent() + data.getCostInBaseCurrency() > amountLimitPerTimeSlot) {
                           log.debug("Exceeds limit [" + (limit.getTotalAmountSpent() + data.getCostInBaseCurrency()) + "]  for user: " + username);
                           this.sendMessage("You have exceeded your limit for games. Please try again later", username);
                           return false;
                        } else {
                           log.debug("Updating amount[" + (limit.getTotalAmountSpent() + data.getCostInBaseCurrency()) + "] in memcache key for user: " + username);
                           limit.add(this.instanceID, data.getCostInBaseCurrency());
                           MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, limit);
                           return true;
                        }
                     }
                  }
               } else {
                  return data.isEligible();
               }
            } else {
               if (!data.isEligible()) {
                  this.sendMessage("You do not have sufficient credit to start a game", username);
               }

               return data.isEligible();
            }
         }
      } catch (Exception var14) {
         log.error(this.getLogMessage("Unable to check balance for user [" + username + "]."), var14);
         this.sendMessage("You do not have sufficient credit to start a game", username);
         return false;
      }
   }

   protected void revertLimitInCache(Collection<String> players) {
      if (players != null) {
         Iterator i$ = players.iterator();

         while(i$.hasNext()) {
            String username = (String)i$.next();
            this.revertLimitInCache(username);
         }
      }

   }

   protected void revertLimitInCache(String username) {
      if (username != null) {
         LimitTracker limit = (LimitTracker)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username);
         if (limit != null && !limit.hasExpired(Calendar.getInstance().getTimeInMillis())) {
            double amount = limit.revert(this.instanceID);
            log.debug("Reverting " + amount + " off limit for user: " + username);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, limit);
         }
      }

   }

   protected String getStringParameter(String parameterName, String defaultValue) {
      return this.gameParameters.containsKey(parameterName) ? (String)this.gameParameters.get(parameterName) : defaultValue;
   }

   protected int getIntParameter(String parameterName, int defaultValue) {
      try {
         return Integer.parseInt((String)this.gameParameters.get(parameterName));
      } catch (Exception var4) {
         return defaultValue;
      }
   }

   protected long getLongParameter(String parameterName, long defaultValue) {
      try {
         return Long.parseLong((String)this.gameParameters.get(parameterName));
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   protected double getDoubleParameter(String parameterName, double defaultValue) {
      try {
         return Double.parseDouble((String)this.gameParameters.get(parameterName));
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   protected boolean getBooleanParameter(String parameterName, boolean defaultValue) {
      try {
         return ((String)this.gameParameters.get(parameterName)).equalsIgnoreCase("on");
      } catch (Exception var4) {
         return defaultValue;
      }
   }

   public String getInstanceID() {
      return this.instanceID;
   }

   public BotInstance getInstance() {
      BotInstance instance = new BotInstance();
      instance.id = this.instanceID;
      instance.type = this.botData.getType();
      instance.displayName = this.botData.getDisplayName();
      instance.description = this.botData.getDescription();
      instance.startedBy = this.botStarter;
      return instance;
   }

   public BotData getBotData() {
      return this.botData;
   }

   public BotChannelPrx getChannelProxy() {
      return this.channelProxy;
   }

   public String[] getEmoticonHotKeys() {
      return this.emoticonHotKeys;
   }

   public boolean purgeIfIdle() {
      return this.purgeIfIdle;
   }

   public void setPurgeIfIdle(boolean purgeIfIdle) {
      this.purgeIfIdle = purgeIfIdle;
   }

   protected void incrementGamesPlayed(Leaderboard.Type leaderboardType, Set<String> playerSet) {
      try {
         if (SystemProperty.getBool("GamesLeaderboardEnabled", true)) {
            List<String> players = new ArrayList(playerSet.size());
            players.addAll(playerSet);
            Leaderboard.recordGamesMetric(leaderboardType, players);
         }
      } catch (Exception var4) {
         log.error(this.getLogMessage("Unexpected exception occured in incrementGamesPlayed(), " + leaderboardType), var4);
      }

   }

   protected void incrementGamesPlayed(Leaderboard.Type leaderboardType, List<String> playerUsernames, List<Integer> playerUserids) {
      try {
         if (SystemProperty.getBool("GamesLeaderboardEnabled", true)) {
            Leaderboard.recordGamesMetric(leaderboardType, playerUsernames, playerUserids);
         }
      } catch (Exception var5) {
         log.error(this.getLogMessage("Unexpected exception occured in incrementGamesPlayed(), " + leaderboardType), var5);
      }

   }

   protected void incrementMostWins(Leaderboard.Type leaderboardType, String winner) {
      try {
         if (SystemProperty.getBool("GamesLeaderboardEnabled", true)) {
            List<String> won_players = new ArrayList(1);
            won_players.add(winner);
            Leaderboard.recordGamesMetric(leaderboardType, won_players);
         }
      } catch (Exception var4) {
         log.error(this.getLogMessage("Unexpected exception occured in incrementMostWins() " + leaderboardType), var4);
      }

   }

   protected void incrementMostWins(Leaderboard.Type leaderboardType, String winner, int winnerUserId) {
      try {
         if (SystemProperty.getBool("GamesLeaderboardEnabled", true)) {
            List<String> won_players = new ArrayList(1);
            won_players.add(winner);
            List<Integer> won_player_ids = new ArrayList(1);
            won_player_ids.add(winnerUserId);
            Leaderboard.recordGamesMetric(leaderboardType, won_players, won_player_ids);
         }
      } catch (Exception var6) {
         log.error(this.getLogMessage("Unexpected exception occured in incrementMostWins() " + leaderboardType), var6);
      }

   }

   protected void logMostWins(String winner, double amt) {
      logMetrics.info("Game ended in " + this.channel + this.formatMostWinsMetrics(winner, amt));
   }

   protected void logGamesPlayed(int numOfPlayers, Set<String> players, double amt) {
      logMetrics.info("New game started in " + this.channel + this.formatGamesPlayedMetrics(numOfPlayers, players, amt));
   }

   protected String formatMostWinsMetrics(String winner, double amt) {
      String metrics = "";
      metrics = metrics + " [WINNERUSERNAME " + winner + "]";
      metrics = metrics + " [PAYAMOUNT " + DECIMAL_FORMAT.format(amt) + "]";
      return metrics;
   }

   protected String formatGamesPlayedMetrics(int numOfPlayers, Set<String> players, double amt) {
      return String.format(" [NUMPLAYERS %d] [PLAYERUSERNAME %s] [JOINAMOUNT %s]", numOfPlayers, StringUtil.join((Collection)players, " "), "" + amt / 100.0D);
   }
}
