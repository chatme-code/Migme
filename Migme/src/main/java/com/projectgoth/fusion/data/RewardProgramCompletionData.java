package com.projectgoth.fusion.data;

import com.projectgoth.fusion.ejb.RewardedUnlockedStoreItemData;
import com.projectgoth.fusion.rewardsystem.outcomes.NotificationTemplateOutcomeData;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class RewardProgramCompletionData implements Serializable {
   private final int oldMigLevel;
   private final int newMigLevel;
   private final int rewardCount;
   private Timestamp rewardedTime;
   private final UserData userData;
   private final RewardProgramData rewardProgramData;
   private final long completionid;
   private final Collection<RewardedReputationData> rewardedReputations;
   private final AccountEntrySourceData accountEntrySourceData;
   private final Collection<RewardedMigCreditData> rewardedMigCredits;
   private final Collection<RewardedStoreItemData> rewardedStoreItems;
   private final Collection<RewardedBadgeData> rewardedBadges;
   private final Collection<RewardedGroupMembershipData> rewardedGroupMemberships;
   private final Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems;
   private final Collection<NotificationTemplateOutcomeData> notificationTemplates;

   public RewardProgramCompletionData(UserData userData, RewardProgramData rewardProgramData, long completionid, Timestamp rewardedTime, Collection<RewardedReputationData> rewardedReputations, Collection<RewardedMigCreditData> rewardedMigCredits, Collection<RewardedStoreItemData> rewardedStoreItems, Collection<RewardedBadgeData> rewardedBadges, Collection<RewardedGroupMembershipData> rewardedGroupMemberships, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems, Collection<NotificationTemplateOutcomeData> notificationTemplateKeys, AccountEntrySourceData accountEntrySourceData) {
      this.userData = userData;
      this.rewardProgramData = rewardProgramData;
      this.completionid = completionid;
      this.rewardedTime = rewardedTime;
      this.accountEntrySourceData = accountEntrySourceData;
      this.rewardedReputations = (Collection)(rewardedReputations != null ? Collections.unmodifiableCollection(rewardedReputations) : Collections.EMPTY_LIST);
      this.rewardedMigCredits = (Collection)(rewardedMigCredits != null ? Collections.unmodifiableCollection(rewardedMigCredits) : Collections.EMPTY_LIST);
      this.rewardedStoreItems = (Collection)(rewardedStoreItems != null ? Collections.unmodifiableCollection(rewardedStoreItems) : Collections.EMPTY_LIST);
      this.rewardedBadges = (Collection)(rewardedBadges != null ? Collections.unmodifiableCollection(rewardedBadges) : Collections.EMPTY_LIST);
      this.rewardedGroupMemberships = (Collection)(rewardedGroupMemberships != null ? Collections.unmodifiableCollection(rewardedGroupMemberships) : Collections.EMPTY_LIST);
      this.rewardedUnlockedStoreItems = (Collection)(rewardedUnlockedStoreItems != null ? Collections.unmodifiableCollection(rewardedUnlockedStoreItems) : Collections.EMPTY_LIST);
      this.notificationTemplates = (Collection)(notificationTemplateKeys != null ? Collections.unmodifiableCollection(notificationTemplateKeys) : Collections.EMPTY_LIST);
      int minOldLevel = -1;
      int maxNewLevel = -1;
      if (rewardedReputations != null) {
         Iterator i$ = rewardedReputations.iterator();

         label68:
         while(true) {
            RewardedReputationData rewardedReputation;
            do {
               do {
                  if (!i$.hasNext()) {
                     break label68;
                  }

                  rewardedReputation = (RewardedReputationData)i$.next();
               } while(rewardedReputation.getOldLevel() == rewardedReputation.getNewLevel());

               if (minOldLevel == -1 || rewardedReputation.getOldLevel() < minOldLevel) {
                  minOldLevel = rewardedReputation.getOldLevel();
               }
            } while(maxNewLevel != -1 && rewardedReputation.getNewLevel() <= maxNewLevel);

            maxNewLevel = rewardedReputation.getNewLevel();
         }
      }

      this.oldMigLevel = minOldLevel;
      this.newMigLevel = maxNewLevel;
      this.rewardCount = sumSizes(rewardedReputations, rewardedMigCredits, rewardedStoreItems, rewardedBadges, rewardedGroupMemberships, rewardedUnlockedStoreItems);
   }

   private static int sumSizes(Collection<?>... colls) {
      int sum = 0;
      Collection[] arr$ = colls;
      int len$ = colls.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Collection<?> c = arr$[i$];
         if (c != null) {
            sum += c.size();
         }
      }

      return sum;
   }

   public int getNewMigLevel() {
      return this.newMigLevel;
   }

   public int getOldMigLevel() {
      return this.oldMigLevel;
   }

   public UserData getUserData() {
      return this.userData;
   }

   public RewardProgramData getRewardProgramData() {
      return this.rewardProgramData;
   }

   public Collection<RewardedReputationData> getRewardedReputations() {
      return this.rewardedReputations;
   }

   public long getCompletionid() {
      return this.completionid;
   }

   public AccountEntrySourceData getAccountEntrySourceData() {
      return this.accountEntrySourceData;
   }

   public Collection<RewardedMigCreditData> getRewardedMigCredits() {
      return this.rewardedMigCredits;
   }

   public Collection<RewardedStoreItemData> getRewardedStoreItems() {
      return this.rewardedStoreItems;
   }

   public Collection<RewardedBadgeData> getRewardedBadges() {
      return this.rewardedBadges;
   }

   public Collection<RewardedGroupMembershipData> getRewardedGroupMemberships() {
      return this.rewardedGroupMemberships;
   }

   public Collection<RewardedUnlockedStoreItemData> getRewardedUnlockedStoreItems() {
      return this.rewardedUnlockedStoreItems;
   }

   public Collection<NotificationTemplateOutcomeData> getNotificationTemplates() {
      return this.notificationTemplates;
   }

   public int getRewardCount() {
      return this.rewardCount;
   }

   public Timestamp getRewardedTime() {
      return this.rewardedTime;
   }

   public String toString() {
      return "RewardProgramID:" + this.rewardProgramData.id + ";UserID:" + this.userData.userID + ";RewardCount:" + this.rewardCount;
   }
}
