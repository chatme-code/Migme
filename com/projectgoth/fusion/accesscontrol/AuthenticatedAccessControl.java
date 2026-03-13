package com.projectgoth.fusion.accesscontrol;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class AuthenticatedAccessControl {
   private static final int LOCAL_CACHE_TIME = 600000;
   private static final int LOCAL_CACHE_RANDOMNESS_TIME = 13;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AuthenticatedAccessControl.class));
   private static Map<String, AuthenticatedAccessControlData> dataMap = new ConcurrentHashMap();
   private static Semaphore semaphore = new Semaphore(1);
   private static long lastUpdated;
   private static Random random = new Random();

   public static void loadData() {
      if (lastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         List<AuthenticatedAccessControlData> newData = misEJB.getAuthenticatedAccessControlData();
         Set<String> newKeySet = new HashSet();
         Iterator i$ = newData.iterator();

         while(i$.hasNext()) {
            AuthenticatedAccessControlData acData = (AuthenticatedAccessControlData)i$.next();

            try {
               String key = acData.name.toLowerCase();
               if (dataMap.containsKey(key)) {
                  log.info(String.format("updating authenticated access control data %s", key));
               } else {
                  log.info(String.format("adding authenticated access control data %s", key));
               }

               newKeySet.add(key);
               dataMap.put(key, acData);
            } catch (Exception var11) {
               log.error("Unable to load authenticated access control data [" + acData.name + "]", var11);
            }
         }

         i$ = dataMap.keySet().iterator();

         while(i$.hasNext()) {
            String key = (String)i$.next();
            if (!newKeySet.contains(key)) {
               log.info(String.format("removing authenticated access control data %s", key));
               dataMap.remove(key);
               log.info(String.format("removed authenticated access control data %s", key));
            }
         }

         lastUpdated = System.currentTimeMillis();
      } catch (Exception var12) {
         log.error("Unable to load authenticated access control data", var12);
      } finally {
         semaphore.release();
      }

   }

   private static AuthenticatedAccessControlData getData(AuthenticatedAccessControlTypeEnum type) {
      long curTime = System.currentTimeMillis();
      if (curTime - lastUpdated > 600000L && curTime - lastUpdated - 600000L > (long)(random.nextInt(13) * 1000)) {
         loadData();
      }

      AuthenticatedAccessControlData acd = (AuthenticatedAccessControlData)dataMap.get(type.name().toLowerCase());
      if (acd == null) {
         acd = type.toData();
      }

      return acd;
   }

   private static boolean checkAccess(AuthenticatedAccessControlParameter param, AuthenticatedAccessControlData acd, AuthenticatedAccessControlTypeEnum type) {
      boolean allowed = false;
      String rateLimit = null;
      log.debug(String.format("Checking auth access for %s by %s with %s", type, param, acd));
      if (acd.isMobileVerifiedAllowed && param.isMobileVerified) {
         allowed = true;
         rateLimit = acd.mobileVerifiedRateLimit;
      } else if (acd.isEmailVerifiedAllowed && param.isEmailVerified) {
         allowed = true;
         rateLimit = acd.emailVerifiedRateLimit;
      }

      if (allowed && !StringUtil.isBlank(rateLimit)) {
         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.AUTH_AC.toString(), MemCachedKeyUtils.getFullKeyFromStrings(type.name(), param.username), rateLimit);
         } catch (MemCachedRateLimiter.LimitExceeded var6) {
            allowed = false;
            log.debug(var6.getPrettyMessage());
         } catch (MemCachedRateLimiter.FormatError var7) {
            allowed = false;
            log.error("Formatting error in rate limiter expression", var7);
         }
      }

      log.debug(String.format(" Result %s", allowed));
      return allowed;
   }

   public static boolean hasAccess(AuthenticatedAccessControlTypeEnum type, AuthenticatedAccessControlParameter param) {
      if (type != null && param != null) {
         AuthenticatedAccessControlData acd = getData(type);
         return acd == null ? false : checkAccess(param, acd, type);
      } else {
         return false;
      }
   }

   public static boolean hasAccess(AuthenticatedAccessControlTypeEnum type, UserData userData) throws IllegalArgumentException {
      if (userData == null) {
         log.error(String.format("Unable to check authenticated access for user for type %s due to null userdata, returning false", type));
         throw new IllegalArgumentException("Invalid user");
      } else {
         return hasAccess(type, new AuthenticatedAccessControlParameter(userData));
      }
   }

   public static boolean hasAccessByUsernameLocal(AuthenticatedAccessControlTypeEnum type, String username) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         return hasAccessByUsername(type, username, userBean);
      } catch (CreateException var4) {
         return false;
      }
   }

   public static boolean hasAccessByUsername(AuthenticatedAccessControlTypeEnum type, String username, Object ejbProxy) throws IllegalArgumentException, IllegalStateException {
      if (type != null && !StringUtil.isBlank(username)) {
         AuthenticatedAccessControlData acd = getData(type);
         if (acd == null) {
            return false;
         } else if (!acd.isMobileVerifiedAllowed && !acd.isEmailVerifiedAllowed) {
            return false;
         } else {
            try {
               AuthenticatedAccessControlParameter acp;
               if (ejbProxy instanceof User) {
                  acp = ((User)ejbProxy).getUserAuthenticatedAccessControlParameter(username);
               } else {
                  if (!(ejbProxy instanceof UserLocal)) {
                     throw new IllegalArgumentException("user bean proxy is neither remote nor local");
                  }

                  acp = ((UserLocal)ejbProxy).getUserAuthenticatedAccessControlParameter(username);
               }

               if (null == acp) {
                  throw new IllegalArgumentException("Unknown user " + username);
               } else {
                  return checkAccess(acp, acd, type);
               }
            } catch (RemoteException var5) {
               log.error(String.format("Unable to check authenticated access control for user '%s' for %s", username, type), var5);
               throw new IllegalStateException("Internal Error");
            }
         }
      } else {
         return false;
      }
   }

   public static boolean hasAccessByUseridLocal(AuthenticatedAccessControlTypeEnum type, int userID) {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         return hasAccessByUserid(type, userID, userBean);
      } catch (CreateException var4) {
         return false;
      }
   }

   public static boolean hasAccessByUserid(AuthenticatedAccessControlTypeEnum type, int userID, Object ejbProxy) throws IllegalArgumentException, IllegalStateException {
      if (type != null && userID >= 0) {
         AuthenticatedAccessControlData acd = getData(type);
         if (acd == null) {
            return false;
         } else if (!acd.isMobileVerifiedAllowed && !acd.isEmailVerifiedAllowed) {
            return false;
         } else {
            try {
               UserData userData;
               if (ejbProxy instanceof User) {
                  userData = ((User)ejbProxy).loadUserFromID(userID);
               } else {
                  if (!(ejbProxy instanceof UserLocal)) {
                     throw new IllegalArgumentException("user bean proxy is neither remote nor local");
                  }

                  userData = ((UserLocal)ejbProxy).loadUserFromID(userID);
               }

               if (userData == null) {
                  throw new IllegalArgumentException("Unknown user " + userID);
               } else {
                  return checkAccess(new AuthenticatedAccessControlParameter(userData), acd, type);
               }
            } catch (RemoteException var6) {
               log.error(String.format("Unable to check authenticated access control for user id %d for %s", userID, type), var6);
               throw new IllegalStateException("Internal Error");
            }
         }
      } else {
         return false;
      }
   }

   static {
      loadData();
   }
}
