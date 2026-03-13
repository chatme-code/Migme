package com.projectgoth.fusion.botservice.bot.migbot.trivia;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.data.TriviaQuestions;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionCategoryEnum;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class Trivia extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Trivia.class));
   public List<String> players;
   Map<String, Score> scores;
   List<Question> questions;
   Question currentQuestion;
   int currentQuestionNumber = 0;
   List<String> questionParticipants = new ArrayList();
   String firstCorrectAnswerBy = null;
   public static final String TIMER_ANSWER = "timerAnswer";
   public static final String TIMER_IDLE = "timerIdle";
   public static final String NUMBER_OF_QUESTIONS = "numberOfQuestions";
   public static final String SOURCE = "source";
   public static final long TIMER_ANSWER_VALUE = 55L;
   public static final int NUMBER_OF_QUESTIONS_VALUE = 5;
   public static final long IDLE_TIME_VALUE = 30L;
   private static final String SOURCE_PACKAGE = "com.projectgoth.fusion.botservice.bot.migbot.trivia.data";
   public long timeToAnswer = 45L;
   public int numQuestions = 5;
   long timeAllowedToIdle = 30L;
   String source = "TriviaQuestions";
   Date lastActivityTime;
   ScheduledFuture answerTimerTask = null;
   private BotData.BotStateEnum gameState;
   private TriviaQuestionCategoryEnum category;

   public Trivia(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.category = TriviaQuestionCategoryEnum.ENGLISH;
      this.loadGameConfig();
      this.players = new ArrayList();
      this.scores = new HashMap();
      log.info(botData.getDisplayName() + "[" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED"));
      String message = this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
      this.sendChannelMessage(message);
      this.updateLastActivityTime();
   }

   private synchronized BotData.BotStateEnum getGameState() {
      return this.gameState;
   }

   private synchronized void setGameState(BotData.BotStateEnum gameState) {
      this.gameState = gameState;
   }

   public void stopBot() {
      if (this.answerTimerTask != null && !this.answerTimerTask.isDone() && !this.answerTimerTask.isCancelled()) {
         this.answerTimerTask.cancel(true);
      }

      this.setGameState(BotData.BotStateEnum.NO_GAME);
      log.debug("Stopped bot instanceID[" + this.instanceID + "]");
   }

   public boolean isIdle() {
      long timeSince = (new Date()).getTime() - this.lastActivityTime.getTime();
      if (timeSince < 0L) {
         log.warn("Error calculating time since. Target date is in the future.");
         return false;
      } else {
         long minutes = timeSince / 60000L % 60L;
         if (minutes > this.timeAllowedToIdle) {
            log.debug("Bot has been idle for " + minutes + (minutes == 1L ? " minute" : " minutes") + ". Marking as idle...");
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean canBeStoppedNow() {
      return (this.gameState != BotData.BotStateEnum.PLAYING || this.pot == null) && this.gameState != BotData.BotStateEnum.GAME_JOINING && this.gameState != BotData.BotStateEnum.GAME_STARTING;
   }

   private synchronized void updateLastActivityTime() {
      this.lastActivityTime = new Date();
   }

   private void loadGameConfig() {
      this.timeToAnswer = this.getLongParameter("timerAnswer", 55L);
      this.numQuestions = this.getIntParameter("numberOfQuestions", 5);
      this.timeAllowedToIdle = this.getLongParameter("timerIdle", 30L);
      this.source = this.getStringParameter("source", this.source);
      this.category = TriviaQuestionCategoryEnum.ENGLISH;
   }

   private void loadQuestions() {
      this.questions = TriviaQuestions.chooseRandomQuestions(this.category, this.numQuestions);
      if (log.isDebugEnabled()) {
         log.debug("botInstanceID [" + this.instanceID + "]: Question set created.");
      }

   }

   public synchronized void onMessage(String username, String messageText, long receivedTimestamp) {
      if (messageText.toLowerCase().startsWith("!start")) {
         if (this.getGameState() == BotData.BotStateEnum.NO_GAME) {
            try {
               this.startGame(username);
            } catch (Exception var6) {
               log.error("Error starting game: ", var6);
            }
         } else {
            this.sendGameCannotBeStartedMessage(username);
         }
      } else if (messageText.startsWith("!") && messageText.length() > "!".length() && this.currentQuestion != null) {
         if (this.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
            if (this.questionParticipants.contains(username)) {
               if (log.isDebugEnabled()) {
                  log.debug(username + " already answered this question.");
               }

               this.sendMessage(this.createMessage("ALREADY_ANSWERED", username), username);
               return;
            }

            if (messageText.substring("!".length()).length() > 1) {
               this.sendMessage(this.createMessage("ANSWER_FORMAT", username), username);
               return;
            }
         }

         boolean isCorrectAnswer = this.checkAnswer(messageText.substring("!".length()));
         if (this.currentQuestion.type == TriviaQuestionTypeEnum.OPEN) {
            this.handleOpenAnswer(username, isCorrectAnswer);
         } else {
            this.handleMultipleChoiceAnswer(username, isCorrectAnswer);
         }
      }

   }

   private void handleMultipleChoiceAnswer(String username, boolean isCorrectAnswer) {
      synchronized(this) {
         if (this.firstCorrectAnswerBy == null && isCorrectAnswer) {
            this.firstCorrectAnswerBy = username;
            this.updateUserScore(username);
            this.getScoreboard();
         }
      }

      this.questionParticipants.add(username);
      this.sendMessage(this.createMessage("WAIT_FOR_ANSWER", username), username);
   }

   private void handleOpenAnswer(String username, boolean isCorrectAnswer) {
      if (isCorrectAnswer) {
         this.updateUserScore(username);
         String scoreboard = this.getScoreboard();
         if (log.isDebugEnabled()) {
            log.debug("User " + username + " answered the question correctly. Timer canceled? " + this.answerTimerTask.isCancelled() + ". Or was the time up(done)? " + this.answerTimerTask.isDone());
         }

         this.sendChannelMessage(this.createMessage("CORRECT_ANSWER", username, (Score)this.scores.get(username), scoreboard));
         this.sendChannelMessage(this.createMessage("SCORES", (String)null, (Score)null, scoreboard));
         this.setNextQuestion();
      } else {
         this.sendMessage(this.createMessage("INCORRECT_ANSWER", username, (Score)this.scores.get(username), (String)null), username);
      }

   }

   private int updateUserScore(String username) {
      Score score = (Score)this.scores.get(username);
      if (score == null) {
         score = new Score(username, 1);
      } else {
         score.incrementScore(1);
      }

      this.scores.put(username, score);
      return score.points;
   }

   public String getScoreboard() {
      List<Score> orderedScores = new ArrayList();
      Iterator i$ = this.scores.values().iterator();

      while(i$.hasNext()) {
         Score score = (Score)i$.next();
         if (score.getScore() > 0) {
            orderedScores.add(score);
         }
      }

      Collections.sort(orderedScores);
      StringBuilder scoreString = new StringBuilder();

      for(int i = 0; i < orderedScores.size(); ++i) {
         Score score = (Score)orderedScores.get(i);
         scoreString.append(score.getPlayer() + " - " + score.getScore());
         if (i < orderedScores.size() - 1) {
            scoreString.append(", ");
         }
      }

      return scoreString.toString();
   }

   public String getWinner() {
      if (this.scores != null && this.scores.size() != 0) {
         List<Score> orderedScores = new ArrayList();
         Iterator i$ = this.scores.values().iterator();

         while(i$.hasNext()) {
            Score score = (Score)i$.next();
            if (score.getScore() > 0) {
               orderedScores.add(score);
            }
         }

         Collections.sort(orderedScores);
         if (orderedScores.size() == 1) {
            return ((Score)orderedScores.get(0)).getPlayer();
         } else if (((Score)orderedScores.get(0)).getScore() != ((Score)orderedScores.get(1)).getScore()) {
            return ((Score)orderedScores.get(0)).getPlayer();
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   synchronized void setNextQuestion() {
      this.questionParticipants.clear();
      this.firstCorrectAnswerBy = null;
      if (this.currentQuestionNumber == this.numQuestions) {
         this.endGame();
      } else {
         if (this.currentQuestionNumber == 0) {
            this.currentQuestion = (Question)this.questions.get(0);
         } else {
            this.currentQuestion = (Question)this.questions.get(this.currentQuestionNumber);
         }

         ++this.currentQuestionNumber;
         String questionMessage = this.currentQuestionNumber == this.numQuestions ? this.createMessage("LAST_QUESTION") : this.createMessage("NTH_QUESTION");
         this.sendChannelMessage(questionMessage);
         if (log.isDebugEnabled()) {
            log.debug(this.botData.getDisplayName() + ": Started timer for TimedCountdownTask : question#" + this.currentQuestionNumber);
         }

         this.answerTimerTask = this.executor.schedule(new Trivia.TimedCountdownTask(this, this.currentQuestionNumber), this.timeToAnswer, TimeUnit.SECONDS);
      }

   }

   private boolean checkAnswer(String messageText) {
      return this.currentQuestion.isCorrectAnswer(messageText);
   }

   public void startGame(String username) throws Exception {
      log.info("Trivia game started by " + username);
      this.updateLastActivityTime();
      if (this.gameState.equals(BotData.BotStateEnum.NO_GAME)) {
         String message = this.createMessage("STARTGAME-NOTICE", username);
         this.setGameState(BotData.BotStateEnum.GAME_STARTING);
         this.gameStarter = username;
         this.sendChannelMessage(message);
         this.executor.execute(new Trivia.StartGame());
      } else {
         this.sendGameCannotBeStartedMessage(username);
      }

   }

   private void sendGameCannotBeStartedMessage(String username) {
      String message = null;
      switch(this.gameState.value()) {
      case 2:
      case 5:
         message = this.createMessage("STATUS-PLAYING", username);
         break;
      case 3:
         message = this.createMessage("STATUS-JOINING", username);
         break;
      case 4:
      default:
         message = this.createMessage("STATUS-CANNOT-START", username);
      }

      this.sendMessage(message, username);
   }

   public synchronized void onUserJoinChannel(String username) {
      String message = null;
      synchronized(this.gameState) {
         switch(this.gameState.value()) {
         case 2:
         case 5:
            message = this.createMessage("GAME_STATE_STARTED");
            break;
         default:
            message = this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
         }
      }

      this.sendMessage(message, username);
   }

   public synchronized void onUserLeaveChannel(String username) {
      this.scores.remove(username);
   }

   public void endGame() {
      String scoreboard = this.getScoreboard();
      if (StringUtils.hasLength(scoreboard)) {
         this.sendChannelMessage(this.createMessage("GAME_OVER", (String)null, (Score)null, scoreboard));
         String winner = this.getWinner();
         if (winner != null) {
            this.sendChannelMessageAndPopUp(winner + " won!");
         }
      } else {
         this.sendChannelMessage(this.createMessage("GAME_OVER_NO_WINNER", (String)null, (Score)null, (String)null));
      }

      this.resetGame();
      this.updateLastActivityTime();
      this.sendChannelMessage(this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
   }

   private void resetGame() {
      this.answerTimerTask = null;
      this.currentQuestion = null;
      this.currentQuestionNumber = 0;
      this.gameStarter = null;
      this.players.clear();
      this.questionParticipants.clear();
      this.firstCorrectAnswerBy = null;
      this.scores.clear();
      this.category = TriviaQuestionCategoryEnum.ENGLISH;
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   protected String createMessage(String messageKey) {
      return this.createMessage(messageKey, (String)null, (Score)null, (String)null);
   }

   String createMessage(String messageKey, String player) {
      return this.createMessage(messageKey, player, (Score)null, (String)null);
   }

   String createMessage(String messageKey, String player, Score currentPlayerScore, String scoreboard) {
      return this.createMessage(messageKey, player, currentPlayerScore, scoreboard, (String)null);
   }

   private String createMessage(String messageKey, String player, Score currentPlayerScore, String scoreboard, String errorInput) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         messageToSend = messageToSend.replaceAll("BOTNAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replaceAll("TIMER_ANSWER", "" + this.timeToAnswer);
         messageToSend = messageToSend.replaceAll("NUM_QUESTIONS", this.numQuestions + "");
         if (this.currentQuestion != null) {
            this.currentQuestion.question = this.currentQuestion.question.replaceAll("\\$", "\\\\\\$");
            this.currentQuestion.answer = this.currentQuestion.answer.replaceAll("\\$", "\\\\\\$");
            messageToSend = messageToSend.replaceAll("CURRENT_QUESTION", this.currentQuestion.question + "?");
            messageToSend = messageToSend.replaceAll("QUESTION_NUMBER", this.currentQuestionNumber + "");
            messageToSend = messageToSend.replaceAll("CORRECT_ANSWER", this.currentQuestion.type == TriviaQuestionTypeEnum.OPEN ? "[" + this.currentQuestion.answer + "]" : this.currentQuestion.answer);
            messageToSend = messageToSend.replaceAll("ANSWER_FORMAT", this.currentQuestion.type == TriviaQuestionTypeEnum.OPEN ? "!<your_answer>" : "!A or !a");
         }

         if (scoreboard != null) {
            messageToSend = messageToSend.replaceAll("SCORES", scoreboard);
         }

         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
            if (currentPlayerScore != null) {
               messageToSend = messageToSend.replaceAll("POINTS", currentPlayerScore.points + (currentPlayerScore.points == 1 ? " point" : " points"));
            }
         }

         messageToSend = messageToSend.replaceAll("CURRENCY", "USD");
         messageToSend = messageToSend.replaceAll("DENOMINATION", "c");
         messageToSend = messageToSend.replaceAll("AMOUNT_START", "0");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         if (this.category != null) {
            messageToSend = messageToSend.replaceAll("CATEGORY", this.category.name());
         }

         if (StringUtils.hasLength(errorInput)) {
            messageToSend = messageToSend.replaceAll("ERROR_INPUT", errorInput);
         }

         return messageToSend;
      } catch (NullPointerException var7) {
         log.error("Outgoing message could not be created, key = " + messageKey, var7);
         return "";
      }
   }

   class TimedCountdownTask implements Runnable {
      Trivia bot;
      int questionNumber = -1;

      TimedCountdownTask(Trivia bot, int questionNumber) {
         this.bot = bot;
         this.questionNumber = questionNumber;
      }

      public int getQuestionNumber() {
         return this.questionNumber;
      }

      public void run() {
         synchronized(this.bot) {
            if (Trivia.this.getGameState() == BotData.BotStateEnum.PLAYING) {
               if (Trivia.log.isDebugEnabled()) {
                  Trivia.log.debug(Trivia.this.botData.getDisplayName() + ": Time up for TimedCountdownTask : questionNumber is :" + this.questionNumber + ", currentQuestionNumber = " + Trivia.this.currentQuestionNumber);
               }

               if (Trivia.this.currentQuestionNumber == this.questionNumber) {
                  Trivia.this.sendChannelMessage(Trivia.this.createMessage("TIME_UP"));
                  if (Trivia.this.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE && Trivia.this.firstCorrectAnswerBy != null) {
                     String scoreboard = Trivia.this.getScoreboard();
                     Trivia.this.sendChannelMessage(Trivia.this.createMessage("FIRST_CORRECT_ANSWER", Trivia.this.firstCorrectAnswerBy, (Score)Trivia.this.scores.get(Trivia.this.firstCorrectAnswerBy), scoreboard));
                     Trivia.this.sendChannelMessage(Trivia.this.createMessage("SCORES", (String)null, (Score)null, scoreboard));
                  }

                  Trivia.this.setNextQuestion();
               }
            }

         }
      }
   }

   class StartGame implements Runnable {
      public void run() {
         if (Trivia.log.isDebugEnabled()) {
            Trivia.log.debug(Trivia.this.botData.getDisplayName() + ": Game starting in StartGame():");
         }

         if (Trivia.this.getGameState() == BotData.BotStateEnum.GAME_STARTING) {
            Trivia.log.info("New game started in " + Trivia.this.channel);
            Trivia.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
            String message = Trivia.this.createMessage("GAME_STARTED_NOTE");
            Trivia.this.sendChannelMessage(message);
            Trivia.this.setGameState(BotData.BotStateEnum.PLAYING);
            Trivia.this.loadQuestions();
            Trivia.this.setNextQuestion();
         }

      }
   }
}
