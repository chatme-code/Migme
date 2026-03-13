package com.projectgoth.fusion.botservice.bot.migbot.chatterbot;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection.CornyJokeIntros;
import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection.CornyJokes;
import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection.DefaultBoyFriendBotResponses;
import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection.PickupLineIntros;
import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection.PickupLines;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class BoyFriend extends Chatterbot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BoyFriend.class));
   DecimalFormat df = new DecimalFormat("0.00");
   Map<String, TextCollection> textMap = new HashMap();
   Map<String, TextCollection> introMap = new HashMap();
   TextCollection responses = new DefaultBoyFriendBotResponses();
   private static final String COMMAND_CORNY_JOKES = "!c";
   private static final String COMMAND_PICKUP_LINES = "!p";
   public static final String PREMIUM_COMMAND_COST = "premiumCommandCost";
   public static final String TIME_BETWEEN_PLAY_NOW_MESSAGES = "timeBetweenPlayNowMessages";
   private double premiumCommandCost = 0.02D;
   private long timeBetweenPlayNowMessages = 150000L;
   private ScheduledFuture playNowTimer;

   public BoyFriend(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDAO);
      this.loadGameConfig();
      this.textMap.put("!c", new CornyJokes());
      this.textMap.put("!p", new PickupLines());
      this.introMap.put("!c", new CornyJokeIntros());
      this.introMap.put("!p", new PickupLineIntros());
      this.sendChannelMessage(this.createMessage("GREETING_MESSAGE", (String)null));
      this.playNowTimer = executor.scheduleWithFixedDelay(new Runnable() {
         public void run() {
            synchronized(BoyFriend.this) {
               long now = System.currentTimeMillis();
               if (now - BoyFriend.this.timeLastUserJoined >= BoyFriend.this.timeBetweenPlayNowMessages && now - BoyFriend.this.timeLastMessageReceived >= BoyFriend.this.timeBetweenPlayNowMessages) {
                  BoyFriend.this.sendChannelMessage(BoyFriend.this.createMessage("PLAY_NOW", (String)null));
               }
            }
         }
      }, 0L, this.timeBetweenPlayNowMessages, TimeUnit.MILLISECONDS);
   }

   private void loadGameConfig() {
      this.premiumCommandCost = this.getDoubleParameter("premiumCommandCost", this.premiumCommandCost);
      this.timeBetweenPlayNowMessages = this.getLongParameter("timeBetweenPlayNowMessages", this.timeBetweenPlayNowMessages);
   }

   public void onUserJoinChannel(String username) {
      super.onUserJoinChannel(username);
      this.sendMessage(this.createMessage("WELCOME_MESSAGE", username), username);
   }

   public synchronized void stopBot() {
      super.stopBot();
      if (this.playNowTimer != null) {
         this.playNowTimer.cancel(true);
         this.playNowTimer = null;
      }

   }

   protected String createMessage(String messageKey, String player) {
      try {
         String messageToSend = (String)this.messages.get(messageKey);
         if (messageToSend == null) {
            messageToSend = messageKey;
         }

         if (player != null) {
            messageToSend = messageToSend.replace("PLAYER", player);
         }

         messageToSend = messageToSend.replace("BOT_NAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replace("CURRENCY", "USD");
         messageToSend = messageToSend.replace("COMMAND_1", "!p");
         messageToSend = messageToSend.replace("COMMAND_2", "!c");
         messageToSend = messageToSend.replace("PREMIUM_AMOUNT", this.df.format(this.premiumCommandCost));
         return messageToSend;
      } catch (NullPointerException var4) {
         log.error("Outgoing message could not be created, key = " + messageKey, var4);
         return "";
      }
   }

   public void onMessage(String username, String messageText, long receivedTimestamp) {
      TextCollection tc = (TextCollection)this.textMap.get(messageText);
      TextCollection intro = (TextCollection)this.introMap.get(messageText);
      if (tc != null && intro != null) {
         try {
            if (!this.userCanAffordItem(username, this.premiumCommandCost)) {
               return;
            }

            Text text = tc.getNextText();
            Text response = intro.getNextText();
            String message = response.getContent();
            message = message.replace("USERNAME", username);
            message = message.replace("TEXT", text.getContent());
            this.sendChannelMessage(message);
            this.chargeUserForItem(username, "boyfriend." + tc.getCode(), this.premiumCommandCost, "Purchase of " + tc.getDisplayName() + " from " + this.botData.getDisplayName());
         } catch (Exception var10) {
            log.error("Unexpected exception username [" + username + "] cost[" + this.premiumCommandCost + "USD" + "] messageText[" + messageText + "] :" + var10.getMessage(), var10);
            this.sendMessage("I'm sorry I'm not feeling well right now. Please visit me another time.", username);
         }

         this.timeLastMessageReceived = System.currentTimeMillis();
      } else {
         super.onMessage(username, messageText, receivedTimestamp);
      }
   }
}
