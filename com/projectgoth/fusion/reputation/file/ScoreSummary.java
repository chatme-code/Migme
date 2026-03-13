package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.reputation.domain.ScoreCategory;

public class ScoreSummary {
   public static final int USERNAME_INDEX = 0;
   public static final int CHATROOM_MESSAGES_SENT_INDEX = 1;
   public static final int PRIVATE_MESSAGES_SENT_INDEX = 2;
   public static final int TOTAL_TIME_INDEX = 3;
   public static final int PHOTOS_UPLOADED_INDEX = 4;
   public static final int KICKS_INDEX = 5;
   public static final int AUTHENTICATED_REFERRAL_INDEX = 6;
   public static final int RECHARGED_AMOUNT_INDEX = 7;
   public static final int VIRTUAL_GIFT_RECEIVED_INDEX = 8;
   public static final int VIRTUAL_GIFT_SENT_INDEX = 9;
   public static final int CALL_DURATION_INDEX = 10;
   public static final int TIME_IN_PRODUCT_CATEGORY_INDEX;
   public static final int CREDITS_SPENT_CATEGORY_INDEX;
   public static final int HUMAN_LIKE_BEHAVIOUR_CATEGORY_INDEX;
   public static final int BASIC_ACTIVITY_CATEGORY_INDEX;
   public static final int TOTAL_SCORE_INDEX;
   public static final int[] LINE_PARTS;
   public static final int EXPECTED_INPUT_FIELD_COUNT;
   public static final int EXPECTED_FIELD_COUNT;
   public static final char DELIMETER = ',';

   public static void main(String[] args) {
      System.out.println(10);
      System.out.println(TIME_IN_PRODUCT_CATEGORY_INDEX);
      System.out.println(CREDITS_SPENT_CATEGORY_INDEX);
      System.out.println(HUMAN_LIKE_BEHAVIOUR_CATEGORY_INDEX);
      System.out.println(BASIC_ACTIVITY_CATEGORY_INDEX);
      System.out.println(TOTAL_SCORE_INDEX);
      System.out.println(EXPECTED_INPUT_FIELD_COUNT);
      System.out.println(EXPECTED_FIELD_COUNT);
   }

   static {
      TIME_IN_PRODUCT_CATEGORY_INDEX = 11 + ScoreCategory.TIME_IN_PRODUCT.value();
      CREDITS_SPENT_CATEGORY_INDEX = TIME_IN_PRODUCT_CATEGORY_INDEX + ScoreCategory.CREDITS_SPENT.value();
      HUMAN_LIKE_BEHAVIOUR_CATEGORY_INDEX = TIME_IN_PRODUCT_CATEGORY_INDEX + ScoreCategory.HUMAN_LIKELY_BEHAVIOUR.value();
      BASIC_ACTIVITY_CATEGORY_INDEX = TIME_IN_PRODUCT_CATEGORY_INDEX + ScoreCategory.BASIC_ACTIVITY.value();
      TOTAL_SCORE_INDEX = BASIC_ACTIVITY_CATEGORY_INDEX + 1;
      LINE_PARTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
      EXPECTED_INPUT_FIELD_COUNT = LINE_PARTS.length;
      EXPECTED_FIELD_COUNT = EXPECTED_INPUT_FIELD_COUNT + 1 + 4;
   }
}
