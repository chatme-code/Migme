package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import java.util.List;

public class CampaignDataDAOChain implements DAOChain {
   private CampaignDataDAOChain nextRead;
   private CampaignDataDAOChain nextWrite;

   public void setNextRead(DAOChain nextRead) {
      this.nextRead = (CampaignDataDAOChain)nextRead;
   }

   public void setNextWrite(DAOChain nextWrite) {
      this.nextWrite = (CampaignDataDAOChain)nextWrite;
   }

   public CampaignData getCampaignData(int campaignid) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getCampaignData(campaignid);
      } else {
         throw new DAOException(String.format("Unable to retrieve campaign data for campaign id:%s", campaignid));
      }
   }

   public CampaignParticipantData getCampaignParticipantData(int userid, int campaignid) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getCampaignParticipantData(userid, campaignid);
      } else {
         throw new DAOException(String.format("Unable to retrieve campaign data for campaign id:%s userid:%s", campaignid, userid));
      }
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getActiveCampaignParticipantDataByType(userid);
      } else {
         throw new DAOException(String.format("Unable to retrieve campaign data for userid:%s", userid));
      }
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid, int type) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getActiveCampaignParticipantDataByType(userid, type);
      } else {
         throw new DAOException(String.format("Unable to retrieve campaign data for type id:%s userid:%s", type, userid));
      }
   }

   public CampaignParticipantData joinCampaign(CampaignParticipantData campaignUserData) throws DAOException {
      if (this.nextWrite != null) {
         return this.nextWrite.joinCampaign(campaignUserData);
      } else {
         throw new DAOException(String.format("Unable to update campaign data for campaign id:%s userid:%s", campaignUserData.getCampaignId(), campaignUserData.getUserId()));
      }
   }

   public CampaignParticipantData getCampaignParticipantDataByMobilePhone(String mobilePhone, int campaignId) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getCampaignParticipantDataByMobilePhone(mobilePhone, campaignId);
      } else {
         throw new DAOException(String.format("Unable to update campaign data for campaign id:%s mobile:%s", campaignId, mobilePhone));
      }
   }
}
