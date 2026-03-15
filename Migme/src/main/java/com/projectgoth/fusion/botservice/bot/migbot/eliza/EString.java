package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class EString {
   static final String num = "0123456789";

   public static int amatch(String str, String pat) {
      int count = 0;
      int i = 0;
      int j = 0;

      while(true) {
         if (i < str.length() && j < pat.length()) {
            char p = pat.charAt(j);
            if (p != '*' && p != '#') {
               if (str.charAt(i) != p) {
                  return -1;
               }

               ++i;
               ++j;
               ++count;
               continue;
            }

            return count;
         }

         return count;
      }
   }

   public static int findPat(String str, String pat) {
      int count = 0;

      for(int i = 0; i < str.length(); ++i) {
         if (amatch(str.substring(i), pat) >= 0) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   public static int findNum(String str) {
      int count = 0;

      for(int i = 0; i < str.length(); ++i) {
         if ("0123456789".indexOf(str.charAt(i)) == -1) {
            return count;
         }

         ++count;
      }

      return count;
   }

   static boolean matchA(String str, String pat, String[] matches) {
      int i = 0;
      int j = 0;
      int pos = 0;

      while(pos < pat.length() && j < matches.length) {
         char p = pat.charAt(pos);
         int n;
         if (p == '*') {
            if (pos + 1 == pat.length()) {
               n = str.length() - i;
            } else {
               n = findPat(str.substring(i), pat.substring(pos + 1));
            }

            if (n < 0) {
               return false;
            }

            matches[j++] = str.substring(i, i + n);
            i += n;
            ++pos;
         } else if (p == '#') {
            n = findNum(str.substring(i));
            matches[j++] = str.substring(i, i + n);
            i += n;
            ++pos;
         } else {
            n = amatch(str.substring(i), pat.substring(pos));
            if (n <= 0) {
               return false;
            }

            i += n;
            pos += n;
         }
      }

      return i >= str.length() && pos >= pat.length();
   }

   public static boolean match(String str, String pat, String[] matches) {
      return matchA(str, pat, matches);
   }

   public static String translate(String str, String src, String dest) {
      if (src.length() != dest.length()) {
      }

      for(int i = 0; i < src.length(); ++i) {
         str = str.replace(src.charAt(i), dest.charAt(i));
      }

      return str;
   }

   public static String compress(String s) {
      String dest = "";
      if (s.length() == 0) {
         return s;
      } else {
         char c = s.charAt(0);

         for(int i = 1; i < s.length(); ++i) {
            if (c != ' ' || s.charAt(i) != ' ' && s.charAt(i) != ',' && s.charAt(i) != '.') {
               if (c != ' ' && s.charAt(i) == '?') {
                  dest = dest + c + " ";
               } else {
                  dest = dest + c;
               }
            }

            c = s.charAt(i);
         }

         dest = dest + c;
         return dest;
      }
   }

   public static String trim(String s) {
      for(int i = 0; i < s.length(); ++i) {
         if (s.charAt(i) != ' ') {
            return s.substring(i);
         }
      }

      return "";
   }

   public static String pad(String s) {
      if (s.length() == 0) {
         return " ";
      } else {
         char first = s.charAt(0);
         char last = s.charAt(s.length() - 1);
         if (first == ' ' && last == ' ') {
            return s;
         } else if (first == ' ' && last != ' ') {
            return s + " ";
         } else if (first != ' ' && last == ' ') {
            return " " + s;
         } else {
            return first != ' ' && last != ' ' ? " " + s + " " : s;
         }
      }
   }

   public static int count(String s, char c) {
      int count = 0;

      for(int i = 0; i < s.length(); ++i) {
         if (s.charAt(i) == c) {
            ++count;
         }
      }

      return count;
   }
}
