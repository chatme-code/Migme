package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.friending.FollowingEvent;
import com.projectgoth.leto.common.event.friending.FriendingRelationshipType;
import com.projectgoth.leto.common.user.UserDetails;

public class MigboFollowingEventTrigger extends RelationshipEventTrigger implements FollowingEvent {
   private boolean autoFollow;

   public MigboFollowingEventTrigger(UserData thisUserData, UserData otherUserData, RelationshipEventTrigger.RelationshipEventTypeEnum eventType, boolean autoFollow) {
      super(RewardProgramData.TypeEnum.MIGBO_FOLLOWING_EVENT, thisUserData, otherUserData, eventType);
      this.autoFollow = autoFollow;
   }

   public MigboFollowingEventTrigger(UserData thisUserData, UserData otherUserData, RelationshipEventTrigger.RelationshipEventTypeEnum eventType) {
      this(thisUserData, otherUserData, eventType, false);
   }

   public UserData getFollowedUser() {
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
