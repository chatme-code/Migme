package com.projectgoth.fusion.botservice.bot.migbot.trivia.enums;

import java.util.ArrayList;
import java.util.List;

public enum TriviaQuestionCategoryEnum {
   ENGLISH(1),
   BAHASA(2);

   private int value;
   public static final int ID_ENGLISH = 1;
   public static final int ID_BAHASA = 2;

   private TriviaQuestionCategoryEnum(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public static TriviaQuestionCategoryEnum fromValue(int value) {
      TriviaQuestionCategoryEnum[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         TriviaQuestionCategoryEnum e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }

   public static List<String> getCategoryList() {
      List<String> categoryList = new ArrayList();
      TriviaQuestionCategoryEnum[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         TriviaQuestionCategoryEnum e = arr$[i$];
         categoryList.add(e.name() + " - " + e.value());
      }

      return categoryList;
   }
}
