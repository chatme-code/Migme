package com.projectgoth.fusion.botservice.bot.migbot.trivia.enums;

public enum TriviaQuestionTypeEnum {
   OPEN(0),
   MULTIPLE_CHOICE(1);

   private int value;
   public static final int ID_OPEN = 0;
   public static final int ID_MULTIPLE_CHOICE = 1;

   private TriviaQuestionTypeEnum(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public static TriviaQuestionTypeEnum fromValue(int value) {
      TriviaQuestionTypeEnum[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         TriviaQuestionTypeEnum e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }
}
