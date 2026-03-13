package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.paintwars.Painter;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class PaintWars extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaintWars.class));

   public PaintWars(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      return chatSource.executeEmoteCommandWithState(this.emoteCommandData.getCommandName(), messageData.toIceObject());
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return new PaintWars.PaintWarsState();
   }

   private static class PaintWarsState extends EmoteCommandState {
      private static final int PENDING_PURCHASE_TIMEOUT = 20000;
      private static final int CLEANUP_PADDING = 10000;
      private static Map<String, PaintWars.PaintWarsState.UserPaintWarsState> userStates = new HashMap();

      private PaintWarsState() {
      }

      public EmoteCommand.ResultType execute(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         String[] args = messageData.getArgs();
         int argsCount = args.length;
         String username = chatSource.getParentUsername();

         try {
            int userReputationLevel = MemCacheOrEJB.getUserReputationLevel(username);
            if (userReputationLevel < Painter.getRequiredLevel()) {
               messageData.messageText = "Sorry, Paint Wars is currently in a private beta phase and you must be migLevel 45 and above to play. Official launch will be soon. Please check back often for your chance to play.";
               this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
               return EmoteCommand.ResultType.HANDLED_AND_STOP;
            }
         } catch (CreateException var15) {
            PaintWars.log.error(var15.getMessage());
            throw new FusionException(var15.getMessage());
         } catch (RemoteException var16) {
            PaintWars.log.error(var16.getMessage());
            throw new FusionException(var16.getMessage());
         }

         synchronized(userStates) {
            PaintWars.PaintWarsState.UserPaintWarsState userState = (PaintWars.PaintWarsState.UserPaintWarsState)userStates.get(username);
            PaintWars.PaintWarsState.State currentState = userState == null ? PaintWars.PaintWarsState.State.IDLE : userState.getCurrentState();
            String targetUsername;
            int points;
            switch(currentState) {
            case IDLE:
               if (args[0].equals("/paint")) {
                  if (argsCount != 2) {
                     throw new FusionException("Usage: /paint [username]");
                  }

                  this.paint(emoteCommandData, messageData, chatSource, args[1]);
               } else if (args[0].equals("/clean")) {
                  if (argsCount != 2) {
                     throw new FusionException("Usage: /clean [username]");
                  }

                  this.clean(emoteCommandData, messageData, chatSource, args[1]);
               } else {
                  if (!args[0].equals("/showpaint")) {
                     throw new FusionException(messageData.messageText + " is not a valid command");
                  }

                  if (argsCount != 1) {
                     throw new FusionException("Usage: /showpaint");
                  }

                  this.displayTaggedUsersInChatroom(emoteCommandData, messageData, chatSource);
               }
               break;
            case WAITING_PAINT:
               if (!args[0].equals("/pw") || argsCount != 2 || !args[1].equals("y") && !args[1].equals("n")) {
                  messageData.messageText = "Please send /pw y to continue or /pw n to stop";
                  this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
               } else {
                  if (args[1].equals("y")) {
                     targetUsername = userState.getTargetUsername();
                     if (chatSource.isUserInChat(targetUsername)) {
                        if (Painter.isClean(targetUsername)) {
                           if (!Painter.isPaintProof(targetUsername)) {
                              if (!Painter.hadInteraction(username, targetUsername)) {
                                 Painter.buyPaintCredit(username);
                                 points = Painter.paint(username, userState.getTargetUsername());
                                 String messageUsername = username;
                                 if (Painter.hasStealthPaint(username)) {
                                    messageUsername = "Player";
                                 }

                                 messageData.messageText = String.format("%s has painted (paintwars-paintemoticon) %s. %s received %d points.", messageUsername, userState.getTargetUsername(), messageUsername, points);
                                 emoteCommandData.setEmoticonKeys(this.convertStrToList("(paintwars-paintemoticon)"));
                                 this.displayPublicMessage(emoteCommandData, messageData, chatSource);
                              } else {
                                 messageData.messageText = String.format("You have already interacted with %s today. Please try again tomorrow. No migCredits were deducted.", targetUsername);
                                 this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                              }
                           } else {
                              messageData.messageText = String.format("%s is currently paint proof. No migCredits were deducted.", targetUsername);
                              this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                           }
                        } else {
                           messageData.messageText = String.format("%s has already been painted. No migCredits were deducted.", targetUsername);
                           this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                        }
                     } else {
                        messageData.messageText = String.format("%s is no longer in the chatroom. No migCredits were deducted.", targetUsername);
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                     }
                  } else {
                     messageData.messageText = String.format("%s was not painted", userState.getTargetUsername());
                     this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                  }

                  this.removeUserState(username);
               }
               break;
            case WAITING_CLEAN:
               if (args[0].equals("/pw") && argsCount == 2 && (args[1].equals("y") || args[1].equals("n"))) {
                  if (args[1].equals("y")) {
                     targetUsername = userState.getTargetUsername();
                     if (chatSource.isUserInChat(targetUsername)) {
                        if (!Painter.isClean(targetUsername)) {
                           if (!Painter.hadInteraction(username, targetUsername)) {
                              Painter.buyCleanCredit(username);
                              points = Painter.clean(username, targetUsername);
                              messageData.messageText = String.format("%s has Cleaned paint (paintwars-cleanemoticon) on %s. %s received %d points.", username, userState.getTargetUsername(), username, points);
                              emoteCommandData.setEmoticonKeys(this.convertStrToList("(paintwars-cleanemoticon)"));
                              this.displayPublicMessage(emoteCommandData, messageData, chatSource);
                           } else {
                              messageData.messageText = String.format("You have already interacted with %s today. Please try again tomorrow. No migCredits were deducted.", targetUsername);
                              this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                           }
                        } else {
                           messageData.messageText = String.format("%s is already clean. No migCredits were deducted.", targetUsername);
                           this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                        }
                     } else {
                        messageData.messageText = String.format("%s is no longer in the chatroom. No migCredits were deducted.", targetUsername);
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                     }
                  } else {
                     messageData.messageText = String.format("%s was not cleaned", userState.getTargetUsername());
                     this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                  }

                  this.removeUserState(username);
               } else {
                  messageData.messageText = "Please send /pw y to continue or /pw n to stop";
                  this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
               }
               break;
            default:
               PaintWars.log.error("Paint Wars user [" + username + "] is in an unknown state");
            }
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }

      public void cleanUp() {
         long currentTime = Calendar.getInstance().getTimeInMillis();
         Iterator itr = userStates.entrySet().iterator();

         while(itr.hasNext()) {
            Entry<String, PaintWars.PaintWarsState.UserPaintWarsState> entry = (Entry)itr.next();
            long timestamp = ((PaintWars.PaintWarsState.UserPaintWarsState)entry.getValue()).getTimestamp();
            if (timestamp + 20000L + 10000L < currentTime) {
               itr.remove();
            }
         }

      }

      private void paint(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource, String targetUsername) throws FusionException {
         String username = chatSource.getParentUsername();

         try {
            if (!username.equals(targetUsername)) {
               if (chatSource.isUserInChat(targetUsername)) {
                  if (Painter.isClean(targetUsername)) {
                     if (!Painter.isPaintProof(targetUsername)) {
                        if (!Painter.hadInteraction(username, targetUsername)) {
                           if (Painter.hasFreePaintCredits(username)) {
                              int points = Painter.paint(username, targetUsername);
                              String messageUsername = username;
                              if (Painter.hasStealthPaint(username)) {
                                 messageUsername = "Player";
                              }

                              messageData.messageText = String.format("%s has painted (paintwars-paintemoticon) %s. %s received %d points.", messageUsername, targetUsername, username, points);
                              emoteCommandData.setEmoticonKeys(this.convertStrToList("(paintwars-paintemoticon)"));
                              this.displayPublicMessage(emoteCommandData, messageData, chatSource);
                              messageData.messageText = String.format("You have %d free Paints left", Painter.getRemainingFreePaints(username));
                              this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                           } else {
                              messageData.messageText = String.format("You have 0 Paints left today. To Paint: %s. Continue? Send /pw y for YES or /pw n for NO in the next 20 seconds", Painter.getPriceOfPaint());
                              this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                              PaintWars.PaintWarsState.UserPaintWarsState userState = new PaintWars.PaintWarsState.UserPaintWarsState(targetUsername, PaintWars.PaintWarsState.State.WAITING_PAINT);
                              userState.scheduleTimer("Timed out! Pending paint credit purchase cancelled. No migCredits were deducted.", emoteCommandData, messageData, chatSource);
                              userStates.put(username, userState);
                           }
                        } else {
                           messageData.messageText = String.format("You have already interacted with %s today. Please try again tomorrow.", targetUsername);
                           this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                        }
                     } else {
                        messageData.messageText = String.format("%s is currently paint proof", targetUsername);
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                        messageData.messageText = String.format("You have %d free Paints left", Painter.getRemainingFreePaints(username));
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                     }
                  } else {
                     messageData.messageText = String.format("%s has already been painted", targetUsername);
                     this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                     messageData.messageText = String.format("You have %d free Paints left", Painter.getRemainingFreePaints(username));
                     this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                  }
               } else {
                  messageData.messageText = String.format("Unable to Paint. %s is not in the room.", targetUsername);
                  this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
               }
            } else {
               messageData.messageText = "You cannot paint yourself";
               this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
            }
         } catch (Exception var8) {
            PaintWars.log.error("Error painting [" + targetUsername + "] with [" + username + "]: " + var8.getLocalizedMessage(), var8);
         }

      }

      private void clean(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource, String targetUsername) throws FusionException {
         String username = chatSource.getParentUsername();

         try {
            if (chatSource.isUserInChat(targetUsername)) {
               if (!Painter.isClean(targetUsername)) {
                  if (!Painter.hadInteraction(username, targetUsername)) {
                     if (Painter.hasFreeCleanCredits(username)) {
                        int points = Painter.clean(username, targetUsername);
                        messageData.messageText = String.format("%s has Cleaned paint on (paintwars-cleanemoticon) %s. %s received %d points.", username, targetUsername, username, points);
                        emoteCommandData.setEmoticonKeys(this.convertStrToList("(paintwars-cleanemoticon)"));
                        this.displayPublicMessage(emoteCommandData, messageData, chatSource);
                        messageData.messageText = String.format("You have %d free Cleans left", Painter.getRemainingFreeCleans(username));
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                     } else {
                        messageData.messageText = String.format("You have 0 Cleans left today. To Clean: %s. Continue? Send /pw y for YES or /pw n for NO in the next 20 seconds", Painter.getPriceOfClean());
                        this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                        PaintWars.PaintWarsState.UserPaintWarsState userState = new PaintWars.PaintWarsState.UserPaintWarsState(targetUsername, PaintWars.PaintWarsState.State.WAITING_CLEAN);
                        userState.scheduleTimer("Timed out! Pending clean credit purchase cancelled. No migCredits were deducted.", emoteCommandData, messageData, chatSource);
                        userStates.put(username, userState);
                     }
                  } else {
                     messageData.messageText = String.format("You have already interacted with %s today. Please try again tomorrow.", targetUsername);
                     this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                  }
               } else {
                  messageData.messageText = String.format("%s is already clean", targetUsername);
                  this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
                  messageData.messageText = String.format("You have %d free cleans left", Painter.getRemainingFreeCleans(username));
                  this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
               }
            } else {
               messageData.messageText = String.format("Unable to Clean. %s is not in the room.", targetUsername);
               this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
            }
         } catch (Exception var7) {
            PaintWars.log.error("Error cleaning [" + targetUsername + "] with [" + username + "]: " + var7.getLocalizedMessage());
         }

      }

      private void displayTaggedUsersInChatroom(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         String[] users = chatSource.getVisibleUsernamesInChat(false);
         ArrayList<String> taggedUsers = Painter.getAllTaggedUsers(users);
         String displayUsers = "";

         for(Iterator itr = taggedUsers.iterator(); itr.hasNext(); displayUsers = displayUsers + (String)itr.next() + "\n") {
         }

         if (displayUsers == "") {
            messageData.messageText = "There are currently no users in the room that are painted";
         } else {
            messageData.messageText = "List of painted users in the room:\n" + displayUsers;
         }

         this.displayPrivateMessage(emoteCommandData, messageData, chatSource);
      }

      private void displayPrivateMessage(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
      }

      private void displayPublicMessage(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
         emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToAllUsersInChat(messageData);
      }

      private void removeUserState(String username) {
         PaintWars.PaintWarsState.UserPaintWarsState userState = (PaintWars.PaintWarsState.UserPaintWarsState)userStates.get(username);
         if (userState != null) {
            userState.stopTimer();
            userStates.remove(username);
         }

      }

      private java.util.List<String> convertStrToList(String str) {
         String[] strArray = new String[]{str};
         return Arrays.asList(strArray);
      }

      // $FF: synthetic method
      PaintWarsState(Object x0) {
         this();
      }

      private class UserPaintWarsState {
         private String targetUsername;
         private PaintWars.PaintWarsState.State currentState;
         private long timestamp;
         private Timer timer;
         private PaintWars.PaintWarsState.UserPaintWarsState.SendTimeoutTextTask task;

         public UserPaintWarsState(String targetUsername, PaintWars.PaintWarsState.State state) {
            this.targetUsername = targetUsername;
            this.currentState = state;
            this.timestamp = Calendar.getInstance().getTimeInMillis();
            this.timer = new Timer("Paint Wars Timer", true);
         }

         public String getTargetUsername() {
            return this.targetUsername;
         }

         public PaintWars.PaintWarsState.State getCurrentState() {
            return this.currentState;
         }

         public long getTimestamp() {
            return this.timestamp;
         }

         public void scheduleTimer(String message, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
            this.task = new PaintWars.PaintWarsState.UserPaintWarsState.SendTimeoutTextTask(message, emoteCommandData, messageData, chatSource);
            this.timer.schedule(this.task, 20000L);
         }

         public void stopTimer() {
            if (this.task != null) {
               this.task.cancel();
            }

         }

         private class SendTimeoutTextTask extends TimerTask {
            private MessageData messageData;
            private ChatSource chatSource;

            SendTimeoutTextTask(String message, EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) {
               this.messageData = new MessageData(messageData.toIceObject());
               emoteCommandData.updateMessageData(this.messageData);
               this.messageData.messageText = message;
               this.chatSource = chatSource;
            }

            public void run() {
               try {
                  this.chatSource.sendMessageToSender(this.messageData);
               } catch (FusionException var2) {
                  PaintWars.log.error("Failed to send message to sender: " + this.messageData);
                  PaintWars.log.error(var2.getMessage());
               }

               UserPaintWarsState.this.currentState = PaintWars.PaintWarsState.State.IDLE;
            }
         }
      }

      private static enum State {
         IDLE,
         WAITING_PAINT,
         WAITING_CLEAN;
      }
   }
}
