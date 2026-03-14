package com.projectgoth.fusion.merchant;

import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.TemplateStringProcessor;
import com.projectgoth.fusion.data.UserData;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoucherIssuanceUtility {
   private final LazyLoader<Set<UserData.TypeEnum>> userTypesAllowedToIssueLoader;

   private VoucherIssuanceUtility() {
      this.userTypesAllowedToIssueLoader = new LazyLoader<Set<UserData.TypeEnum>>("userTypesAllowedToIssue", 60000L) {
         protected Set<UserData.TypeEnum> fetchValue() throws Exception {
            int[] userTypeCodes = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.USER_TYPES_ALLOWED_TO_ISSUE);
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
               return Collections.emptySet();
            }
         }
      };
   }

   public static VoucherIssuanceUtility getInstance() {
      return VoucherIssuanceUtility.Singletons.INSTANCE;
   }

   public boolean migLevelCheckEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.ENABLE_MIG_LEVEL_CHECK);
   }

   public int getMinMigLevel() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.MIN_MIG_LEVEL);
   }

   public boolean userTypeCheckEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.ENABLE_USER_TYPE_CHECK);
   }

   public String getDisallowedUserTypeErrorMessage(UserData.TypeEnum userType) throws IOException {
      String template = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.DISALLOWED_USER_TYPE_ERROR_MESSAGE);
      Map<String, String> templateParams = new HashMap();
      templateParams.put("user.type.label", userType.getLabel());
      return TemplateStringProcessor.process(template, templateParams);
   }

   public String getInsufficientMigLevelErrorMessage() throws IOException {
      String template = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.INSUFFICIENT_MIG_LEVEL_ERROR_MESSAGE);
      Map<String, String> templateParams = new HashMap();
      templateParams.put("min.miglevel", String.valueOf(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.MIN_MIG_LEVEL)));
      return TemplateStringProcessor.process(template, templateParams);
   }

   public void resetLoader() {
      this.userTypesAllowedToIssueLoader.invalidateCache();
   }

   public boolean userTypeAllowedToIssueVoucher(UserData.TypeEnum userType) {
      if (userType == null) {
         return false;
      } else {
         Set<?> values = (Set)this.userTypesAllowedToIssueLoader.getValue();
         return values.contains(userType);
      }
   }

   // $FF: synthetic method
   VoucherIssuanceUtility(Object x0) {
      this();
   }

   private static final class Singletons {
      private static final VoucherIssuanceUtility INSTANCE = new VoucherIssuanceUtility();
   }
}
