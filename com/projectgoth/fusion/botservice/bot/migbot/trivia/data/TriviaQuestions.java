package com.projectgoth.fusion.botservice.bot.migbot.trivia.data;

import com.projectgoth.fusion.botservice.bot.migbot.trivia.Question;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionCategoryEnum;
import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MessageBundle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class TriviaQuestions {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(TriviaQuestions.class));
   private static final String BUNDLE_NAME = "resource.Trivia_Questions";
   public static List<Question> englishQuestions = new ArrayList();
   public static List<Question> bahasaQuestions = new ArrayList();

   public static List<Question> chooseRandomQuestions(int itemsToSelect) {
      return chooseRandomQuestions(TriviaQuestionCategoryEnum.ENGLISH, itemsToSelect);
   }

   public static List<Question> chooseRandomQuestions(TriviaQuestionCategoryEnum category, int itemsToSelect) {
      Random random = new Random();
      List<Question> questions = getQuestionSet(category);
      int sourceSize = questions.size();
      int[] selections = new int[itemsToSelect];
      ArrayList<Question> resultArray = new ArrayList();

      for(int count = 0; count < itemsToSelect; ++count) {
         int selection = random.nextInt(sourceSize - count);
         selections[count] = selection;

         for(int scanIdx = count - 1; scanIdx >= 0; --scanIdx) {
            if (selection >= selections[scanIdx]) {
               ++selection;
            }
         }

         resultArray.add(questions.get(selection));
      }

      return resultArray;
   }

   private static List<Question> getQuestionSet(TriviaQuestionCategoryEnum category) {
      switch(category.value()) {
      case 1:
         if (englishQuestions.isEmpty()) {
            englishQuestions = loadQuestions(Locale.ENGLISH);
         }

         return englishQuestions;
      case 2:
         if (bahasaQuestions.isEmpty()) {
            bahasaQuestions = loadQuestions(new Locale("id_ID_", "id", "ID"));
         }

         return bahasaQuestions;
      default:
         return englishQuestions;
      }
   }

   private static List<Question> loadQuestions(Locale locale) throws MissingResourceException {
      List<Question> questions = new ArrayList();
      ResourceBundle englishQuestions = MessageBundle.getBundle("resource.Trivia_Questions", locale);
      if (englishQuestions != null) {
         Enumeration keys = englishQuestions.getKeys();

         while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = englishQuestions.getString(key);

            try {
               int questionNumber = Integer.parseInt(key);
               questions.add(createQuestion(questionNumber, value));
            } catch (NumberFormatException var7) {
               log.warn("Unable to parse the question number: " + key);
            }
         }
      }

      return questions;
   }

   private static Question createQuestion(int questionNumber, String question) {
      StringTokenizer tokenizer = new StringTokenizer(question, "\t");
      TriviaQuestionTypeEnum typeEnum = null;
      String questionTypeStr = tokenizer.nextToken();
      boolean var5 = true;

      try {
         int type = Integer.parseInt(questionTypeStr);
         typeEnum = TriviaQuestionTypeEnum.fromValue(type);
      } catch (Exception var10) {
         log.warn("Error parsing question string: " + question);
         typeEnum = TriviaQuestionTypeEnum.OPEN;
      }

      String questionStr = tokenizer.nextToken();
      String answer = tokenizer.nextToken();
      Question questionObj = new Question(questionNumber, typeEnum, questionStr, answer.toLowerCase());
      if (typeEnum == TriviaQuestionTypeEnum.MULTIPLE_CHOICE) {
         if (tokenizer.hasMoreTokens()) {
            char answerChar = tokenizer.nextToken().toLowerCase().charAt(0);
            questionObj.setAnswerChar(answerChar);
            if (log.isDebugEnabled()) {
               log.debug("Question.answerChar = " + answerChar);
            }
         } else {
            log.warn("Improper question format for multiple choice question: " + question);
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("Question " + questionNumber + ": " + question);
      }

      return questionObj;
   }

   public static void main(String[] args) {
      List<Question> questionList = chooseRandomQuestions(TriviaQuestionCategoryEnum.ENGLISH, 10);
      Iterator i$ = questionList.iterator();

      while(i$.hasNext()) {
         Question question = (Question)i$.next();
         System.out.println(question);
      }

   }
}
