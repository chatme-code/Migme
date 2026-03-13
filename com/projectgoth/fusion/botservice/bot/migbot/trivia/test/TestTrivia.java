package com.projectgoth.fusion.botservice.bot.migbot.trivia.test;

import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionCategoryEnum;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.BotData;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class TestTrivia {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(TestTrivia.class));
   public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
   private BotData.BotStateEnum gameState;
   private List<String> players;
   public String gameStarter;
   Map<String, TestTrivia.Score> scores;
   List<TestTrivia.TestQuestion> testQuestions;
   TestTrivia.TestQuestion currentQuestion;
   int currentQuestionNumber;
   List<String> questionParticipants;
   String firstCorrectAnswerBy;
   protected static final String COMMAND_CHAR = "!";
   protected static final String COMMAND_START = "!start";
   protected static final String COMMAND_NO = "!n";
   public static final String TIMER_ANSWER = "timerAnswer";
   public static final String NUMBER_OF_QUESTIONS = "numberOfQuestions";
   public static final long TIMER_ANSWER_VALUE = 5L;
   public static final int NUMBER_OF_QUESTIONS_VALUE = 5;
   public static final long IDLE_TIME_VALUE = 5L;
   protected double amountStartGame;
   public long timeToAnswer;
   public int numQuestions;
   Date lastActivityTime;
   ScheduledFuture answerTimerTask;
   private TriviaQuestionCategoryEnum category;
   protected Map<String, String> messages;

   public TestTrivia() {
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.currentQuestionNumber = 0;
      this.questionParticipants = new ArrayList();
      this.firstCorrectAnswerBy = null;
      this.amountStartGame = 0.0D;
      this.timeToAnswer = 5L;
      this.numQuestions = 5;
      this.answerTimerTask = null;
      this.category = TriviaQuestionCategoryEnum.ENGLISH;
      this.players = new ArrayList();
      this.scores = new HashMap();
      this.category = TriviaQuestionCategoryEnum.ENGLISH;
      this.messages = new HashMap();
      this.messages.put("ADDED_TO_GAME", "PLAYER: added to game. ");
      this.messages.put("CHARGES_APPLY_POT", "Charges apply. CURRENCY AMOUNT_POT");
      this.messages.put("CHARGES_APPLY_JOIN", "Charges apply. CURRENCY AMOUNT_JOIN");
      this.messages.put("CHARGE_NEW_GAME", "PLAYER: Charges apply. CURRENCY AMOUNT_START for new game.");
      this.messages.put("CHARGE_NEW_POT", "PLAYER: Charges apply. CURRENCY AMOUNT_POT Create/enter pot.");
      this.messages.put("CHARGE_CONF_NO_MSG", " CMD_NO to cancel. CONF_TIMER seconds");
      this.messages.put("STARTGAME-NOTICE", "PLAYER started a game!");
      this.messages.put("INSUFFICIENT_FUNDS_START", "PLAYER: Sorry, insufficient funds to start game.");
      this.messages.put("INSUFFICIENT_FUNDS_JOIN", "PLAYER: Sorry, insufficient funds to join game.");
      this.messages.put("INSUFFICIENT_FUNDS_POT", "PLAYER: Sorry, insufficient funds to join pot.");
      this.messages.put("STATUS-STARTED", "A game is already started.");
      this.messages.put("STATUS-PLAYING", "A game is currently on.");
      this.messages.put("STATUS-JOINING", "A game is on. CMD_JOIN to join. Charges may apply.");
      this.messages.put("STATUS-CANNOT-START", "Sorry, new game cannot be started now.");
      this.messages.put("PLAYER_CHARGED_START", "PLAYER: You were charged LOCAL_CURNCY AMT_START_LOCAL to start game.");
      this.messages.put("PLAYER_CHARGED_JOIN", "PLAYER: You were charged LOCAL_CURNCY AMT_JOIN_LOCAL to join game.");
      this.messages.put("PLAYER_CHARGED_POT", "PLAYER: You were charged LOCAL_CURNCY AMT_POT_LOCAL to enter the pot.");
      this.messages.put("BOT_ADDED", "Bot BOTNAME added to room.");
      this.messages.put("NOT_CHARGED", "PLAYER: You were not charged.");
      this.messages.put("INVALID_COMMAND", "PLAYER: Invalid command.");
      this.messages.put("START_GAME", "CMD_START to start a new game of Trivia");
      this.messages.put("NTH_QUESTION", "#QUESTION_NUMBER [ANSWER_FORMAT  to answer] : CURRENT_QUESTION ");
      this.messages.put("LAST_QUESTION", "Last Question [ANSWER_FORMAT  to answer] : CURRENT_QUESTION");
      this.messages.put("CORRECT_ANSWER", "PLAYER : POINTS! CORRECT_ANSWER is correct!");
      this.messages.put("INCORRECT_ANSWER", "PLAYER: That is incorrect. Anyone else?");
      this.messages.put("SCORES", "Scoreboard: SCORES");
      this.messages.put("TIME_UP", "Time''s up! Answer: CORRECT_ANSWER");
      this.messages.put("REPEAT_QUESTION", "Repeating Question QUESTION_NUMBER: CURRENT_QUESTION");
      this.messages.put("GAME_STATE_STARTED", "A Trivia game is on now. Question QUESTION_NUMBER: CURRENT_QUESTION");
      this.messages.put("GAME_STATE_DEFAULT_NO_AMOUNT", "CMD_START to start a game of Trivia");
      this.messages.put("GAME_STATE_DEFAULT_AMOUNT", "CMD_START to start a game of Trivia. CURRENCY AMOUNT_START");
      this.messages.put("GAME_STARTED_NOTE", "Trivia begins! NUM_QUESTIONS QUESTIONS. TIMER_ANSWER seconds each. Go!");
      this.messages.put("GAME_OVER", "Game over! Scoreboard: SCORES");
      this.messages.put("GAME_OVER_NO_WINNER", "Game over! There''s no winner. Boo!");
      this.messages.put("START_CATEGORIES", "Categories are: CATEGORY_STRING");
      this.messages.put("INVALID_CATEGORY", "PLAYER: categoryId ERROR_INPUT is invalid. Game not be started.");
      this.messages.put("CATEGORY", "PLAYER: A game will be started in CATEGORY.");
      this.messages.put("WAIT_FOR_ANSWER", "Thanks PLAYER! Please wait for the answer.");
      this.messages.put("ALREADY_ANSWERED", "PLAYER: you had your chance! Please wait.");
      this.messages.put("ANSWER_FORMAT", "PLAYER: Type ANSWER_FORMAT to answer.");
      this.messages.put("FIRST_CORRECT_ANSWER", "PLAYER got it first! POINTS!");
   }

   public synchronized BotData.BotStateEnum getGameState() {
      return this.gameState;
   }

   public synchronized void setGameState(BotData.BotStateEnum gameState) {
      this.gameState = gameState;
   }

   public void sendMessage(String message, String player) {
      debug("PLAYER MSG: message = " + message);
   }

   public void sendChannelMessage(String message) {
      debug("CHANNEL MSG: " + message);
   }

   private void loadQuestions() {
      int questionNumber = 0;
      this.testQuestions = new ArrayList();
      List<String> questionList = new ArrayList();
      String source = "SportsTriviaQuestions";

      try {
         Class c = Class.forName("com.projectgoth.fusion.botservice.bot.migbot.trivia.data." + source);
         System.out.println("Loaded class: " + c);
         Class[] signature = new Class[]{TriviaQuestionCategoryEnum.class, Integer.TYPE};
         Method m = c.getDeclaredMethod("chooseRandomQuestions", signature);
         System.out.println("Got method: " + m);
         Object[] parameters = new Object[]{this.category, this.numQuestions};
         questionList = (List)m.invoke((Object)null, parameters);
         System.out.println("Output: " + questionList);
      } catch (Exception var8) {
         log.error("Question source is invalid: " + source);
      }

      Iterator i$ = ((List)questionList).iterator();

      while(i$.hasNext()) {
         String questionObj = (String)i$.next();
         StringTokenizer tokenizer = new StringTokenizer(questionObj, "\t");

         while(tokenizer.hasMoreTokens()) {
            this.createQuestion(questionNumber, questionObj, tokenizer);
         }
      }

      if (log.isDebugEnabled()) {
         debug("Question set created.");
      }

   }

   private void createQuestion(int questionNumber, String questionObj, StringTokenizer tokenizer) {
      TriviaQuestionTypeEnum typeEnum = null;
      String questionTypeStr = tokenizer.nextToken();
      boolean var6 = true;

      try {
         int type = Integer.parseInt(questionTypeStr);
         typeEnum = TriviaQuestionTypeEnum.fromValue(type);
      } catch (Exception var11) {
         System.out.println("Error parsing question string: " + questionObj);
         typeEnum = TriviaQuestionTypeEnum.OPEN;
      }

      String questionStr = tokenizer.nextToken();
      String answer = tokenizer.nextToken();
      TestTrivia.TestQuestion testQuestion = new TestTrivia.TestQuestion(questionNumber++, typeEnum, questionStr, answer);
      if (typeEnum == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
         if (tokenizer.hasMoreTokens()) {
            char answerChar = tokenizer.nextToken().toLowerCase().charAt(0);
            testQuestion.setAnswerChar(answerChar);
            if (log.isDebugEnabled()) {
               debug("Question.answerChar = " + answerChar);
            }
         } else {
            debug("Improper question format for multiple choice question: " + questionObj);
         }
      }

      if (log.isDebugEnabled()) {
         debug("Question " + questionNumber + ": " + testQuestion);
      }

      this.testQuestions.add(testQuestion);
   }

   public synchronized void onMessage(String username, String messageText) {
      if (messageText.toLowerCase().startsWith("!start")) {
         if (this.getGameState() == BotData.BotStateEnum.NO_GAME) {
            if (messageText.trim().length() > "!start".length()) {
               String parameter = messageText.trim().substring("!start".length() + 1);
               if (StringUtils.hasLength(parameter) && this.checkCategoryParameter(parameter, username)) {
                  try {
                     this.startGame(username);
                  } catch (Exception var6) {
                     log.error("Error starting game with custom category. Command was : '" + messageText + "'", var6);
                  }
               }
            } else {
               try {
                  this.startGame(username);
               } catch (Exception var5) {
                  log.error("Error starting game: ", var5);
               }
            }
         } else {
            this.sendGameCannotBeStartedMessage(username);
         }
      } else if (messageText.startsWith("!") && messageText.length() > "!".length() && this.currentQuestion != null) {
         if (this.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
            if (this.questionParticipants.contains(username)) {
               if (log.isDebugEnabled()) {
                  debug(username + " already answered this question.");
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
         if (this.answerTimerTask != null && !this.answerTimerTask.isCancelled()) {
            this.answerTimerTask.cancel(false);
            if (log.isDebugEnabled()) {
               debug("User " + username + " answered the question correctly. Timer canceled? " + this.answerTimerTask.isCancelled() + ". Or was the time up(done)? " + this.answerTimerTask.isDone());
            }
         }

         this.sendChannelMessage(this.createMessage("CORRECT_ANSWER", username, (TestTrivia.Score)this.scores.get(username), scoreboard));
         this.sendChannelMessage(this.createMessage("SCORES", (String)null, (TestTrivia.Score)null, scoreboard));
         this.setNextQuestion();
      } else {
         this.sendChannelMessage(this.createMessage("INCORRECT_ANSWER", username, (TestTrivia.Score)this.scores.get(username), (String)null));
      }

   }

   private boolean checkCategoryParameter(String parameter, String username) {
      boolean isValid = false;
      if (StringUtils.hasLength(parameter)) {
         try {
            int categ = Integer.parseInt(parameter);
            synchronized(this.category) {
               this.category = TriviaQuestionCategoryEnum.fromValue(categ);
            }

            if (this.category != null) {
               isValid = true;
               debug("user [" + username + "] is starting a Trivia game in category: " + this.category.name());
               this.sendMessage(this.createMessage("CATEGORY", username), username);
            } else {
               this.category = TriviaQuestionCategoryEnum.ENGLISH;
               debug("user [" + username + "] specified an invalid category: " + parameter + ".");
               this.sendMessage(this.createMessage("INVALID_CATEGORY", username, (TestTrivia.Score)null, (String)null, parameter), username);
            }
         } catch (NumberFormatException var8) {
            debug("user [" + username + "] specified an invalid category: " + parameter + ".");
            this.sendMessage(this.createMessage("INVALID_CATEGORY", username, (TestTrivia.Score)null, (String)null, parameter), username);
         }
      }

      return isValid;
   }

   private int updateUserScore(String username) {
      TestTrivia.Score score = (TestTrivia.Score)this.scores.get(username);
      if (score == null) {
         score = new TestTrivia.Score(username, 1);
      } else {
         score.incrementScore(1);
      }

      this.scores.put(username, score);
      return score.points;
   }

   public String getScoreboard() {
      List<TestTrivia.Score> orderedScores = new ArrayList();
      Iterator i$ = this.scores.values().iterator();

      while(i$.hasNext()) {
         TestTrivia.Score score = (TestTrivia.Score)i$.next();
         if (score.getScore() > 0) {
            orderedScores.add(score);
         }
      }

      Collections.sort(orderedScores);
      StringBuilder scoreString = new StringBuilder();

      for(int i = 0; i < orderedScores.size(); ++i) {
         TestTrivia.Score score = (TestTrivia.Score)orderedScores.get(i);
         scoreString.append(score.getPlayer() + " - " + score.getScore());
         if (i < orderedScores.size() - 1) {
            scoreString.append(", ");
         }
      }

      return scoreString.toString();
   }

   synchronized void setNextQuestion() {
      this.questionParticipants.clear();
      this.firstCorrectAnswerBy = null;
      if (this.currentQuestionNumber == this.numQuestions) {
         this.endGame();
      } else {
         if (this.currentQuestionNumber == 0) {
            this.currentQuestion = (TestTrivia.TestQuestion)this.testQuestions.get(0);
         } else {
            this.currentQuestion = (TestTrivia.TestQuestion)this.testQuestions.get(this.currentQuestionNumber);
         }

         ++this.currentQuestionNumber;
         String questionMessage = this.currentQuestionNumber == this.numQuestions ? this.createMessage("LAST_QUESTION") : this.createMessage("NTH_QUESTION");
         this.sendChannelMessage(questionMessage);
         this.answerTimerTask = executor.schedule(new TestTrivia.TimedCountdownTask(this, this.currentQuestionNumber), this.timeToAnswer, TimeUnit.SECONDS);
      }

   }

   private boolean checkAnswer(String messageText) {
      return this.currentQuestion.isCorrectAnswer(messageText);
   }

   public void startGame(String username) throws Exception {
      String message;
      if (this.gameState.equals(BotData.BotStateEnum.NO_GAME)) {
         message = this.createMessage("STARTGAME-NOTICE", username);
         this.setGameState(BotData.BotStateEnum.GAME_STARTING);
         this.gameStarter = username;
         this.sendChannelMessage(message);
         executor.execute(new TestTrivia.StartGame());
      } else {
         message = null;
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
            message = this.amountStartGame > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
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
         this.sendChannelMessage(this.createMessage("GAME_OVER", (String)null, (TestTrivia.Score)null, scoreboard));
      } else {
         this.sendChannelMessage(this.createMessage("GAME_OVER_NO_WINNER", (String)null, (TestTrivia.Score)null, (String)null));
      }

      this.resetGame();
      this.sendChannelMessage(this.amountStartGame > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
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

   static void debug(Object message) {
      System.out.println(message);
   }

   protected String createMessage(String messageKey) {
      return this.createMessage(messageKey, (String)null, (TestTrivia.Score)null, (String)null);
   }

   String createMessage(String messageKey, String player) {
      return this.createMessage(messageKey, player, (TestTrivia.Score)null, (String)null);
   }

   String createMessage(String messageKey, String player, TestTrivia.Score currentPlayerScore, String scoreboard) {
      return this.createMessage(messageKey, player, currentPlayerScore, scoreboard, (String)null);
   }

   private String createMessage(String messageKey, String player, TestTrivia.Score currentPlayerScore, String scoreboard, String errorInput) {
      try {
         if (log.isDebugEnabled()) {
            debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         messageToSend = messageToSend.replaceAll("BOTNAME", "TriviaBot");
         messageToSend = messageToSend.replaceAll("TIMER_ANSWER", "" + this.timeToAnswer);
         messageToSend = messageToSend.replaceAll("NUM_QUESTIONS", this.numQuestions + "");
         if (this.currentQuestion != null) {
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

         messageToSend = messageToSend.replaceAll("AMOUNT_START", this.amountStartGame / 100.0D + "");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         if (this.category != null) {
            messageToSend = messageToSend.replaceAll("CATEGORY", this.category.name());
         }

         if (StringUtils.hasLength(errorInput)) {
            messageToSend = messageToSend.replaceAll("ERROR_INPUT", errorInput);
         }

         return messageToSend;
      } catch (NullPointerException var7) {
         debug("Outgoing message could not be created, key = " + messageKey + " " + var7);
         return "";
      }
   }

   public static void main(String[] args) {
      try {
         SecureRandom random = new SecureRandom();
         System.out.println("Starting Trivia test game");
         TestTrivia t = new TestTrivia();
         Thread.sleep(1000L);
         int numPlayers = 25;
         List<String> testPlayers = new ArrayList();

         int playerChoice;
         for(playerChoice = 0; playerChoice < numPlayers; ++playerChoice) {
            testPlayers.add("dave" + playerChoice);
         }

         t.onMessage("dave0", "!start");

         while(true) {
            if (t.getGameState() == BotData.BotStateEnum.PLAYING) {
               for(; t.getGameState() == BotData.BotStateEnum.PLAYING; Thread.sleep(1000L)) {
                  debug("Next round of typing......................................");
                  playerChoice = random.nextInt(testPlayers.size());
                  int commandChoice = random.nextInt(3);
                  String message = "";
                  switch(commandChoice) {
                  case 0:
                     if (t.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
                        message = "C";
                     } else {
                        message = "just some text";
                     }
                     break;
                  case 1:
                     if (t.currentQuestion != null) {
                        if (t.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
                           message = t.currentQuestion.answerChar + "";
                        } else {
                           message = t.currentQuestion.answer;
                        }
                     }
                     break;
                  default:
                     message = "!wrong answer text";
                  }

                  String player = (String)testPlayers.get(playerChoice);
                  debug(player + " sends " + "!" + message);
                  t.onMessage(player, "!" + message);
                  if (random.nextDouble() < 0.1D && numPlayers > 0) {
                     String user = "dave" + random.nextInt(numPlayers);
                     t.onUserLeaveChannel(user);
                     testPlayers.remove(user);
                     --numPlayers;
                  }
               }

               System.out.println("Bye");
               break;
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   class Score implements Comparable<TestTrivia.Score> {
      String player;
      int points;

      public Score(String player, int points) {
         this.player = player;
         this.points = points;
      }

      public String getPlayer() {
         return this.player;
      }

      public int getScore() {
         return this.points;
      }

      public void incrementScore(int points) {
         this.points += points;
      }

      public boolean equals(Object scoreObj) {
         if (scoreObj != null && scoreObj instanceof TestTrivia.Score) {
            TestTrivia.Score score = (TestTrivia.Score)scoreObj;
            return this.player.equals(score.player) && this.points == score.points;
         } else {
            return false;
         }
      }

      public int compareTo(TestTrivia.Score o) {
         if (this.points > o.points) {
            return -1;
         } else {
            return this.points > o.points ? 0 : 1;
         }
      }
   }

   class TimedCountdownTask implements Runnable {
      TestTrivia bot;
      int questionNumber = -1;

      TimedCountdownTask(TestTrivia bot, int questionNumber) {
         this.bot = bot;
         this.questionNumber = questionNumber;
      }

      public int getQuestionNumber() {
         return this.questionNumber;
      }

      public void run() {
         synchronized(this.bot) {
            if (this.bot.getGameState() == BotData.BotStateEnum.PLAYING && this.bot.currentQuestionNumber == this.questionNumber) {
               this.bot.sendChannelMessage(this.bot.createMessage("TIME_UP"));
               if (this.bot.currentQuestion.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE && this.bot.firstCorrectAnswerBy != null) {
                  String scoreboard = this.bot.getScoreboard();
                  this.bot.sendChannelMessage(this.bot.createMessage("FIRST_CORRECT_ANSWER", this.bot.firstCorrectAnswerBy, (TestTrivia.Score)this.bot.scores.get(this.bot.firstCorrectAnswerBy), scoreboard));
                  this.bot.sendChannelMessage(this.bot.createMessage("SCORES", (String)null, (TestTrivia.Score)null, scoreboard));
               }

               this.bot.setNextQuestion();
            }

         }
      }
   }

   class TestQuestion {
      int questionID;
      TriviaQuestionTypeEnum type;
      String question;
      String answer;
      char answerChar;

      public TestQuestion(int questionID, TriviaQuestionTypeEnum type, String question, String answer) {
         this.questionID = questionID;
         this.type = type;
         this.question = question;
         this.answer = answer;
      }

      public void setAnswerChar(char answerChar) {
         if (this.type == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
            this.answerChar = answerChar;
         }

      }

      public boolean isCorrectAnswer(String a) {
         boolean var10000;
         label27: {
            if (StringUtils.hasLength(a)) {
               if (this.type.value() == 0) {
                  if (a.toLowerCase().contains(this.answer)) {
                     break label27;
                  }
               } else if (a.length() == 1 && a.trim().toLowerCase().charAt(0) == this.answerChar) {
                  break label27;
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      }

      public String toString() {
         return "Type: " + this.type.name() + ", Question: " + this.question + ", Answer: " + this.answer;
      }
   }

   class StartGame implements Runnable {
      public void run() {
         if (TestTrivia.this.getGameState() == BotData.BotStateEnum.GAME_STARTING) {
            TestTrivia.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
            String message = TestTrivia.this.createMessage("GAME_STARTED_NOTE");
            TestTrivia.this.sendChannelMessage(message);
            TestTrivia.this.setGameState(BotData.BotStateEnum.PLAYING);
            TestTrivia.this.loadQuestions();
            TestTrivia.this.setNextQuestion();
         }

      }
   }
}
