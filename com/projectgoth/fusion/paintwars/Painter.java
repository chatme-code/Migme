package com.projectgoth.fusion.paintwars;

import com.projectgoth.fusion.botservice.bot.migbot.common.LimitTracker;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class Painter {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Painter.class));
   private static final int DAY_IN_SECONDS = 86400;
   private static final int RECORD_EXPIRY_TIME = 2592000;
   private static final int INVENTORY_LIMIT = 30;
   private static final int DEFAULT_REQUIRED_LEVEL = 1;
   private static int freePaintsPerDay = 3;
   private static int freeCleansPerDay = 2;
   private static final String CURRENCY = "USD";
   private static double priceOfPaintCredit = 0.01D;
   private static double priceOfCleanCredit = 0.02D;
   private static double priceOfIdenticon = 0.01D;
   private static int paintsSentEligibility = 35;
   private static int cleansSentEligibility = 21;
   private static final String PRICE_OF_PAINT_CREDIT = "PriceOfPaintCredit";
   private static final String PRICE_OF_CLEAN_CREDIT = "PriceOfCleanCredit";
   private static final String PRICE_OF_IDENTICON = "PriceOfIdenticon";
   private static final String FREE_PAINTS_PER_DAY = "FreePaintsPerDay";
   private static final String FREE_CLEANS_PER_DAY = "FreeCleansPerDay";
   private static final String PAINTS_SENT_ELIGIBILITY = "PaintsSentEligibility";
   private static final String CLEANS_SENT_ELIGIBILITY = "CleansSentEligibility";

   public static int paint(String user1, String user2) throws FusionException {
      int userId = getUserId(user1);
      int targetUserId = getUserId(user2);
      long currentTime = getCurrentTimeInSeconds();
      String key = "";
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var11) {
         throw new FusionException(var11.getMessage());
      }

      key = Painter.KeySpace.USER.toString() + userId;
      int points = calculatePoints(user1, user2, true, isClean(user1));
      if (hasDualPaint(user1)) {
         points *= 2;
      }

      masterInstance.hincrBy(key, "TotalPaintWarsPoints", (long)points);
      masterInstance.hincrBy(key, "TotalPaintsSent", 1L);
      key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.PAINTS_SENT.toString();
      masterInstance.zadd(key, (double)currentTime, targetUserId + ":" + user2 + ":" + currentTime);
      Redis.disconnect(masterInstance, log);
      Leaderboard.recordGamesMetric(Leaderboard.Type.PAINT_WARS_PAINT_POINTS, user1, userId, points);
      checkRaffleEligibility(user1, userId, key, (double)currentTime, paintsSentEligibility);

      try {
         masterInstance = Redis.getMasterInstanceForUserID(targetUserId);
      } catch (Exception var10) {
         throw new FusionException(var10.getMessage());
      }

      key = Painter.KeySpace.USER.toString() + targetUserId;
      masterInstance.hincrBy(key, "TotalPaintsReceived", 1L);
      key = Painter.KeySpace.U.toString() + targetUserId + ":" + Painter.KeySpace.PAINTS_RECEIVED.toString();
      masterInstance.zadd(key, (double)currentTime, userId + ":" + user1 + ":" + currentTime);
      Redis.disconnect(masterInstance, log);
      log.info(user1 + " painted " + user2 + " and received " + points + " points");
      return points;
   }

   public static int clean(String user1, String user2) throws FusionException {
      int userId = getUserId(user1);
      int targetUserId = getUserId(user2);
      long currentTime = getCurrentTimeInSeconds();
      String key = "";
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var11) {
         throw new FusionException(var11.getMessage());
      }

      key = Painter.KeySpace.USER.toString() + userId;
      int points = calculatePoints(user1, user2, false, isClean(user1));
      masterInstance.hincrBy(key, "TotalPaintWarsPoints", (long)points);
      masterInstance.hincrBy(key, "TotalCleansSent", 1L);
      key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.CLEANS_SENT.toString();
      masterInstance.zadd(key, (double)currentTime, targetUserId + ":" + user2 + ":" + currentTime);
      Redis.disconnect(masterInstance, log);
      Leaderboard.recordGamesMetric(Leaderboard.Type.PAINT_WARS_PAINT_POINTS, user1, userId, points);
      checkRaffleEligibility(user1, userId, key, (double)currentTime, cleansSentEligibility);

      try {
         masterInstance = Redis.getMasterInstanceForUserID(targetUserId);
      } catch (Exception var10) {
         throw new FusionException(var10.getMessage());
      }

      key = Painter.KeySpace.USER.toString() + targetUserId;
      masterInstance.hincrBy(key, "TotalCleansReceived", 1L);
      key = Painter.KeySpace.U.toString() + targetUserId + ":" + Painter.KeySpace.CLEANS_RECEIVED.toString();
      masterInstance.zadd(key, (double)currentTime, userId + ":" + user1 + ":" + currentTime);
      Redis.disconnect(masterInstance, log);
      log.info(user1 + " cleaned " + user2 + " and received " + points + " points");
      return points;
   }

   public static boolean isClean(String user) throws FusionException {
      return isClean(user, (Integer)null);
   }

   public static boolean isClean(String user, Integer userId) throws FusionException {
      if (userId == null) {
         userId = getUserId(user);
      }

      double latestPaintTimestamp = 0.0D;
      double latestCleanTimestamp = 0.0D;
      String key = "";
      key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.PAINTS_RECEIVED.toString();
      Set<Tuple> paintsReceived = getSortedSet(userId, key, 0, 0);
      Iterator<Tuple> itr = paintsReceived.iterator();
      if (itr.hasNext()) {
         latestPaintTimestamp = ((Tuple)itr.next()).getScore();
         key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.CLEANS_RECEIVED.toString();
         Set var9 = getSortedSet(userId, key, 0, 0);
         itr = var9.iterator();
         if (itr.hasNext()) {
            latestCleanTimestamp = ((Tuple)itr.next()).getScore();
            if (latestPaintTimestamp > latestCleanTimestamp) {
               long previousTime = getCurrentTimeInSeconds() - 259200L;
               if (latestPaintTimestamp > (double)previousTime) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean hadInteraction(String user1, String user2) throws FusionException {
      int userId = getUserId(user1);
      int targetUserId = getUserId(user2);
      String key = "";
      key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.PAINTS_SENT.toString();
      Set<Tuple> paintsSent = getSortedSet(userId, key, 0, -1);
      if (containsWithinPeriod(paintsSent, targetUserId + ":" + user2, 86400)) {
         return true;
      } else {
         key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.CLEANS_SENT.toString();
         Set<Tuple> cleansSent = getSortedSet(userId, key, 0, -1);
         return containsWithinPeriod(cleansSent, targetUserId + ":" + user2, 86400);
      }
   }

   public static ArrayList<String> getAllTaggedUsers(String[] users) throws FusionException {
      ArrayList<String> taggedUsers = new ArrayList();

      for(int i = 0; i < users.length; ++i) {
         if (!isClean(users[i])) {
            taggedUsers.add(users[i]);
         }
      }

      return taggedUsers;
   }

   public static PainterStats getStats(String user) throws FusionException {
      int userId = getUserId(user);
      String key = Painter.KeySpace.USER.toString() + userId;
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var14) {
         throw new FusionException(var14.getMessage());
      }

      if (slaveInstance == null) {
         return new PainterStats();
      } else {
         String points = slaveInstance.hget(key, "TotalPaintWarsPoints");
         String paintsSent = slaveInstance.hget(key, "TotalPaintsSent");
         String paintsReceived = slaveInstance.hget(key, "TotalPaintsReceived");
         int paintsRemaining = getRemainingFreePaints(user);
         String cleansSent = slaveInstance.hget(key, "TotalCleansSent");
         String cleansReceived = slaveInstance.hget(key, "TotalCleansReceived");
         int cleansRemaining = getRemainingFreeCleans(user);
         Redis.disconnect(slaveInstance, log);
         PainterStats stats = new PainterStats();

         try {
            stats.setTotalPaintWarsPoints(StringUtil.isBlank(points) ? 0 : Integer.parseInt(points));
            stats.setTotalPaintsSent(StringUtil.isBlank(paintsSent) ? 0 : Integer.parseInt(paintsSent));
            stats.setTotalPaintsReceived(StringUtil.isBlank(paintsReceived) ? 0 : Integer.parseInt(paintsReceived));
            stats.setPaintsRemaining(paintsRemaining);
            stats.setTotalCleansSent(StringUtil.isBlank(cleansSent) ? 0 : Integer.parseInt(cleansSent));
            stats.setTotalCleansReceived(StringUtil.isBlank(cleansReceived) ? 0 : Integer.parseInt(cleansReceived));
            stats.setCleansRemaining(cleansRemaining);
            return stats;
         } catch (NumberFormatException var13) {
            throw new FusionException("Error parsing stats for user: " + user);
         }
      }
   }

   public static Vector<List<String>> getStatsDetails(String username, int type, int offset, int numberOfEntries) throws FusionException {
      Vector<List<String>> statsDetails = null;
      int userId = getUserId(username);
      String field = "";
      if (type == 1) {
         field = Painter.KeySpace.PAINTS_SENT.toString();
      } else if (type == 2) {
         field = Painter.KeySpace.PAINTS_RECEIVED.toString();
      } else if (type == 3) {
         field = Painter.KeySpace.CLEANS_SENT.toString();
      } else {
         if (type != 4) {
            throw new FusionException("Unknown getStatsDetails type");
         }

         field = Painter.KeySpace.CLEANS_RECEIVED.toString();
      }

      String key = Painter.KeySpace.U.toString() + userId + ":" + field;
      purgeExpiredRecords(userId, key);
      Set<Tuple> users = getSortedSet(userId, key, offset, offset + (numberOfEntries - 1));
      statsDetails = convertUserSetToVector(users);
      return statsDetails;
   }

   public static void purgeExpiredRecords(int userId, String key) throws FusionException {
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var8) {
         throw new FusionException(var8.getMessage());
      }

      Set<Tuple> sortedSet = masterInstance.zrangeWithScores(key, 0L, -1L);
      Iterator itr = sortedSet.iterator();

      while(itr.hasNext()) {
         Tuple member = (Tuple)itr.next();
         double score = member.getScore();
         if (!(score < (double)(getCurrentTimeInSeconds() - 2592000L))) {
            break;
         }

         masterInstance.zrem(key, new String[]{member.getElement()});
      }

      Redis.disconnect(masterInstance, log);
   }

   public static String getUserIdenticonIndex(String username) throws FusionException {
      int userId = getUserId(username);
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var5) {
         throw new FusionException(var5.getMessage());
      }

      if (slaveInstance == null) {
         return "0";
      } else {
         String key = Painter.KeySpace.USER.toString() + userId;
         String index = slaveInstance.hget(key, "IdenticonIndex");
         index = StringUtil.isBlank(index) ? "0" : index;
         Redis.disconnect(slaveInstance, log);
         return index;
      }
   }

   public static Vector<List<String>> getUserPaint(String username) throws FusionException {
      int userId = getUserId(username);
      List<String> userDetails = new ArrayList();
      Vector<List<String>> user = new Vector();
      if (!isClean(username)) {
         String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.PAINTS_RECEIVED.toString();
         Set<Tuple> paintsReceived = getSortedSet(userId, key, 0, 0);
         Iterator<Tuple> itr = paintsReceived.iterator();
         if (itr.hasNext()) {
            userDetails.add(((Tuple)itr.next()).getElement());
            userDetails.add(getUserIdenticonIndex(username));
         }
      }

      user.add(userDetails);
      return user;
   }

   public static String getPriceOfPaint() {
      return "USD " + priceOfPaintCredit;
   }

   public static String getPriceOfClean() {
      return "USD " + priceOfCleanCredit;
   }

   public static String getPriceOfIdenticon() {
      return "USD " + priceOfIdenticon;
   }

   public static void buyIdenticon(String username) throws FusionException {
      chargeUser(username, "USD", priceOfIdenticon, "Identicon purchase");
      int userId = getUserId(username);
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var4) {
         throw new FusionException(var4.getMessage());
      }

      String key = Painter.KeySpace.USER.toString() + userId;
      masterInstance.hincrBy(key, "IdenticonIndex", 1L);
      Redis.disconnect(masterInstance, log);
   }

   public static void buyPaintCredit(String username) throws FusionException {
      chargeUser(username, "USD", priceOfPaintCredit, "Paint Wars paint credit purchase");
   }

   public static void buyCleanCredit(String username) throws FusionException {
      chargeUser(username, "USD", priceOfCleanCredit, "Paint Wars clean credit purchase");
   }

   public static void buySpecialItem(String username, int typeId) throws FusionException {
      if (isInventoryFull(username)) {
         throw new FusionException("The inventory is full");
      } else {
         double price = 0.0D;
         String currency = "";
         boolean itemFound = false;
         Vector<ItemData> specialItems = getSpecialItems();
         Iterator itr = specialItems.iterator();

         try {
            while(itr.hasNext()) {
               ItemData item = (ItemData)itr.next();
               if (item.getId() == typeId) {
                  currency = item.getCurrency();
                  price = item.getPrice();
                  itemFound = true;
               }
            }
         } catch (NumberFormatException var9) {
            log.error("Unable to parse item price: " + var9.getMessage());
         } catch (IndexOutOfBoundsException var10) {
            log.error("Unable to parse item details: " + var10.getMessage());
         }

         if (!itemFound) {
            throw new FusionException("Item not found");
         } else {
            if (typeId == 1) {
               chargeUser(username, currency, price, "Paint Wars paint proof item purchase");
            } else if (typeId == 2) {
               chargeUser(username, currency, price, "Paint Wars dual paint item purchase");
            } else {
               if (typeId != 3) {
                  log.error("Unknown item type");
                  throw new FusionException("Unknown item type");
               }

               chargeUser(username, currency, price, "Paint Wars stealth paint item purchase");
            }

            addItemToInventory(username, typeId);
         }
      }
   }

   public static boolean isInventoryFull(String username) throws FusionException {
      int userId = getUserId(username);
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var6) {
         throw new FusionException(var6.getMessage());
      }

      String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.INVENTORY.toString();
      int inventorySize = 0;
      if (slaveInstance != null) {
         Long resultLong = slaveInstance.zcard(key);
         if (resultLong == null) {
            throw new FusionException("isInventoryFull-zcard returns null");
         }

         inventorySize = resultLong.intValue();
         Redis.disconnect(slaveInstance, log);
      }

      return inventorySize >= 30;
   }

   public static void addItemToInventory(String username, int itemId) throws FusionException {
      int userId = getUserId(username);
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var5) {
         throw new FusionException(var5.getMessage());
      }

      String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.INVENTORY.toString();
      masterInstance.zadd(key, (double)getCurrentTimeInSeconds(), Integer.toString(itemId) + ":" + getCurrentTimeInSeconds());
      Redis.disconnect(masterInstance, log);
   }

   public static Vector<ItemData> getSpecialItems() throws FusionException {
      try {
         Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         return contentBean.getPaintWarsSpecialItems();
      } catch (CreateException var1) {
         log.error(var1.getMessage());
         throw new FusionException(var1.getMessage());
      } catch (RemoteException var2) {
         log.error(var2.getMessage());
         throw new FusionException(var2.getMessage());
      }
   }

   public static String getSpecialItemName(int itemId) throws FusionException {
      Vector<ItemData> specialItems = getSpecialItems();
      Iterator itr = specialItems.iterator();

      ItemData item;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         item = (ItemData)itr.next();
      } while(item.getId() != itemId);

      return item.getName();
   }

   public static Vector<List<String>> getUserInventory(String username) throws FusionException {
      int userId = getUserId(username);
      String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.INVENTORY.toString();
      Set<Tuple> itemSet = getSortedSet(userId, key, 0, -1);
      return convertSetToVector(itemSet);
   }

   public static void useSpecialItem(String username, int itemId) throws FusionException {
      int userId = getUserId(username);
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getMasterInstanceForUserID(userId);
      } catch (Exception var10) {
         throw new FusionException(var10.getMessage());
      }

      String key = "";
      key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.INVENTORY.toString();
      Set<Tuple> itemSet = getSortedSet(userId, key, 0, -1);
      Iterator<Tuple> itr = itemSet.iterator();
      boolean itemFound = false;

      while(itr.hasNext()) {
         Tuple member = (Tuple)itr.next();
         String itemName = member.getElement();
         if (itemName.startsWith(Integer.toString(itemId))) {
            masterInstance.zrem(key, new String[]{member.getElement()});
            itemFound = true;
            break;
         }
      }

      if (!itemFound) {
         log.error("Item not found");
         throw new FusionException("Item not found");
      } else {
         key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.ITEM_IN_USE.toString() + Integer.toString(itemId);
         masterInstance.zadd(key, (double)getCurrentTimeInSeconds(), "TRUE");
         masterInstance.expire(key, 86400);
         Redis.disconnect(masterInstance, log);
      }
   }

   public static boolean isPaintProof(String username) throws FusionException {
      return checkIfSpecialItemUsed(username, 1);
   }

   public static boolean hasDualPaint(String username) throws FusionException {
      return checkIfSpecialItemUsed(username, 2);
   }

   public static boolean hasStealthPaint(String username) throws FusionException {
      return checkIfSpecialItemUsed(username, 3);
   }

   private static boolean checkIfSpecialItemUsed(String username, int itemType) throws FusionException {
      int userId = getUserId(username);
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var6) {
         throw new FusionException(var6.getMessage());
      }

      if (slaveInstance == null) {
         return false;
      } else {
         String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.ITEM_IN_USE.toString() + Integer.toString(itemType);
         boolean itemUsed = false;
         if (slaveInstance.exists(key)) {
            itemUsed = true;
         }

         Redis.disconnect(slaveInstance, log);
         return itemUsed;
      }
   }

   private static void chargeUser(String username, String currency, double price, String description) throws FusionException {
      try {
         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         if (accountEJB.userCanAffordCost(username, price, currency, (Connection)null)) {
            checkMerchantLimits(username, price);
            accountEJB.chargeUserForGameItem(username, "Paint Wars", description, price, currency, new AccountEntrySourceData(Painter.class));
         } else {
            throw new FusionException("Insufficient credits. Please recharge credits.");
         }
      } catch (FusionException var6) {
         throw var6;
      } catch (Exception var7) {
         throw new FusionException(var7.getMessage());
      }
   }

   public static boolean hasFreePaintCredits(String username) throws FusionException {
      int remainingFreeCredits = getRemainingFreePaints(username);
      return remainingFreeCredits > 0;
   }

   public static boolean hasFreeCleanCredits(String username) throws FusionException {
      int remainingFreeCredits = getRemainingFreeCleans(username);
      return remainingFreeCredits > 0;
   }

   public static int getRemainingFreePaints(String username) throws FusionException {
      int userId = getUserId(username);
      String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.PAINTS_SENT.toString();
      return getRemainingFreeCreditCount(userId, key, freePaintsPerDay);
   }

   public static int getRemainingFreeCleans(String username) throws FusionException {
      int userId = getUserId(username);
      String key = Painter.KeySpace.U.toString() + userId + ":" + Painter.KeySpace.CLEANS_SENT.toString();
      return getRemainingFreeCreditCount(userId, key, freeCleansPerDay);
   }

   private static int getRemainingFreeCreditCount(int userId, String key, int freeCreditsPerDay) throws FusionException {
      if (freeCreditsPerDay <= 0) {
         return 0;
      } else {
         long previousDayTime = getCurrentTimeInSeconds() - 86400L;
         Set<Tuple> sentSet = getSortedSet(userId, key, 0, freeCreditsPerDay - 1);
         int remaining = freeCreditsPerDay;
         Iterator itr = sentSet.iterator();

         while(itr.hasNext()) {
            double actionTimeInSeconds = ((Tuple)itr.next()).getScore();
            if (actionTimeInSeconds < (double)previousDayTime) {
               break;
            }

            --remaining;
            if (remaining <= 0) {
               break;
            }
         }

         return remaining;
      }
   }

   public static int getRequiredLevel() {
      int requiredLevel = 1;
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getGamesSlaveInstance();
      } catch (Exception var5) {
         log.warn("Unable to load game configuration");
      }

      if (slaveInstance != null) {
         String requiredLevelStr = slaveInstance.hget("PaintWars", "ReqdMigLevel");
         if (requiredLevelStr != null) {
            try {
               requiredLevel = Integer.parseInt(requiredLevelStr);
            } catch (NumberFormatException var4) {
               log.error("Unable to parse required mig level for paint wars");
            }
         }

         Redis.disconnect(slaveInstance, log);
      }

      return requiredLevel;
   }

   private static int getUserId(String username) throws FusionException {
      boolean var1 = true;

      try {
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         int userId = userBean.getUserID(username, (Connection)null);
         return userId;
      } catch (CreateException var3) {
         log.error("Unable to create userbean: " + var3.getMessage());
         throw new FusionException(var3.getMessage());
      } catch (RemoteException var4) {
         log.error("Unable to get user id: " + var4.getMessage());
         throw new FusionException(var4.getMessage());
      }
   }

   private static Set<Tuple> getSortedSet(int userId, String key, int min, int max) throws FusionException {
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var6) {
         throw new FusionException("Unable to get slave instance: " + var6.getMessage());
      }

      Set sortedSet;
      if (slaveInstance == null) {
         sortedSet = Collections.emptySet();
         return sortedSet;
      } else {
         sortedSet = slaveInstance.zrevrangeWithScores(key, (long)min, (long)max);
         Redis.disconnect(slaveInstance, log);
         return sortedSet;
      }
   }

   private static String getHashValue(int userId, String field) throws FusionException {
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getSlaveInstanceForUserID(userId);
      } catch (Exception var5) {
         throw new FusionException(var5.getMessage());
      }

      if (slaveInstance == null) {
         return "";
      } else {
         String key = Painter.KeySpace.USER.toString() + userId;
         String value = slaveInstance.hget(key, field);
         Redis.disconnect(slaveInstance, log);
         return value;
      }
   }

   private static boolean containsWithinPeriod(Set<Tuple> sortedSet, String targetUser, int period) {
      long previousDayTime = getCurrentTimeInSeconds() - (long)period;
      Iterator itr = sortedSet.iterator();

      while(itr.hasNext()) {
         Tuple member = (Tuple)itr.next();
         double scoreInSeconds = member.getScore();
         if (scoreInSeconds < (double)previousDayTime) {
            break;
         }

         if (member.getElement().startsWith(targetUser)) {
            return true;
         }
      }

      return false;
   }

   private static Vector<List<String>> convertUserSetToVector(Set<Tuple> set) throws FusionException {
      Vector<List<String>> vector = new Vector();
      Iterator itr = set.iterator();

      while(itr.hasNext()) {
         Tuple member = (Tuple)itr.next();
         String element = member.getElement();
         int index = element.lastIndexOf(58);
         if (index == -1) {
            log.warn("Error converting set to vector. Element: " + element);
         } else {
            String userInfo = element.substring(0, index);
            index = userInfo.indexOf(58);
            if (index == -1) {
               log.warn("Error converting set to vector. Element: " + element);
            } else {
               int userId = Integer.parseInt(userInfo.substring(0, index));
               String score = getHashValue(userId, "TotalPaintWarsPoints");
               String timestamp = convertDoubleToStr(member.getScore());
               List<String> userDetails = new ArrayList();
               userDetails.add(userInfo);
               userDetails.add(score);
               userDetails.add(timestamp);
               vector.add(userDetails);
            }
         }
      }

      return vector;
   }

   private static Vector<List<String>> convertSetToVector(Set<Tuple> set) {
      Vector<List<String>> vector = new Vector();
      Iterator itr = set.iterator();

      while(itr.hasNext()) {
         Tuple member = (Tuple)itr.next();
         List<String> list = new ArrayList();
         String element = member.getElement();
         String[] splitElement = element.split(":");

         for(int i = 0; i < splitElement.length; ++i) {
            list.add(splitElement[i]);
         }

         vector.add(list);
      }

      return vector;
   }

   private static int calculatePoints(String user1, String user2, boolean isPainting, boolean isClean) throws FusionException {
      if (isClean) {
         if (isPainting) {
            return areFriends(user1, user2) ? 10 : 15;
         } else {
            return areFriends(user1, user2) ? 25 : 30;
         }
      } else if (isPainting) {
         return areFriends(user1, user2) ? 5 : 10;
      } else if (user1.equals(user2)) {
         return 0;
      } else {
         return areFriends(user1, user2) ? 10 : 15;
      }
   }

   private static boolean areFriends(String user1, String user2) throws FusionException {
      try {
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         Set<String> broadcastList = userBean.loadBroadcastList(user1, (Connection)null);
         return broadcastList.contains(user2);
      } catch (CreateException var4) {
         log.error(var4.getMessage());
         throw new FusionException(var4.getMessage());
      } catch (RemoteException var5) {
         log.error(var5.getMessage());
         throw new FusionException(var5.getMessage());
      } catch (SQLException var6) {
         log.error(var6.getMessage());
         throw new FusionException(var6.getMessage());
      }
   }

   private static long getCurrentTimeInSeconds() {
      return Calendar.getInstance().getTimeInMillis() / 1000L;
   }

   private static String convertDoubleToStr(double value) {
      NumberFormat numberFormat = NumberFormat.getInstance();
      numberFormat.setGroupingUsed(false);
      return numberFormat.format(value);
   }

   private static void checkRaffleEligibility(String username, int userId, String key, double currentTime, int eligibilityAmount) throws FusionException {
      int eligibilityIndex = 0;
      if (eligibilityAmount > 0) {
         eligibilityIndex = eligibilityAmount - 1;
      }

      Set<Tuple> paintsSent = getSortedSet(userId, key, eligibilityIndex, eligibilityIndex);
      Iterator<Tuple> itr = paintsSent.iterator();
      if (itr.hasNext()) {
         Jedis masterInstance = null;

         try {
            masterInstance = Redis.getGamesMasterInstance();
         } catch (Exception var16) {
            log.warn("Unable to load game configuration");
         }

         if (masterInstance != null) {
            double lastRaffleTime = 0.0D;
            String lastRaffleTimeStr = masterInstance.hget("PaintWars", "LastRaffleTime");
            if (lastRaffleTimeStr != null) {
               try {
                  lastRaffleTime = Double.parseDouble(lastRaffleTimeStr);
               } catch (NumberFormatException var15) {
                  log.warn("Unable to parse last raffle time");
               }
            }

            double paintSentTimestamp = ((Tuple)itr.next()).getScore();
            if (paintSentTimestamp > lastRaffleTime) {
               masterInstance.zadd("PaintWars:RaffleCandidates", currentTime, username);
            }

            Redis.disconnect(masterInstance, log);
         }
      }

   }

   private static void checkMerchantLimits(String username, double cost) throws FusionException {
      if (cost != 0.0D) {
         try {
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userBean.loadUser(username, false, false);
            if (userData.type.value() != UserData.TypeEnum.MIG33_TOP_MERCHANT.value()) {
               return;
            }
         } catch (CreateException var9) {
            throw new FusionException(var9.getMessage());
         } catch (RemoteException var10) {
            throw new FusionException(var10.getMessage());
         }

         double amountLimitPerTimeSlot = SystemProperty.getDouble("MerchantLimitPerTimeSlot", 20.0D);
         long timeSlot = SystemProperty.getLong("MerchantLimitTimeSlot", 24L) * 60L * 60L * 1000L;
         LimitTracker limit = (LimitTracker)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username);
         String instanceId = "PaintWars";
         if (cost > amountLimitPerTimeSlot) {
            log.debug("Top merchant: " + username + " cannot make this purchase without going over the limit. Cost: " + cost);
            throw new FusionException("You cannot make this purchase without exceeding your limit for games.");
         } else {
            if (limit == null) {
               log.debug("Creating new game merchant limit for: " + username);
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, new LimitTracker(instanceId, Calendar.getInstance().getTimeInMillis() + timeSlot, cost));
            } else if (limit.hasExpired(Calendar.getInstance().getTimeInMillis())) {
               log.debug("Recreating new game merchant limit for: " + username);
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, new LimitTracker(instanceId, Calendar.getInstance().getTimeInMillis() + timeSlot, cost));
            } else {
               if (limit.getTotalAmountSpent() + cost > amountLimitPerTimeSlot) {
                  log.debug("Top merchant: " + username + " has exceeded the merchant limits. Merchant would have spent: " + (limit.getTotalAmountSpent() + cost));
                  throw new FusionException("You have exceeded your limit for games. Please try again later");
               }

               log.debug("Updating merchant limit balance for " + username + ". Balance is: " + (limit.getTotalAmountSpent() + cost));
               limit.add(instanceId, cost);
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_GAME_LIMIT, username, limit);
            }

         }
      }
   }

   static {
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getGamesSlaveInstance();
      } catch (Exception var3) {
         log.warn("Unable to load game configuration");
      }

      String key = "PaintWars";
      String value = null;
      if (slaveInstance != null) {
         value = slaveInstance.hget(key, "PriceOfPaintCredit");
         if (value != null) {
            priceOfPaintCredit = Double.parseDouble(value);
         }

         value = slaveInstance.hget(key, "PriceOfCleanCredit");
         if (value != null) {
            priceOfCleanCredit = Double.parseDouble(value);
         }

         value = slaveInstance.hget(key, "PriceOfIdenticon");
         if (value != null) {
            priceOfIdenticon = Double.parseDouble(value);
         }

         value = slaveInstance.hget(key, "FreePaintsPerDay");
         if (value != null) {
            freePaintsPerDay = Integer.parseInt(value);
         }

         value = slaveInstance.hget(key, "FreeCleansPerDay");
         if (value != null) {
            freeCleansPerDay = Integer.parseInt(value);
         }

         value = slaveInstance.hget(key, "PaintsSentEligibility");
         if (value != null) {
            paintsSentEligibility = Integer.parseInt(value);
         }

         value = slaveInstance.hget(key, "CleansSentEligibility");
         if (value != null) {
            cleansSentEligibility = Integer.parseInt(value);
         }

         Redis.disconnect(slaveInstance, log);
      }

   }

   private static enum KeySpace {
      USER("User:"),
      U("U:"),
      PAINTS_SENT("PaintsSent"),
      PAINTS_RECEIVED("PaintsReceived"),
      CLEANS_SENT("CleansSent"),
      CLEANS_RECEIVED("CleansReceived"),
      INVENTORY("PaintWars:Inventory"),
      ITEM_IN_USE("PaintWars:ItemInUse:"),
      RAFFLE_CANDIDATES("PaintWars:RaffleCandidates");

      private String value;

      private KeySpace(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }
}
