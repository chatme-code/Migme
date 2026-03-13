package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class CrystalBall extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CrystalBall.class));
   private static final long DEFAULT_RETRY_COOLDOWN_PERIOD = 60000L;
   private static final long DEFAULT_CONFIRMATION_WAITTIME = 10000L;
   private static final long DEFAULT_GENERATION_WAITTIME = 1000L;
   private static final long DEFAULT_ANSWER_WAITTIME = 3000L;
   private static final Pattern CONFIRMATION_PATTERN = Pattern.compile("^\\s*(.+)\\s+\\*[yY]\\s*$");
   private static final String DEFAULT_GENERATION_TEXTS = EmoteCommandUtils.createRandomTextCandidateString(new String[]{"The fog is lifting...", "The spirits are speaking to me...", "I'm seeing something...", "It's materialising...", "The answer's on its way...", "The spirits are present...", "Foretelling...", "Psychic powers working...", "Electricity is pulsing...", "Meditating for you...", "The answer's becoming clear...", "Powerful forces working...", "Feeling the warmth...", "Spirit wheel spinning...", "Ohmmmm, Ohmmmm...", "Metaphysical powers flowing...", "I see... I see... something's coming...", "The orb is glowing...", "Strange light from within...", "Smoke swirling within...", "The crystal pulses with eerie light...", "The fogginess is fading...", "Ghostly vapours swirling...", "Spirits are clearing the vale...", "Shapes appearing in the mist...", "I fear to watch, yet I can't turn away...", "The orb glows hotter...", "Hissing sounds from all around. The spirits are speaking...", "Oh! Amazing light! It's becoming clear...", "The horrible, black void grows brighter...", "Lightning flashes within! Mists dissipating...", "Arcane knowledge flowing and swirling...", "Let me see.. hmmm..."});
   private static final String DEFAULT_ANSWER_TEXTS = EmoteCommandUtils.createRandomTextCandidateString(new String[]{"As I see it, yes.", "It is certain.", "It is decidedly so.", "Most likely.", "Outlook good.", "Signs point to yes.", "Without a doubt.", "Yes.", "Yes - definitely.", "You may rely on it.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.", "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful.", "Probably not.", "Highly unlikely.", "Forget about it!", "No way!", "Chances are slim."});

   public CrystalBall(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return new CrystalBall.CrystalBallState();
   }

   private static CrystalBall.CommandData parseCommand(MessageData messageData, ChatSource chatSource) {
      String[] args = messageData.messageText.split("\\s+", 2);
      if (args.length >= 2 && !StringUtil.isBlank(args[1])) {
         CrystalBall.CommandData cd = new CrystalBall.CommandData();
         Matcher m = CONFIRMATION_PATTERN.matcher(args[1]);
         if (m.matches()) {
            cd.question = m.group(1);
            cd.withConfirmation = true;
         } else {
            cd.question = args[1];
            if ("y".equalsIgnoreCase(cd.question)) {
               cd.isAnswerYes = true;
               cd.isQuestion = false;
            } else if ("n".equalsIgnoreCase(cd.question)) {
               cd.isAnswerYes = false;
               cd.isQuestion = false;
            }
         }

         return cd;
      } else {
         return null;
      }
   }

   protected String[] getHelpMessage(ChatSource chatSource) throws FusionException {
      return new String[]{"Usage: /cb [a yes/no question]"};
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      return this.handleHelpCommand(messageData, chatSource) ? EmoteCommand.ResultType.HANDLED_AND_STOP : chatSource.executeEmoteCommandWithState(this.emoteCommandData.getCommandName(), messageData.toIceObject());
   }

   private static class CrystalBallState extends EmoteCommandState {
      CrystalBall.CrystalBallState.CBState currentState;
      String currentUsername;
      String currentQuestion;
      long currentAskedTimestamp;
      private Map<String, Long> dataMap;
      private Timer cbTimer;

      private CrystalBallState() {
         this.currentState = CrystalBall.CrystalBallState.CBState.IDLE;
         this.currentUsername = null;
         this.currentQuestion = null;
         this.currentAskedTimestamp = 0L;
         this.dataMap = new HashMap();
      }

      private void intoCoolDown() {
         this.currentState = CrystalBall.CrystalBallState.CBState.IDLE;
         CrystalBall.log.debug(String.format("Adding user '%s' to cool down, start '%s', expiring '%s'", this.currentUsername, new Date(this.currentAskedTimestamp + 10000L), new Date(this.currentAskedTimestamp + 10000L + 60000L)));
         this.dataMap.put(this.currentUsername, this.currentAskedTimestamp + 10000L);
      }

      private CrystalBall.CrystalBallState.MessageInfo tryEnterAskedState(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
         boolean sufficientCredit = true;
         String sender = messageData.source;
         CrystalBall.CrystalBallState.MessageInfo mi = null;

         try {
            Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
            SessionPrx sessionPrx = chatSource.getSessionPrx();
            sufficientCredit = contentBean.buyPaidEmote(sender, new PaidEmoteData(emoteCommandData), chatSource.getEmotePurchaseLocation().value(), new AccountEntrySourceData(sessionPrx.getRemoteIPAddress(), sessionPrx.getSessionID(), sessionPrx.getMobileDeviceIce(), sessionPrx.getUserAgentIce()));
            if (CrystalBall.log.isDebugEnabled()) {
               CrystalBall.log.debug(String.format("User [%s] charged for Crystal Ball, amount [%s]", sender, emoteCommandData.getPriceWithCurrency()));
            }
         } catch (CreateException var9) {
            CrystalBall.log.error(String.format("Unable to create EJB to check whether user '%s' could afford to play Crystal Ball, q='%s'", sender, this.currentQuestion), var9);
            mi = this.formatPrivateMessage(String.format("Unable to check your migCredits at the moment. Please try again later."), emoteCommandData);
         } catch (RemoteException var10) {
            CrystalBall.log.error(String.format("Unable to check whether user '%s' could afford to play Crystal Ball, q='%s'", sender, this.currentQuestion), var10);
            mi = this.formatPrivateMessage(String.format("Unable to check your migCredits at the moment. Please try again later."), emoteCommandData);
         } catch (EJBException var11) {
            CrystalBall.log.error(String.format("Unable to deduct migCredits for user '%s' to buy Crystal Ball emote, q='%s'", sender, this.currentQuestion), var11);
            mi = this.formatPrivateMessage(String.format("Unable to deduct your migCredits at the moment. Please try again later."), emoteCommandData);
         }

         if (mi == null) {
            if (!sufficientCredit) {
               mi = this.formatPrivateMessage(String.format("Insufficient migCredits. Please get more and try again."), emoteCommandData);
               this.intoCoolDown();
            } else {
               this.currentState = CrystalBall.CrystalBallState.CBState.ASKED;
               mi = this.formatAskMessage(this.currentQuestion, this.currentUsername, emoteCommandData);
               if (this.cbTimer == null) {
                  this.cbTimer = new Timer("Crystal Ball timer", true);
               }

               String text = EmoteCommandUtils.getRandomText(CrystalBall.DEFAULT_GENERATION_TEXTS);
               CrystalBall.CrystalBallState.SendGenerationTextTask task = new CrystalBall.CrystalBallState.SendGenerationTextTask(this.formatNormalMessage(text, emoteCommandData).message, emoteCommandData, messageData, chatSource);
               this.cbTimer.schedule(task, 1000L);
            }
         }

         return mi;
      }

      private CrystalBall.CrystalBallState.MessageInfo formatNormalMessage(String message, EmoteCommandData emoteCommandData) {
         return new CrystalBall.CrystalBallState.MessageInfo(String.format(emoteCommandData.getMessageText(), "", "", message), false, false, (String)null);
      }

      private CrystalBall.CrystalBallState.MessageInfo formatPrivateMessage(String message, EmoteCommandData emoteCommandData) {
         String msgText = emoteCommandData.getMessageText();
         return new CrystalBall.CrystalBallState.MessageInfo(String.format(msgText.substring(2, msgText.length() - 2), "", "", "[PVT] " + message));
      }

      private boolean isPrivateMessage(String message) {
         return !message.startsWith("**") && message.contains("[PVT]");
      }

      private CrystalBall.CrystalBallState.MessageInfo formatAskMessage(String message, String username, EmoteCommandData emoteCommandData) {
         return new CrystalBall.CrystalBallState.MessageInfo(String.format(emoteCommandData.getMessageText(), username + " asked ", "", "\"%s?\""), false, true, message);
      }

      private CrystalBall.CrystalBallState.MessageInfo formatAnswerMessage(String message, String username, EmoteCommandData emoteCommandData) {
         return new CrystalBall.CrystalBallState.MessageInfo(String.format(emoteCommandData.getMessageText(), "", " to " + username, message), false, false, (String)null);
      }

      public EmoteCommand.ResultType execute(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         CrystalBall.CommandData cd = CrystalBall.parseCommand(messageData, chatSource);
         String sender = messageData.source;
         CrystalBall.CrystalBallState.MessageInfo messageInfo = null;
         synchronized(this.dataMap) {
            long curTime = System.currentTimeMillis();
            boolean expiredWaiting = this.currentState == CrystalBall.CrystalBallState.CBState.WAITING && curTime - this.currentAskedTimestamp > 10000L;
            if (this.currentState != CrystalBall.CrystalBallState.CBState.IDLE && !expiredWaiting && !sender.equals(this.currentUsername)) {
               messageInfo = this.formatPrivateMessage(String.format("%s is consulting the Crystal Ball now. Please ask again in a moment.", this.currentUsername), emoteCommandData);
            } else {
               if (expiredWaiting) {
                  this.intoCoolDown();
               }

               switch(this.currentState) {
               case IDLE:
                  if (this.dataMap.containsKey(sender) && curTime - (Long)this.dataMap.get(sender) < 60000L) {
                     messageInfo = this.formatPrivateMessage(String.format("Please wait %d seconds before consulting the Crystal Ball again.", 60L), emoteCommandData);
                  } else if (cd.isQuestion) {
                     this.currentUsername = sender;
                     this.currentQuestion = cd.question;
                     this.currentAskedTimestamp = curTime;
                     if (cd.withConfirmation) {
                        messageInfo = this.tryEnterAskedState(emoteCommandData, messageData, chatSource);
                     } else {
                        messageInfo = this.formatPrivateMessage(String.format("Consulting the Crystal Ball: %s. Continue? Send /cb y to continue and /cb n to stop. %d seconds...", emoteCommandData.getPriceWithCurrency(), 10L), emoteCommandData);
                        this.currentState = CrystalBall.CrystalBallState.CBState.WAITING;
                        if (this.cbTimer == null) {
                           this.cbTimer = new Timer("Crystal Ball timer", true);
                        }

                        String text = this.formatPrivateMessage("Timed out! Crystal Ball cancelled. No migCredits were deducted.", emoteCommandData).message;
                        CrystalBall.CrystalBallState.SendTimeoutTextTask task = new CrystalBall.CrystalBallState.SendTimeoutTextTask(this.currentUsername, this.currentAskedTimestamp, text, emoteCommandData, messageData, chatSource);
                        this.cbTimer.schedule(task, 10100L);
                     }
                  } else {
                     messageInfo = this.formatPrivateMessage(String.format("Please enter the question first before answering /cb y or /cb n to proceed."), emoteCommandData);
                  }
                  break;
               case WAITING:
                  if (cd.isQuestion) {
                     messageInfo = this.formatPrivateMessage(String.format("Please send /cb y to continue and /cb n to stop."), emoteCommandData);
                  } else if (cd.isAnswerYes) {
                     messageInfo = this.tryEnterAskedState(emoteCommandData, messageData, chatSource);
                  } else {
                     this.intoCoolDown();
                     messageInfo = this.formatPrivateMessage("Crystal Ball cancelled. No migCredits were deducted.", emoteCommandData);
                  }
                  break;
               case ASKED:
               case MUMBLED:
                  CrystalBall.log.error("Should not reach this part");
               }
            }
         }

         if (messageInfo == null) {
            CrystalBall.log.error(String.format("message is null after processing /cb. something is wrong. curState='%s', curUser='%s', curQ='%s', curAskedTS='%s'", this.currentState.name(), this.currentUsername, this.currentQuestion, new Date(this.currentAskedTimestamp)));
            throw new FusionException("Unable to process the request");
         } else {
            messageData.messageText = messageInfo.message;
            emoteCommandData.updateMessageData(messageData);
            if (messageInfo.isPrivate) {
               chatSource.sendMessageToSender(messageData);
            } else if (messageInfo.requireTruncation) {
               chatSource.sendMessageWithTruncationToAllUsersInChat(messageData, messageInfo.subMessageToTruncate);
            } else {
               chatSource.sendMessageToAllUsersInChat(messageData);
            }

            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }

      public void cleanUp() {
         long curTime = System.currentTimeMillis();
         synchronized(this.dataMap) {
            Iterator iter = this.dataMap.entrySet().iterator();

            while(iter.hasNext()) {
               Entry<String, Long> e = (Entry)iter.next();
               if (curTime - (Long)e.getValue() > 60000L) {
                  iter.remove();
                  CrystalBall.log.info(String.format("removed expired cool down record %s, %d", e.getKey(), e.getValue()));
               }
            }

         }
      }

      // $FF: synthetic method
      CrystalBallState(Object x0) {
         this();
      }

      private class MessageInfo {
         String message = null;
         boolean isPrivate = true;
         boolean requireTruncation = false;
         String subMessageToTruncate = null;

         MessageInfo() {
         }

         MessageInfo(String message) {
            this.message = message;
         }

         MessageInfo(String message, boolean isPrivate, boolean requireTruncation, String subMessage) {
            this.message = message;
            this.isPrivate = isPrivate;
            this.requireTruncation = requireTruncation;
            this.subMessageToTruncate = subMessage;
         }
      }

      private class SendAnswerTextTask extends TimerTask {
         MessageData messageData;
         ChatSource chatSource;

         SendAnswerTextTask(String message, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
            this.messageData = new MessageData(messageData.toIceObject());
            emoteCommandData.updateMessageData(this.messageData);
            this.messageData.messageText = message;
            this.chatSource = chatSource;
         }

         public void run() {
            try {
               this.chatSource.sendMessageToAllUsersInChat(this.messageData);
            } catch (FusionException var12) {
               CrystalBall.log.error(String.format("Failed to broadcast message to chatSource, msg='%s'", this.messageData), var12);
            } finally {
               synchronized(CrystalBallState.this.dataMap) {
                  CrystalBallState.this.currentState = CrystalBall.CrystalBallState.CBState.IDLE;
               }
            }

         }
      }

      private class SendGenerationTextTask extends TimerTask {
         MessageData messageData;
         ChatSource chatSource;
         EmoteCommandData emoteCommandData;

         SendGenerationTextTask(String message, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
            this.messageData = new MessageData(messageData.toIceObject());
            this.messageData.messageText = message;
            this.chatSource = chatSource;
            this.emoteCommandData = emoteCommandData;
            emoteCommandData.updateMessageData(this.messageData);
         }

         public void run() {
            try {
               this.chatSource.sendMessageToAllUsersInChat(this.messageData);
            } catch (FusionException var14) {
               CrystalBall.log.error(String.format("Failed to broadcast message to chatSource, msg='%s'", this.messageData), var14);
            } finally {
               synchronized(CrystalBallState.this.dataMap) {
                  CrystalBallState.this.currentState = CrystalBall.CrystalBallState.CBState.MUMBLED;
                  CrystalBall.CrystalBallState.MessageInfo mi = CrystalBallState.this.formatAnswerMessage(EmoteCommandUtils.getRandomText(CrystalBall.DEFAULT_ANSWER_TEXTS), this.messageData.source, this.emoteCommandData);
                  CrystalBall.CrystalBallState.SendAnswerTextTask task = CrystalBallState.this.new SendAnswerTextTask(mi.message, this.emoteCommandData, this.messageData, this.chatSource);
                  CrystalBallState.this.cbTimer.schedule(task, 3000L);
               }
            }

         }
      }

      private class SendTimeoutTextTask extends TimerTask {
         String originalSender;
         long originalAskedTimestamp;
         MessageData messageData;
         ChatSource chatSource;

         SendTimeoutTextTask(String sender, long askedTimestamp, String message, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
            this.originalAskedTimestamp = askedTimestamp;
            this.originalSender = sender;
            this.messageData = new MessageData(messageData.toIceObject());
            emoteCommandData.updateMessageData(this.messageData);
            this.messageData.messageText = message;
            this.chatSource = chatSource;
         }

         public void run() {
            long curTime = System.currentTimeMillis();
            synchronized(CrystalBallState.this.dataMap) {
               if (CrystalBallState.this.currentState != CrystalBall.CrystalBallState.CBState.WAITING || this.originalAskedTimestamp != CrystalBallState.this.currentAskedTimestamp || !this.originalSender.equals(CrystalBallState.this.currentUsername)) {
                  return;
               }

               CrystalBallState.this.intoCoolDown();
            }

            try {
               this.chatSource.sendMessageToSender(this.messageData);
            } catch (FusionException var5) {
               CrystalBall.log.error(String.format("Failed to send message to sender, msg='%s'", this.messageData), var5);
            }

         }
      }

      private static enum CBState {
         IDLE,
         WAITING,
         ASKED,
         MUMBLED;
      }
   }

   private static class CommandData {
      String question;
      boolean isQuestion;
      boolean isAnswerYes;
      boolean withConfirmation;

      private CommandData() {
         this.isQuestion = true;
         this.isAnswerYes = false;
         this.withConfirmation = false;
      }

      // $FF: synthetic method
      CommandData(Object x0) {
         this();
      }
   }
}
