package com.projectgoth.fusion.botservice.bot.migbot.questionbot;

import com.projectgoth.fusion.common.StringUtil;
import java.util.Locale;

public class QuestionPackFactory {
   public static QuestionPack getQuestionPack(String name, String bundleName, Locale locale) {
      if (!StringUtil.isBlank(name) && !name.equals("questions")) {
         return name.equals("taring") ? new BahasaQuestionPack(bundleName, locale) : null;
      } else {
         return new DefaultQuestionPack(bundleName, locale);
      }
   }
}
