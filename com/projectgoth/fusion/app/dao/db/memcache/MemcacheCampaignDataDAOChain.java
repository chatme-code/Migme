package com.projectgoth.fusion.app.dao.db.memcache;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.CampaignDataDAOChain;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import org.apache.log4j.Logger;

public class MemcacheCampaignDataDAOChain extends CampaignDataDAOChain {
   private static final Logger log = Logger.getLogger(MemcacheCampaignDataDAOChain.class);

   public CampaignData getCampaignData(int campaignid) throws DAOException {
      CampaignData cd = (CampaignData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN, String.valueOf(campaignid));
      if (cd == null) {
         cd = super.getCampaignData(campaignid);
         if (cd != null) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN, String.valueOf(campaignid), cd);
         }
      }

      return cd;
   }

   public CampaignParticipantData getCampaignParticipantData(int userid, int campaignid) throws DAOException {
      CampaignParticipantData cpd = (CampaignParticipantData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_PARTICIPANT, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(userid), String.valueOf(campaignid)));
      if (cpd == null) {
         cpd = super.getCampaignParticipantData(userid, campaignid);
         if (cpd != null) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_PARTICIPANT, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(userid), String.valueOf(campaignid)), cpd);
         }
      }

      return cpd;
   }

   public CampaignParticipantData joinCampaign(CampaignParticipantData campaignParticipantData) throws DAOException {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se604UserAgentTrackingEnabled.getValue()) {
         CampaignParticipantData insertedCampaignParticipantData = super.joinCampaign(campaignParticipantData);
         if (insertedCampaignParticipantData != null) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_PARTICIPANT, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(campaignParticipantData.getUserId()), String.valueOf(campaignParticipantData.getCampaignId())), insertedCampaignParticipantData);
         }

         return insertedCampaignParticipantData;
      } else {
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_PARTICIPANT, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(campaignParticipantData.getUserId()), String.valueOf(campaignParticipantData.getCampaignId())), campaignParticipantData);
         super.joinCampaign(campaignParticipantData);
         return campaignParticipantData;
      }
   }

   public CampaignParticipantData getCampaignParticipantDataByMobilePhone(String mobilePhone, int campaignid) throws DAOException {
      CampaignParticipantData cpd = (CampaignParticipantData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_MOBILE, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(mobilePhone), String.valueOf(campaignid)));
      if (cpd == null) {
         cpd = super.getCampaignParticipantDataByMobilePhone(mobilePhone, campaignid);
         if (cpd != null) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CAMPAIGN_MOBILE, MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(mobilePhone), String.valueOf(campaignid)), cpd);
         }
      }

      return cpd;
   }
}
