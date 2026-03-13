package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.post.MigboPostActionEvent;
import com.projectgoth.leto.common.event.post.PostActionType;
import com.projectgoth.leto.common.utils.enums.IEnumValueGetter;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.util.EnumSet;
import java.util.Set;

public class MigboPostEventTrigger extends RewardProgramTrigger implements MigboPostActionEvent {
   public String postID;
   public MigboPostEventTrigger.PostEventTypeEnum eventType;
   public EnumSet<Enums.ThirdPartyEnum> shareToThirdParty;

   public MigboPostEventTrigger(UserData postAuthorUserData, String postID, MigboPostEventTrigger.PostEventTypeEnum eventType) {
      super(RewardProgramData.TypeEnum.MIGBO_POST_EVENT, postAuthorUserData);
      this.postID = postID;
      this.eventType = eventType;
   }

   public String getPostID() {
      return this.postID;
   }

   public PostActionType getActionType() {
      return this.eventType != null ? this.eventType.postActionType : null;
   }

   public Set<Enums.ThirdPartyEnum> getSharedToSites() {
      return this.shareToThirdParty;
   }

   public static enum PostEventTypeEnum implements IEnumValueGetter<Integer> {
      REPLIED_TO(PostActionType.REPLIED_TO),
      RESHARED(PostActionType.RESHARED),
      SUBSCRIBED(PostActionType.SUBSCRIBED),
      EMOTIONAL_FOOTPRINTED(PostActionType.EMOTIONAL_FOOTPRINTED);

      private final PostActionType postActionType;

      private PostEventTypeEnum(PostActionType postActionType) {
         this.postActionType = postActionType;
      }

      public int getType() {
         return this.postActionType.getEnumValue();
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static MigboPostEventTrigger.PostEventTypeEnum fromType(int type) {
         return (MigboPostEventTrigger.PostEventTypeEnum)MigboPostEventTrigger.PostEventTypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(type);
      }

      public Integer getEnumValue() {
         return this.postActionType.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, MigboPostEventTrigger.PostEventTypeEnum> INSTANCE = new ValueToEnumMap(MigboPostEventTrigger.PostEventTypeEnum.class);
      }
   }
}
