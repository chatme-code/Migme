package com.projectgoth.fusion.authentication.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.authentication.domain.PersistedCredential;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;

public class CredentialList {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CredentialList.class));
   public static final String CREDENTIAL_LIST_NAMESPACE = "CRDL";
   public static final String CREDENTIAL_LIST_DISTRIBUTED_LOCK_NAMESPACE = "CDRLDL";

   public static List<PersistedCredential> newCredentialList() {
      return new ArrayList();
   }

   public static String getKey(int userID) {
      return MemCachedUtils.getCacheKeyInNamespace("CRDL", Integer.toString(userID));
   }

   public static List<PersistedCredential> getCredentialList(MemCachedClient instance, int userID) {
      return (List)instance.get(getKey(userID));
   }

   public static boolean setCredentialList(MemCachedClient instance, int userID, List<PersistedCredential> credentialList) {
      Calendar now = Calendar.getInstance();
      now.add(6, 5);
      return instance.set(getKey(userID), credentialList, now.getTime());
   }

   public static boolean deleteCredentialList(MemCachedClient instance, int userID) {
      return instance.delete(getKey(userID));
   }
}
