package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.slice.FusionException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class Cupid extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Cupid.class));
   private static SecureRandom secureRandom = new SecureRandom();
   private static final String CUPID_VALUE_SEPARATOR = ";;;";
   private static final Pattern CUPID_ACTION_VERB_PATTERN = Pattern.compile("^([^;]+);(.*)$");
   private static final Pattern CUPID_ACTION_PATTERN = Pattern.compile("/cupid[ ]+([^ ].*)[ ]+([a-z0-9._-]+)", 2);
   private static final Pattern CUPID_ACTION_NO_TARGET_PATTERN = Pattern.compile("/cupid[ ]+([^ ].*)", 2);
   private static final String DEFAULT_CUPID_POSITIVE_ACTIONS = StringUtil.join((Object[])(new String[]{"grab;grabs", "kiss;kisses", "wink;winks", "touch;touches", "impress;impresses", "check out;checks out", "invite;invites", "nudge;nudges", "massage;massages", "taste;tastes", "pinch;pinches", "hold;holds", "caress;caresses", "comfort;comforts", "lick;licks", "handcuff;handcuffs", "court;courts", "woo;woos"}), ";;;");
   private static final String DEFAULT_CUPID_NEGATIVE_ACTIONS = StringUtil.join((Object[])(new String[]{"snob;snobs", "shake;shakes", "slap;slaps", "roll;rolls", "shove;shoves", "pull;pulls", "push;pushes", "bite;bites", "bully;bullies", "trick;tricks", "take;takes", "hurt;hurts", "steal;steals", "wrestle;wrestles", "torture;tortures", "blame;blames", "karate chop;karate chops", "taunt;taunts"}), ";;;");
   private static final String DEFAULT_CUPID_POSITIVE_VALUES = StringUtil.join((Object[])(new String[]{"Seems that you are getting along, why don't you make a step closer?", "Hey hey you're so fine, both of you make each other fine!", "Seems that you like it a lot, I wonder what's next!", "Seems that you are getting it on, what's next?", "I can smell flirty here... go on, make more move!", "Compatibility is in the air!", "Mmm... this is where love begins...", "So good to feel love here!", "I like the way both of you like grooving it!", "Both of you make a perfect match!", "Match made in heaven, don't you think?", "What a wonderful feeling that love here is cooking!"}), ";;;");
   private static final String DEFAULT_CUPID_NEGATIVE_VALUES = StringUtil.join((Object[])(new String[]{"Oh no... someone's in trouble. Bad move!", "Where is the match going? Try again?", "Come on, you can do better than that!", "Mis-match? Could you try better?", "Better run fast and try to make a different move!", "Uh-oh, not a good response... better make it up!", "Where is the love?", "I think you should know what love is.", "Playing hard to get?", "Funny how chemistry works. Try something else this time!", "Love is blind. Please be kind.", "You need more love lessons on how to flirt!"}), ";;;");
   private static final long DEFAULT_CUPID_EXPIRY_PERIOD = 40000L;
   private static final long DEFAULT_CUPID_EXPIRY_REMOVAL_PERIOD = 80000L;
   private static final String DEFAULT_CUPID_LATE_RESPONSE_MESSAGE = "Oops, %s's %s to %s is gone! Act faster!";
   private static final String DEFAULT_CUPID_SELF_ACTION_MESSAGE = "Silly you.. You can't %s yourself";
   private static final String DEFAULT_CUPID_HELP_MESSAGE = "**Use \"/cupid [action] [username]\", actions are: %s**";

   private static long getCupidExpiryPeriod() {
      return SystemProperty.getLong("EmoteCupidExpiryPeriod", 40000L);
   }

   private static long getCupidExpiryRemovalPeriod() {
      return SystemProperty.getLong("EmoteCupidExpiryRemovalPeriod", 80000L);
   }

   public Cupid(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   private static Map<String, String> buildActionMap(String actions) {
      Map<String, String> actionMap = new HashMap();
      String[] arr$ = actions.split(";;;");
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String cmd = arr$[i$];
         Matcher m = CUPID_ACTION_VERB_PATTERN.matcher(cmd);
         if (m.matches()) {
            actionMap.put(m.group(1).toLowerCase(), m.group(2));
         } else {
            log.warn(String.format("Unable to parse cupid action '%s' from '%s'", cmd, actions));
         }
      }

      return actionMap;
   }

   protected String[] getHelpMessage(ChatSource chatSource) throws FusionException {
      String cupidPositiveActionString = SystemProperty.get("EmoteCupidPositiveActions", DEFAULT_CUPID_POSITIVE_ACTIONS);
      java.util.List<String> positiveActions = new LinkedList(buildActionMap(cupidPositiveActionString).keySet());
      String cupidNegativeActionString = SystemProperty.get("EmoteCupidNegativeActions", DEFAULT_CUPID_NEGATIVE_ACTIONS);
      java.util.List<String> negativeActions = new LinkedList(buildActionMap(cupidNegativeActionString).keySet());
      int p = (positiveActions.size() + 1) / 2;
      int n = (negativeActions.size() + 1) / 2;
      LinkedList actionsForHelpMsg = new LinkedList();

      while(p > 0 && n > 0) {
         if (p > 0) {
            actionsForHelpMsg.add(positiveActions.remove(secureRandom.nextInt(positiveActions.size())));
            --p;
         }

         if (n > 0) {
            actionsForHelpMsg.add(negativeActions.remove(secureRandom.nextInt(negativeActions.size())));
            --n;
         }
      }

      return new String[]{String.format(SystemProperty.get("EmoteCupidHelp", "**Use \"/cupid [action] [username]\", actions are: %s**"), StringUtil.join((Collection)actionsForHelpMsg, ", "))};
   }

   protected boolean showHelpMessages(String[] helpMessages, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (helpMessages.length == 0) {
         return false;
      } else {
         String[] arr$ = helpMessages;
         int len$ = helpMessages.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String msg = arr$[i$];
            messageData.messageText = msg;
            this.emoteCommandData.updateMessageData(messageData);
            chatSource.getSessionI().sendMessageBackToUserAsEmote(messageData, (String)null);
         }

         return true;
      }
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      if (this.handleHelpCommand(messageData, chatSource)) {
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      } else {
         Cupid.CupidCommand cc = Cupid.CupidCommand.parseCupidCommand(messageData, chatSource);
         if (cc.targetUsername.equals(messageData.source)) {
            messageData.messageText = String.format(this.emoteCommandData.getMessageText(), String.format(SystemProperty.get("EmoteCupidSelfAction", "Silly you.. You can't %s yourself"), cc.actionVerb));
            this.emoteCommandData.updateMessageData(messageData);
            chatSource.getSessionI().sendMessageBackToUserAsEmote(messageData, (String)null);
            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         } else {
            return chatSource.executeEmoteCommandWithState(this.emoteCommandData.getCommandName(), messageData.toIceObject());
         }
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return new Cupid.CupidState();
   }

   private static class CupidState extends EmoteCommandState {
      private Map<String, Cupid.CupidData> dataMap;

      private CupidState() {
         this.dataMap = new HashMap();
      }

      public EmoteCommand.ResultType execute(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         String originalMessageText = messageData.messageText;
         Cupid.CupidCommand cc = Cupid.CupidCommand.parseCupidCommand(messageData, chatSource);
         String senderUsername = messageData.source;
         boolean isResponse = false;
         Cupid.CupidData lateResponse = null;
         boolean isRepeat = false;
         String sendKey = String.format("%s%s%s", senderUsername, ";;;", cc.targetUsername);
         String receiveKey = String.format("%s%s%s", cc.targetUsername, ";;;", senderUsername);
         synchronized(this.dataMap) {
            long curTime = System.currentTimeMillis();
            long expiryPeriod = Cupid.getCupidExpiryPeriod();
            long expiryRemovalPeriod = Cupid.getCupidExpiryRemovalPeriod();
            Cupid.CupidData scd = (Cupid.CupidData)this.dataMap.get(sendKey);
            if (scd != null && !scd.isExpired(expiryPeriod, curTime)) {
               isRepeat = true;
            }

            if (!isRepeat) {
               scd = (Cupid.CupidData)this.dataMap.remove(receiveKey);
               if (scd != null) {
                  if (!scd.isExpired(expiryPeriod, curTime)) {
                     isResponse = true;
                  } else if (!scd.isExpired(expiryRemovalPeriod, curTime)) {
                     lateResponse = scd;
                  }
               } else {
                  this.dataMap.put(sendKey, new Cupid.CupidData(senderUsername, cc.targetUsername, cc.actionVerb));
               }
            }
         }

         if (isRepeat) {
            throw new FusionException(String.format("You can only use /cupid to the same user every %d seconds", Cupid.getCupidExpiryPeriod() / 1000L));
         } else {
            String cupidValueString;
            if (lateResponse != null) {
               cupidValueString = SystemProperty.get("EmoteCupidLateResponse", "Oops, %s's %s to %s is gone! Act faster!");
               messageData.messageText = String.format(emoteCommandData.getMessageText(), String.format(cupidValueString, lateResponse.sender, lateResponse.action, lateResponse.recipient));
               emoteCommandData.updateMessageData(messageData);
               Cupid.log.info(String.format("cupid late response '%s %s %s' to %s", messageData.source, cc.action, cc.targetUsername, lateResponse));
               chatSource.sendMessageToAllUsersInChat(messageData);
            } else {
               messageData.messageText = String.format("**%s %s %s**", messageData.source, cc.action, cc.targetUsername);
               emoteCommandData.updateMessageData(messageData);
               Cupid.log.info(String.format("cupid action %s: %s", isResponse ? "responded" : "initiated", messageData.messageText));
               chatSource.sendMessageToAllUsersInChat(messageData);
               if (isResponse) {
                  if (cc.isActionPositive) {
                     cupidValueString = SystemProperty.get("EmoteCupidPositiveValues", Cupid.DEFAULT_CUPID_POSITIVE_VALUES);
                  } else {
                     cupidValueString = SystemProperty.get("EmoteCupidNegativeValues", Cupid.DEFAULT_CUPID_NEGATIVE_VALUES);
                  }

                  String[] values = cupidValueString.split(";;;");
                  if (values.length == 0) {
                     Cupid.log.error(String.format("No %s responses for /cupid emote: user=%s, msg=%s", cc.isActionPositive ? "positive" : "negative", senderUsername, originalMessageText));
                     return EmoteCommand.ResultType.NOTHANDLED;
                  }

                  String cupidResponse = values[Cupid.secureRandom.nextInt(values.length)];
                  cupidResponse = String.format("%s %s: %s", senderUsername, cc.targetUsername, cupidResponse);
                  Cupid.log.info(String.format("cupid bot %s response msg %s", cc.isActionPositive ? "positive" : "negative", cupidResponse));
                  messageData.messageText = String.format(emoteCommandData.getMessageText(), cupidResponse);
                  emoteCommandData.updateMessageData(messageData);
                  chatSource.sendMessageToAllUsersInChat(messageData);
               }
            }

            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }

      public void cleanUp() {
         long curTime = System.currentTimeMillis();
         long expiryRemovalPeriod = Cupid.getCupidExpiryRemovalPeriod();
         synchronized(this.dataMap) {
            Iterator iter = this.dataMap.entrySet().iterator();

            while(iter.hasNext()) {
               Entry<String, Cupid.CupidData> e = (Entry)iter.next();
               if (((Cupid.CupidData)e.getValue()).isExpired(expiryRemovalPeriod, curTime)) {
                  iter.remove();
                  Cupid.log.info(String.format("removed expired action %s", e.getValue()));
               }
            }

         }
      }

      // $FF: synthetic method
      CupidState(Object x0) {
         this();
      }
   }

   private static class CupidCommand {
      public String actionVerb;
      public String action;
      public String targetUsername;
      public boolean isActionPositive;

      private CupidCommand(String actionVerb, String action, String targetUsername, boolean isActionPositive) {
         this.actionVerb = actionVerb;
         this.action = action;
         this.targetUsername = targetUsername;
         this.isActionPositive = isActionPositive;
      }

      public static Cupid.CupidCommand parseCupidCommand(MessageData messageData, ChatSource chatSource) throws FusionException {
         Matcher m = Cupid.CUPID_ACTION_PATTERN.matcher(messageData.messageText);
         String actionVerb;
         String targetUsername;
         if (!m.matches()) {
            if (chatSource.getChatType() != ChatSource.ChatType.PRIVATE_CHAT) {
               throw new FusionException("Usage: /cupid [action] [username]");
            }

            m = Cupid.CUPID_ACTION_NO_TARGET_PATTERN.matcher(messageData.messageText);
            if (!m.matches()) {
               throw new FusionException("Usage: /cupid [action]");
            }

            actionVerb = m.group(1).toLowerCase();
            targetUsername = ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
         } else {
            actionVerb = m.group(1).toLowerCase();
            targetUsername = m.group(2).toLowerCase();
            if (chatSource.getChatType() == ChatSource.ChatType.PRIVATE_CHAT && !((MessageDestinationData)messageData.messageDestinations.get(0)).destination.equals(targetUsername)) {
               m = Cupid.CUPID_ACTION_NO_TARGET_PATTERN.matcher(messageData.messageText);
               if (!m.matches()) {
                  throw new FusionException("Usage: /cupid [action]");
               }

               actionVerb = m.group(1).toLowerCase();
               targetUsername = ((MessageDestinationData)messageData.messageDestinations.get(0)).destination;
            }
         }

         String cupidPositiveActionString = SystemProperty.get("EmoteCupidPositiveActions", Cupid.DEFAULT_CUPID_POSITIVE_ACTIONS);
         Map<String, String> actionMap = Cupid.buildActionMap(cupidPositiveActionString);
         boolean actionPositive = true;
         String action = (String)actionMap.get(actionVerb);
         if (StringUtil.isBlank(action)) {
            String cupidNegativeActionString = SystemProperty.get("EmoteCupidNegativeActions", Cupid.DEFAULT_CUPID_NEGATIVE_ACTIONS);
            actionMap = Cupid.buildActionMap(cupidNegativeActionString);
            action = (String)actionMap.get(actionVerb);
            if (StringUtil.isBlank(action)) {
               throw new FusionException(String.format("Cupid action '%s' is not supported", actionVerb));
            }

            actionPositive = false;
         }

         if (!chatSource.isUserVisibleInChat(targetUsername)) {
            throw new FusionException(targetUsername + " is not in the chat");
         } else {
            return new Cupid.CupidCommand(actionVerb, action, targetUsername, actionPositive);
         }
      }
   }

   private static class CupidData {
      public String sender;
      public String recipient;
      public String action;
      public long actionTime;

      CupidData(String senderUsername, String recipientUsername, String action) {
         this.sender = senderUsername;
         this.recipient = recipientUsername;
         this.action = action;
         this.actionTime = System.currentTimeMillis();
      }

      boolean isExpired() {
         return this.isExpired(Cupid.getCupidExpiryPeriod());
      }

      boolean isExpired(long expiryPeriod) {
         return this.isExpired(expiryPeriod, System.currentTimeMillis());
      }

      boolean isExpired(long expiryPeriod, long currentTimeMillis) {
         return currentTimeMillis - this.actionTime > expiryPeriod;
      }

      boolean isRemovalExpired() {
         return this.isExpired(Cupid.getCupidExpiryRemovalPeriod());
      }

      public String toString() {
         return String.format("[%s %s %s at %s]", this.sender, this.action, this.recipient, new Date(this.actionTime));
      }
   }
}
