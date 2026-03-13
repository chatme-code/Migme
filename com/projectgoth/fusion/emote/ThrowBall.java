package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class ThrowBall extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ThrowBall.class));
   private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
   private static final Pattern THROWBALL_COMMAND_PATTERN = Pattern.compile("/throwball(?:\\s+([a-z0-9._-]+))?", 2);
   private static final Pattern STEALBALL_COMMAND_PATTERN = Pattern.compile("/stealball\\s+([a-z0-9._-]+)", 2);

   public ThrowBall(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return new ThrowBall.ThrowBallState();
   }

   private static String parseThrowballCommand(MessageData messageData, ChatSource chatSource, boolean pickTarget) throws FusionException {
      Matcher m = THROWBALL_COMMAND_PATTERN.matcher(messageData.messageText);
      if (!m.matches()) {
         throw new FusionException("Usage: /throwball [optional-username]");
      } else {
         String target = m.group(1);
         if (pickTarget && target == null) {
            String[] allUsers = chatSource.getVisibleUsernamesInChat(false);
            if (allUsers.length == 0) {
               throw new FusionException("There are no other users in the chat");
            }

            target = allUsers[RANDOM_GENERATOR.nextInt(allUsers.length)];
         }

         if (target != null) {
            target = target.toLowerCase();
            if (target.equals(messageData.source.toLowerCase())) {
               throw new FusionException("You can not throw the ball to yourself");
            }

            if (!chatSource.isUserVisibleInChat(target)) {
               throw new FusionException(target + " is not in the chat");
            }
         }

         return target;
      }
   }

   private static String parseStealballCommand(MessageData messageData, ChatSource chatSource) throws FusionException {
      Matcher m = STEALBALL_COMMAND_PATTERN.matcher(messageData.messageText);
      if (!m.matches()) {
         throw new FusionException("Usage: /stealball [username]");
      } else {
         String target = m.group(1).toLowerCase();
         if (!chatSource.isUserInChat(target)) {
            throw new FusionException(target + " is not in the chat");
         } else {
            return target;
         }
      }
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.messageText.toLowerCase().split(" ", 2);
      String command = args[0].substring(1);
      if ("throwball".equals(command)) {
         parseThrowballCommand(messageData, chatSource, false);
      } else if (!"catchball".equals(command)) {
         if (!"stealball".equals(command)) {
            return null;
         }

         parseStealballCommand(messageData, chatSource);
      }

      return chatSource.executeEmoteCommandWithState(this.emoteCommandData.getCommandName(), messageData.toIceObject());
   }

   public static class ThrowBallState extends EmoteCommandState {
      ThrowBall.ThrowBallState.BallState currentState;
      String userThrewBall;
      String userThrewAtBall;
      String userWithBall;
      long lastStateChangeTime;
      boolean ballCaught;
      private Map<String, ThrowBall.BallData> dataMap;
      private static final long DEFAULT_THROWBALL_WAITTIME = 40000L;
      private static final long DEFAULT_CATCHBALL_WAITTIME = 40000L;
      private static final long DEFAULT_CATCHBALL_EXPIRE_TIME = 80000L;

      public ThrowBallState() {
         this.currentState = ThrowBall.ThrowBallState.BallState.IDLE;
         this.lastStateChangeTime = 0L;
         this.dataMap = new HashMap();
      }

      private void changeState(ThrowBall.ThrowBallState.BallState newState, String thrower, String target, String holder, ThrowBall.ThrowBallState.BallCommand ballCmd) {
         this.currentState = newState;
         this.userThrewBall = thrower;
         this.userThrewAtBall = target;
         this.userWithBall = holder;
         this.lastStateChangeTime = System.currentTimeMillis();
         if (newState != ThrowBall.ThrowBallState.BallState.IDLE) {
            if (ballCmd == ThrowBall.ThrowBallState.BallCommand.CATCH) {
               this.dataMap.remove(this.userThrewAtBall);
            } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.THROW) {
               this.dataMap.put(this.userThrewAtBall, new ThrowBall.BallData(this.userThrewBall, this.lastStateChangeTime));
               this.dataMap.remove(this.userThrewBall);
            } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.STEAL) {
               this.dataMap.remove(this.userWithBall);
            }
         }

      }

      public void cleanUp() {
         long curTime = System.currentTimeMillis();
         synchronized(this.dataMap) {
            Iterator iter = this.dataMap.entrySet().iterator();

            while(iter.hasNext()) {
               Entry<String, ThrowBall.BallData> e = (Entry)iter.next();
               if (curTime - ((ThrowBall.BallData)e.getValue()).timestamp > 80000L) {
                  iter.remove();
                  ThrowBall.log.info(String.format("removed expired action %s", e.getValue()));
               }
            }

         }
      }

      public EmoteCommand.ResultType execute(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         String[] args = messageData.messageText.toLowerCase().split(" ", 2);
         ThrowBall.ThrowBallState.BallCommand ballCmd = ThrowBall.ThrowBallState.BallCommand.fromString(args[0].substring(1));
         String message = null;
         synchronized(this.dataMap) {
            if (this.currentState == ThrowBall.ThrowBallState.BallState.IDLE) {
               message = this.executeWhenIdle(ballCmd, emoteCommandData, messageData, chatSource);
            } else if (this.currentState == ThrowBall.ThrowBallState.BallState.THROWN) {
               message = this.executeWhenThrown(ballCmd, emoteCommandData, messageData, chatSource);
            } else if (this.currentState == ThrowBall.ThrowBallState.BallState.AT_HAND) {
               message = this.executeWhenAtHand(ballCmd, emoteCommandData, messageData, chatSource);
            }
         }

         if (message == null) {
            return EmoteCommand.ResultType.NOTHANDLED;
         } else {
            messageData.messageText = String.format("**Referee: %s**", message);
            emoteCommandData.updateMessageData(messageData);
            chatSource.sendMessageToAllUsersInChat(messageData);
            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }

      private String executeWhenThrown(ThrowBall.ThrowBallState.BallCommand ballCmd, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         long curTime = System.currentTimeMillis();
         String stealer;
         String target;
         if (ballCmd == ThrowBall.ThrowBallState.BallCommand.THROW) {
            stealer = messageData.source;
            if (curTime - this.lastStateChangeTime <= 40000L) {
               if (stealer.equals(this.userThrewBall)) {
                  return String.format("%s, you've just thrown the ball. Wait for it to be caught.", stealer);
               } else {
                  return stealer.equals(this.userThrewAtBall) ? String.format("%s, the ball has been thrown at you. Catch it now!", stealer) : String.format("%s, the ball has been thrown. Steal it now!", stealer);
               }
            } else {
               target = ThrowBall.parseThrowballCommand(messageData, chatSource, true);
               this.changeState(ThrowBall.ThrowBallState.BallState.THROWN, stealer, target, this.userWithBall, ballCmd);
               return String.format(emoteCommandData.getMessageText(), stealer, target);
            }
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.CATCH) {
            stealer = messageData.source;
            boolean expired = curTime - this.lastStateChangeTime > 40000L;
            if (!expired && stealer.equals(this.userThrewBall)) {
               return String.format("Silly %s, you cannot catch a ball thrown by yourself.", stealer);
            } else if (!expired && stealer.equals(this.userThrewAtBall)) {
               this.changeState(ThrowBall.ThrowBallState.BallState.AT_HAND, this.userThrewBall, stealer, stealer, ballCmd);
               return String.format(emoteCommandData.getMessageText(), stealer);
            } else {
               ThrowBall.BallData bd = (ThrowBall.BallData)this.dataMap.get(stealer);
               boolean catchLastThrownExpired = bd != null && curTime - bd.timestamp <= 80000L;
               if (stealer.equals(this.userThrewAtBall) && catchLastThrownExpired) {
                  this.dataMap.remove(stealer);
                  return String.format("Too bad, %s, the ball has dropped. Be faster next time!", stealer);
               } else if (!expired) {
                  return catchLastThrownExpired ? String.format("Too bad, %s, %s has thrown the ball already. Be faster next time!", stealer, this.userThrewBall) : String.format("Silly %s, the ball was not thrown at you. Steal it now!", stealer);
               } else {
                  this.changeState(ThrowBall.ThrowBallState.BallState.IDLE, this.userThrewBall, this.userThrewAtBall, this.userWithBall, ballCmd);
                  return catchLastThrownExpired ? String.format("Too bad, %s, %s has thrown the ball already. Be faster next time!", stealer, this.userThrewBall) : String.format("%s, the ball is free. Throw it now!", stealer);
               }
            }
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.STEAL) {
            stealer = messageData.source;
            target = ThrowBall.parseStealballCommand(messageData, chatSource);
            boolean expired = curTime - this.lastStateChangeTime > 40000L;
            if (!expired) {
               if (stealer.equals(this.userThrewBall) && target.equals(this.userThrewAtBall)) {
                  return String.format("Silly %s, you cannot steal a ball that you just threw.", stealer);
               } else if (stealer.equals(this.userThrewAtBall)) {
                  return target.equals(this.userThrewAtBall) ? String.format("Silly %s, you cannot steal a ball from yourself.", stealer) : String.format("Silly %s, the ball has been thrown at you. Why steal it from someone else?! Catch it now!", stealer);
               } else if (target.equals(this.userThrewAtBall)) {
                  this.changeState(ThrowBall.ThrowBallState.BallState.AT_HAND, this.userThrewBall, this.userThrewBall, stealer, ballCmd);
                  return String.format(emoteCommandData.getMessageText(), stealer, target);
               } else {
                  return String.format("%s, the ball is not with %s, you cannot steal it.", stealer, target);
               }
            } else {
               this.changeState(ThrowBall.ThrowBallState.BallState.IDLE, this.userThrewBall, this.userThrewAtBall, this.userWithBall, ballCmd);
               return String.format("%s, the ball is free. Throw it now!", stealer);
            }
         } else {
            return null;
         }
      }

      private String executeWhenAtHand(ThrowBall.ThrowBallState.BallCommand ballCmd, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         long curTime = System.currentTimeMillis();
         String stealer;
         String target;
         if (ballCmd == ThrowBall.ThrowBallState.BallCommand.THROW) {
            stealer = messageData.source;
            if (stealer.equals(this.userWithBall)) {
               target = ThrowBall.parseThrowballCommand(messageData, chatSource, true);
               this.changeState(ThrowBall.ThrowBallState.BallState.THROWN, stealer, target, this.userWithBall, ballCmd);
               return String.format(emoteCommandData.getMessageText(), stealer, target);
            } else if (curTime - this.lastStateChangeTime <= 40000L) {
               return String.format("%s, the ball is with %s. Wait for it to be thrown.", stealer, this.userWithBall);
            } else {
               target = ThrowBall.parseThrowballCommand(messageData, chatSource, true);
               this.changeState(ThrowBall.ThrowBallState.BallState.THROWN, stealer, target, this.userWithBall, ballCmd);
               return String.format(emoteCommandData.getMessageText(), stealer, target);
            }
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.CATCH) {
            stealer = messageData.source;
            boolean expired = curTime - this.lastStateChangeTime > 40000L;
            ThrowBall.BallData bd;
            if (!expired) {
               if (stealer.equals(this.userWithBall)) {
                  return String.format("Silly %s, the ball is with you. Throw it now!", stealer);
               } else {
                  bd = (ThrowBall.BallData)this.dataMap.get(stealer);
                  return bd != null && curTime - bd.timestamp <= 80000L ? String.format("Too bad, %s, %s has thrown the ball already. Be faster next time!", stealer, this.userThrewBall) : String.format("%s, the ball is with %s. You cannot catch it now.", stealer, this.userWithBall);
               }
            } else {
               this.changeState(ThrowBall.ThrowBallState.BallState.IDLE, this.userThrewBall, this.userThrewAtBall, this.userWithBall, ballCmd);
               bd = (ThrowBall.BallData)this.dataMap.get(stealer);
               return bd != null && curTime - bd.timestamp <= 80000L ? String.format("Too bad, %s, %s has thrown the ball already. Be faster next time!", stealer, this.userThrewBall) : String.format("%s, the ball is free. Throw it now!", stealer, this.userWithBall);
            }
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.STEAL) {
            stealer = messageData.source;
            target = ThrowBall.parseStealballCommand(messageData, chatSource);
            boolean expired = curTime - this.lastStateChangeTime > 40000L;
            if (!expired) {
               if (stealer.equals(this.userWithBall)) {
                  return target.equals(this.userWithBall) ? String.format("Silly %s, you cannot steal a ball from yourself.", stealer) : String.format("Silly %s, the ball is with you. Why are you still trying to steal it from %s.", stealer, target);
               } else {
                  return target.equals(this.userWithBall) ? String.format("%s, the ball is with %s. Wait for it to be thrown.", stealer, this.userWithBall) : String.format("%s, the ball is not with %s. You cannot steal it.", stealer, target);
               }
            } else {
               this.changeState(ThrowBall.ThrowBallState.BallState.IDLE, this.userThrewBall, this.userThrewAtBall, this.userWithBall, ballCmd);
               return String.format("%s, the ball is free. Throw it now!", stealer);
            }
         } else {
            return null;
         }
      }

      private String executeWhenIdle(ThrowBall.ThrowBallState.BallCommand ballCmd, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         String catcher;
         if (ballCmd == ThrowBall.ThrowBallState.BallCommand.THROW) {
            catcher = ThrowBall.parseThrowballCommand(messageData, chatSource, true);
            String thrower = messageData.source;
            this.changeState(ThrowBall.ThrowBallState.BallState.THROWN, thrower, catcher, this.userWithBall, ballCmd);
            return String.format(emoteCommandData.getMessageText(), thrower, catcher);
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.CATCH) {
            catcher = messageData.source;
            return String.format("%s, the ball is free. Throw it Now!", catcher);
         } else if (ballCmd == ThrowBall.ThrowBallState.BallCommand.STEAL) {
            catcher = messageData.source;
            return String.format("%s, the ball is free. Throw it Now!", catcher);
         } else {
            return null;
         }
      }

      private static enum BallCommand {
         THROW,
         CATCH,
         STEAL;

         public static ThrowBall.ThrowBallState.BallCommand fromString(String command) {
            if ("throwball".equals(command)) {
               return THROW;
            } else if ("catchball".equals(command)) {
               return CATCH;
            } else {
               return "stealball".equals(command) ? STEAL : null;
            }
         }
      }

      private static enum BallState {
         IDLE,
         THROWN,
         AT_HAND;
      }
   }

   public static class BallData {
      String username;
      long timestamp;

      public BallData(String username, long timestamp) {
         this.username = username;
         this.timestamp = timestamp;
      }

      public String toString() {
         return String.format("[BallData %s %s]", this.username, new Date(this.timestamp));
      }
   }
}
