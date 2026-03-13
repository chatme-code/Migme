package com.projectgoth.fusion.reputation.util;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.reputation.cache.ReputationLastRan;

public class SetLastIds {
   public static MemCachedClient memCached;

   public static void main(String[] args) {
      if (args.length < 5) {
         System.out.println("ERROR");
         System.out.println("usage: SetLastIds <session archive id> <account entry id> <phone call id> <virtual gift received id> <session archive table name>");
         System.exit(1);
      }

      ReputationLastRan.setSessionArchiveLastId(memCached, Integer.parseInt(args[0]));
      ReputationLastRan.setAccountEntryLastId(memCached, Integer.parseInt(args[1]));
      ReputationLastRan.setPhoneCallLastId(memCached, Integer.parseInt(args[2]));
      ReputationLastRan.setVirtualGiftLastId(memCached, Integer.parseInt(args[3]));
      ReputationLastRan.setSessionArchiveTableName(memCached, args[4]);
      System.out.println(ReputationLastRan.getSessionArchiveLastId(memCached));
      System.out.println(ReputationLastRan.getAccountEntryLastId(memCached));
      System.out.println(ReputationLastRan.getPhoneCallLastId(memCached));
      System.out.println(ReputationLastRan.getVirtualGiftLastId(memCached));
      System.out.println(ReputationLastRan.getSessionArchiveTableName(memCached));
   }

   static {
      memCached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }
}
