package com.projectgoth.fusion.credittransfer;

import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CreditTransferUtils {
   private final LazyLoader<Set<Integer>> exemptedUserIdsLoader;
   private final LazyLoader<Set<UserData.TypeEnum>> userTypesToCheckLoader;

   private CreditTransferUtils() {
      this.exemptedUserIdsLoader = new LazyLoader<Set<Integer>>("EXEMPTED_USER_IDS", 60000L) {
         protected Set<Integer> fetchValue() throws Exception {
            int[] userIds = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.USERIDS_EXEMPTED_FROM_MIG_LEVEL_CHECK);
            if (userIds != null && userIds.length > 0) {
               HashSet<Integer> userIdSet = new HashSet();
               int[] arr$ = userIds;
               int len$ = userIds.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int userId = arr$[i$];
                  userIdSet.add(userId);
               }

               return Collections.unmodifiableSet(userIdSet);
            } else {
               return Collections.EMPTY_SET;
            }
         }
      };
      this.userTypesToCheckLoader = new LazyLoader<Set<UserData.TypeEnum>>("userTypesToCheck", 60000L) {
         protected Set<UserData.TypeEnum> fetchValue() throws Exception {
            int[] userTypeCodes = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.USER_TYPES_TO_APPLY_MIG_LEVEL_CHECK);
            if (userTypeCodes != null && userTypeCodes.length > 0) {
               HashSet<UserData.TypeEnum> userTypeSet = new HashSet();
               int[] arr$ = userTypeCodes;
               int len$ = userTypeCodes.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int userTypeCode = arr$[i$];
                  UserData.TypeEnum userType = UserData.TypeEnum.fromValue(userTypeCode);
                  if (userType != null) {
                     userTypeSet.add(userType);
                  }
               }

               return Collections.unmodifiableSet(userTypeSet);
            } else {
               return Collections.EMPTY_SET;
            }
         }
      };
   }

   public static CreditTransferUtils getInstance() {
      return CreditTransferUtils.CreditTransferUtilsSingletonHolder.INSTANCE;
   }

   public boolean migLevelCheckEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.ENABLE_MIG_LEVEL_CHECK);
   }

   public boolean isUserIdExemptedFromMigLevelCheck(int userId) {
      return ((Set)this.exemptedUserIdsLoader.getValue()).contains(userId);
   }

   public boolean userTypeRequiresMigLevelCheck(UserData.TypeEnum userType) {
      return userType == null ? true : ((Set)this.userTypesToCheckLoader.getValue()).contains(userType);
   }

   public boolean applyMigLevelCheckForVerifiedAccountStatusValue(UserData.AccountVerifiedEnum accountVerified) {
      return accountVerified == UserData.AccountVerifiedEnum.VERIFIED_OK ? SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.ENABLE_MIG_LEVEL_CHECK_FOR_ACCOUNT_VERIFIED_USER) : true;
   }

   public void resetLoader() {
      this.exemptedUserIdsLoader.invalidateCache();
      this.userTypesToCheckLoader.invalidateCache();
   }

   // $FF: synthetic method
   CreditTransferUtils(Object x0) {
      this();
   }

   private static class CreditTransferUtilsSingletonHolder {
      public static final CreditTransferUtils INSTANCE = new CreditTransferUtils();
   }
}
