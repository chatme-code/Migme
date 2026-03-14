package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MigboEnums;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.post.MigboPostCreatedEvent;
import com.projectgoth.leto.common.event.post.PostContentType;
import com.projectgoth.leto.common.event.post.PostOriginality;
import com.projectgoth.leto.common.event.post.PostingApplicationType;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigboPostCreatedTrigger extends RewardProgramTrigger implements MigboPostCreatedEvent {
   public MigboEnums.MigboPostOriginalityEnum postOriginality;
   public MigboEnums.MigboPostTypeEnum postType;
   public MigboEnums.PostApplicationEnum application;
   public List<String> hashtags;
   public List<Map<String, String>> links;
   public String parentPostID;
   public EnumSet<Enums.ThirdPartyEnum> shareToThirdParty;

   public MigboPostCreatedTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.MIGBO_POST_CREATED, userData);
   }

   public PostOriginality getOriginality() {
      return this.postOriginality != null ? this.postOriginality.toPostOriginality() : null;
   }

   public PostContentType getContentType() {
      return this.postType != null ? this.postType.toPostContentType() : null;
   }

   public PostingApplicationType getFromApplicationType() {
      return this.application != null ? this.application.toPostingApplicationType() : null;
   }

   public List<String> getHashtags() {
      return this.hashtags;
   }

   public List<Map<String, String>> getLinks() {
      return this.links;
   }

   public String getParentPostID() {
      return this.parentPostID;
   }

   public Set<Enums.ThirdPartyEnum> getSharedToSites() {
      return this.shareToThirdParty;
   }
}
