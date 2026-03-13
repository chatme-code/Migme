package com.projectgoth.fusion.botservice.bot.migbot.questionbot;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MessageBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public abstract class QuestionPack {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(QuestionBot.class));
   protected List<Question> questions = new ArrayList();
   private int nextQuestionIndex = 0;
   private boolean shuffled = false;

   public Question getNextQuestion() {
      if (!this.shuffled) {
         Collections.shuffle(this.questions);
         this.shuffled = true;
      }

      Question nextQuestion = (Question)this.questions.get(this.nextQuestionIndex);
      ++this.nextQuestionIndex;
      if (this.nextQuestionIndex >= this.questions.size()) {
         Collections.shuffle(this.questions);
         this.nextQuestionIndex = 0;
      }

      return nextQuestion;
   }

   protected static List<Question> loadQuestions(String bundleName, Locale locale) throws MissingResourceException {
      List<Question> questions = new ArrayList();
      ResourceBundle questionsRb = MessageBundle.getBundle(bundleName, locale);
      if (questionsRb != null) {
         Enumeration keys = questionsRb.getKeys();

         while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = questionsRb.getString(key);

            try {
               int questionNumber = Integer.parseInt(key);
               Question question = createQuestion(questionNumber, value);
               if (question != null) {
                  questions.add(question);
               }
            } catch (NumberFormatException var9) {
               log.warn("Unable to parse the question number: " + key);
            }
         }
      } else {
         log.warn("Couldn't retrieve resource bundle for question pack");
      }

      return questions;
   }

   private static Question createQuestion(int questionNumber, String question) {
      StringTokenizer tokenizer = new StringTokenizer(question, "\t");
      String questionTypeStr = tokenizer.nextToken();
      boolean var4 = true;

      int type;
      try {
         type = Integer.parseInt(questionTypeStr);
      } catch (Exception var9) {
         log.warn("Error parsing question string: " + question);
         return null;
      }

      String questionStr = "";
      String answer = "";

      try {
         questionStr = tokenizer.nextToken();
         answer = tokenizer.nextToken();
      } catch (NoSuchElementException var8) {
         log.error("Error retrieving next token. Question was: " + question);
         return null;
      }

      return new Question(type, questionStr, answer);
   }
}
