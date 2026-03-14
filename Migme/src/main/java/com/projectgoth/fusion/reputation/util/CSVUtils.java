package com.projectgoth.fusion.reputation.util;

public class CSVUtils {
   public static String getColumnFromLine(String line, int column, char delimeter) {
      if (line != null && line.length() != 0) {
         int index = 0;
         int currentColumn = 0;
         if (column > 0) {
            while(currentColumn++ < column && index < line.length() - 1) {
               index = line.indexOf(delimeter, index + 1);
            }

            if (index >= line.length() - 1 || index < 0) {
               return null;
            }
         }

         int finalIndex = line.indexOf(delimeter, index + 1);
         if (column > 0) {
            ++index;
         }

         return finalIndex < 0 ? line.substring(index) : line.substring(index, finalIndex);
      } else {
         return null;
      }
   }

   public static String getColumnFromLineAfter(String line, int column, char delimeter, String after) {
      if (line != null && line.length() != 0) {
         int index = line.indexOf(after) + after.length();
         int currentColumn = 0;
         if (column > 0) {
            while(currentColumn++ < column && index < line.length() - 1) {
               index = line.indexOf(delimeter, index + 1);
            }

            if (index >= line.length() - 1 || index < 0) {
               return null;
            }
         }

         int finalIndex = line.indexOf(delimeter, index + 1);
         if (column > 0) {
            ++index;
         }

         return finalIndex < 0 ? line.substring(index) : line.substring(index, finalIndex);
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      System.out.println(getColumnFromLine("100,1,102,3,104", 0, ','));
      System.out.println(getColumnFromLine("100,1,102,3,104", 1, ','));
      System.out.println(getColumnFromLine("100,1,102,3,104", 2, ','));
      System.out.println(getColumnFromLine("100,1,102,3,104", 3, ','));
      System.out.println(getColumnFromLine("100,1,102,3,104", 4, ','));
      System.out.println(getColumnFromLine("100,1,102,3,104", 5, ','));
      System.out.println(getColumnFromLineAfter("bla bla - 100,1,102,3,104", 0, ',', "- "));
      System.out.println(getColumnFromLine("bla bla - 100,1,102,3,104", 2, ','));
   }
}
