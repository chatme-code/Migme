package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.CampaignParticipation;
import com.projectgoth.leto.common.event.campaigns.SubjectParticipatedCampaigns;
import java.util.HashMap;
import java.util.Map;

public abstract class RewardProgramTriggerWithParticipatedCampaigns extends RewardProgramTrigger implements SubjectParticipatedCampaigns {
   private Map<Integer, CampaignParticipation> participatedCampaigns = new HashMap();

   protected RewardProgramTriggerWithParticipatedCampaigns(RewardProgramData.TypeEnum programType, UserData userData) {
      super(programType, userData);
   }

   public Map<Integer, CampaignParticipation> getParticipatedCampaigns() {
      return this.participatedCampaigns;
   }

   public void setParticipatedCampaigns(Map<Integer, CampaignParticipation> participatedCampaigns) {
      this.participatedCampaigns = participatedCampaigns;
   }
}
