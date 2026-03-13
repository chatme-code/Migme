package com.projectgoth.fusion.botservice.bot.migbot.questionbot;

import java.util.Locale;

public class DefaultQuestionPack extends QuestionPack {
   public DefaultQuestionPack(String bundleName, Locale locale) {
      this.questions = loadQuestions(bundleName, locale);
   }
}
