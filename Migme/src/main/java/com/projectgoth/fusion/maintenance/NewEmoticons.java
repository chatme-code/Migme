package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;

public class NewEmoticons {
   private static int startEmoticonPackID = 39;
   private static int startEmoticonID = 1153;
   private static int startEmoticonHotkeyID = 1360;
   private static String packName = "Clock Pack";
   private static String packPriceAUD = "0.28";
   private static String packDirectoryName = "time";
   private static String[][] emoticons = new String[][]{{"one", "(time_one)", "one"}, {"two", "(time_two)", "two"}, {"three", "(time_three)", "three"}, {"four", "(time_four)", "four"}, {"five", "(time_five)", "five"}, {"six", "(time_six)", "six"}, {"seven", "(time_seven)", "seven"}, {"eight", "(time_eight)", "eight"}, {"nine", "(time_nine)", "nine"}, {"ten", "(time_ten)", "ten"}, {"eleven", "(time_eleven)", "eleven"}, {"twelve", "(time_twelve)", "twelve"}};

   public static void main(String[] args) {
      createInsertStatements();
   }

   private static void createInsertStatements() {
      System.out.println("INSERT INTO emoticonpack (id, type, name, price, forsale, status) VALUES (" + startEmoticonPackID + ", " + EmoticonPackData.TypeEnum.PREMIUM_PURCHASE.value() + ", '" + packName + "', " + packPriceAUD + ", 0, 1);");

      for(int i = 0; i < emoticons.length; ++i) {
         for(int size = 12; size <= 16; size += 2) {
            System.out.println("INSERT INTO emoticon (id, emoticonpackid, type, alias, width, height, location, locationpng) VALUES (" + startEmoticonID + ", " + startEmoticonPackID + ", " + EmoticonData.TypeEnum.IMAGE.value() + ", '" + emoticons[i][0] + "', " + size + ", " + size + ", " + "'/usr/fusion/emoticons/" + packDirectoryName + "/" + emoticons[i][2] + "_" + size + ".gif', " + "'/usr/fusion/emoticons/" + packDirectoryName + "/" + emoticons[i][2] + "_" + size + ".png');");
            System.out.println("INSERT INTO emoticonhotkey (id, emoticonid, type, hotkey) VALUES (" + startEmoticonHotkeyID + ", " + startEmoticonID + ", " + EmoticonData.HotKeyTypeEnum.PRIMARY.value() + ", '" + emoticons[i][1] + "');");
            ++startEmoticonHotkeyID;
            ++startEmoticonID;
         }
      }

   }
}
