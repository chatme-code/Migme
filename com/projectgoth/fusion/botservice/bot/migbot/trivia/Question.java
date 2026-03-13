package com.projectgoth.fusion.botservice.bot.migbot.trivia;

import com.projectgoth.fusion.botservice.bot.migbot.trivia.enums.TriviaQuestionTypeEnum;
import org.springframework.util.StringUtils;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class Question {
   int questionID;
   TriviaQuestionTypeEnum type;
   String question;
   String answer;
   char answerChar;
   private AbstractStringMetric metric = new Levenshtein();
   private double similarityScoreThreshold = 0.7D;

   public Question(int questionID, TriviaQuestionTypeEnum type, String question, String answer) {
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
               if (this.isSimilarAnswer(a)) {
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

   private boolean isSimilarAnswer(String guess) {
      if (this.answer.length() > 4) {
         float stringSimilarity = this.metric.getSimilarity(guess, this.answer);
         return (double)stringSimilarity >= this.similarityScoreThreshold;
      } else {
         return this.answer.equalsIgnoreCase(guess);
      }
   }
}
