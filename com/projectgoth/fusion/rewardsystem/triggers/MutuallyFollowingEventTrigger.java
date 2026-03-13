package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.friending.FriendingRelationshipType;
import com.projectgoth.leto.common.event.friending.MutuallyFollowingEvent;
import com.projectgoth.leto.common.user.UserDetails;

public class MutuallyFollowingEventTrigger extends RelationshipEventTrigger implements MutuallyFollowingEvent {
   private boolean thisUserFollowedBack;

   public MutuallyFollowingEventTrigger(UserData thisUser, UserData otherUser, boolean thisUserFollowedBack) {
      super(RewardProgramData.TypeEnum.MUTUALLY_FOLLOWING_EVENT, thisUser, otherUser, RelationshipEventTrigger.RelationshipEventTypeEnum.MUTUALLY_FOLLOWING);
      this.quantityDelta = 1;
      this.amountDelta = 0.0D;
      this.currency = "USD";
      this.thisUserFollowedBack = thisUserFollowedBack;
   }

   public boolean isThisUserFollowedBacked() {
      return this.thisUserFollowedBack;
   }

   public UserDetails getOtherUser() {
      return this.getOtherUserData();
   }

   public FriendingRelationshipType getFriendingRelationship() {
      return this.getRelationshipEvent().toFriendingRelationshipType();
   }

   public boolean isSubjectUserAsFollowBacker() {
      return this.isThisUserFollowedBacked();
   }

   public UserDetails getFriendUser() {
      return this.getOtherUser();
   }
}
