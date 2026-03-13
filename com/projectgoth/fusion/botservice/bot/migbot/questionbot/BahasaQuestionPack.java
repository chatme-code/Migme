package com.projectgoth.fusion.botservice.bot.migbot.questionbot;

import java.util.Locale;

public class BahasaQuestionPack extends QuestionPack {
   public BahasaQuestionPack(String bundleName, Locale locale) {
      this.questions = loadQuestions(bundleName, locale);
   }
}
