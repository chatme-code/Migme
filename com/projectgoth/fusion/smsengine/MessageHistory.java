package com.projectgoth.fusion.smsengine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MessageHistory {
   private static final int HISTORY_SIZE = 100000;
   private static long[] idArray = new long[100000];
   private static Set<Long> idSet = new HashSet(100000);
   private static int top = 0;

   public static boolean add(long id) {
      synchronized(idSet) {
         if (idSet.contains(id)) {
            return false;
         } else {
            idSet.remove(idArray[top]);
            idArray[top] = id;
            top = ++top % 100000;
            idSet.add(id);
            return true;
         }
      }
   }

   public static void remove(long id) {
      synchronized(idSet) {
         idSet.remove(id);
      }
   }

   static {
      Arrays.fill(idArray, Long.MIN_VALUE);
   }
}
