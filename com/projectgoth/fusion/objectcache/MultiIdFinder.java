package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class MultiIdFinder {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MultiIdFinder.class));

   public static Set<String> getMultiIds(HashMap<String, List<String>> ipToUsernames, String chatroomName) {
      User userEJB = null;

      try {
         userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      } catch (CreateException var8) {
         log.error("Unable to create User EJB: " + var8);
         return null;
      }

      Set<String> banList = new HashSet();
      Iterator iter = ipToUsernames.entrySet().iterator();

      while(iter.hasNext()) {
         Entry<String, List<String>> entry = (Entry)iter.next();
         String ip = (String)entry.getKey();
         List<String> usernames = (List)entry.getValue();
         if (usernames != null && usernames.size() >= 2) {
            addUsersToBanListThatMeetCriteria(banList, MultiIdFinder.MultiIdCriteriaTypeEnum.MIGLEVEL, usernames, ip, chatroomName, userEJB);
            addUsersToBanListThatMeetCriteria(banList, MultiIdFinder.MultiIdCriteriaTypeEnum.NUM_FRIENDS, usernames, ip, chatroomName, userEJB);
            addUsersToBanListThatMeetCriteria(banList, MultiIdFinder.MultiIdCriteriaTypeEnum.NUM_GIFTS, usernames, ip, chatroomName, userEJB);
         }
      }

      return banList;
   }

   private static void addUsersToBanListThatMeetCriteria(Set<String> banList, MultiIdFinder.MultiIdCriteriaTypeEnum type, List<String> usernames, String ip, String chatroomName, User userEJB) {
      HashMap<Integer, List<String>> criteriaValueToUsernames = new HashMap();
      Iterator i$ = usernames.iterator();

      while(true) {
         String username;
         do {
            if (!i$.hasNext()) {
               Set<String> criteriaBanList = getUsersWithIdenticalCriteria(criteriaValueToUsernames);
               Iterator i$ = criteriaBanList.iterator();

               while(i$.hasNext()) {
                  String username = (String)i$.next();
                  log.info("Adding User [" + username + "] with IP [" + ip + "] to Chatroom [" + chatroomName + "] ban list for containing the same IP and [" + type + "] as another chatroom user");
               }

               banList.addAll(criteriaBanList);
               return;
            }

            username = (String)i$.next();
         } while(banList.contains(username));

         int criteriaValue = -1;
         switch(type) {
         case MIGLEVEL:
            try {
               criteriaValue = MemCacheOrEJB.getUserReputationLevel(username);
            } catch (Exception var11) {
               log.error("Unable to check mig level: " + var11);
            }
            break;
         case NUM_FRIENDS:
            try {
               Set<String> bcl = userEJB.loadBroadcastList(username, (Connection)null);
               criteriaValue = bcl.size();
               if (criteriaValue <= 0) {
                  continue;
               }
            } catch (Exception var13) {
               log.error("Unable to get broadcast list: " + var13);
            }
            break;
         case NUM_GIFTS:
            try {
               int userId = userEJB.getUserID(username, (Connection)null);
               if (userId > 0) {
                  criteriaValue = userEJB.getGiftsReceivedCount(userId);
                  if (criteriaValue <= 0) {
                     continue;
                  }
               }
            } catch (Exception var12) {
               log.error("Unable to get gifts received count: " + var12);
            }
            break;
         default:
            log.error("Unknown MultiIdCriteriaTypeEnum [" + type + "]");
            return;
         }

         if (criteriaValue != -1) {
            if (!criteriaValueToUsernames.containsKey(criteriaValue)) {
               List<String> list = new ArrayList();
               list.add(username);
               criteriaValueToUsernames.put(criteriaValue, list);
            } else {
               ((List)criteriaValueToUsernames.get(criteriaValue)).add(username);
            }
         }
      }
   }

   private static Set<String> getUsersWithIdenticalCriteria(HashMap<Integer, List<String>> map) {
      Set<String> usernames = new HashSet();
      Iterator iter = map.entrySet().iterator();

      while(true) {
         List list;
         do {
            if (!iter.hasNext()) {
               return usernames;
            }

            list = (List)((Entry)iter.next()).getValue();
         } while(list.size() <= 1);

         Iterator i$ = list.iterator();

         while(i$.hasNext()) {
            String item = (String)i$.next();
            usernames.add(item);
         }
      }
   }

   public static enum MultiIdCriteriaTypeEnum {
      MIGLEVEL(1),
      NUM_FRIENDS(2),
      NUM_GIFTS(3);

      private int value;

      private MultiIdCriteriaTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MultiIdFinder.MultiIdCriteriaTypeEnum fromValue(int value) {
         MultiIdFinder.MultiIdCriteriaTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MultiIdFinder.MultiIdCriteriaTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
