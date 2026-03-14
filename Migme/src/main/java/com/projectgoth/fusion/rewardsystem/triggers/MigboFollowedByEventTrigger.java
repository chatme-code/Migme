package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.friending.FollowedByEvent;
import com.projectgoth.leto.common.event.friending.FriendingRelationshipType;
import com.projectgoth.leto.common.user.UserDetails;

public class MigboFollowedByEventTrigger extends RelationshipEventTrigger implements FollowedByEvent {
   private boolean autoFollow;

   public MigboFollowedByEventTrigger(UserData thisUserData, UserData otherUserData, RelationshipEventTrigger.RelationshipEventTypeEnum relationshipEventType, boolean autoFollow) {
      super(RewardProgramData.TypeEnum.MIGBO_FOLLOWED_BY_EVENT, thisUserData, otherUserData, relationshipEventType);
      this.autoFollow = autoFollow;
   }

   public MigboFollowedByEventTrigger(UserData thisUserData, UserData otherUserData, RelationshipEventTrigger.RelationshipEventTypeEnum relationshipEventType) {
      this(thisUserData, otherUserData, relationshipEventType, false);
   }

   public UserData getFollowerUser() {
      return this.getOtherUserData();
   }

   public boolean isAutoFollow() {
      return this.autoFollow;
   }

   public UserDetails getOtherUser() {
      return this.getOtherUserData();
   }

   public FriendingRelationshipType getFriendingRelationship() {
      return this.getRelationshipEvent().toFriendingRelationshipType();
   }
}
