package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.CampaignDataDAOChain;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import java.util.List;

public class CampaignDataDAO {
   private CampaignDataDAOChain readChain;
   private CampaignDataDAOChain writeChain;

   public CampaignDataDAO(CampaignDataDAOChain readChain, CampaignDataDAOChain writeChain) {
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public CampaignData getCampaignData(int campaignid) throws DAOException {
      return this.readChain.getCampaignData(campaignid);
   }

   public CampaignParticipantData getCampaignParticipantData(int userid, int campaignid) throws DAOException {
      return this.readChain.getCampaignParticipantData(userid, campaignid);
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid, int type) throws DAOException {
      return this.readChain.getActiveCampaignParticipantDataByType(userid, type);
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid) throws DAOException {
      return this.readChain.getActiveCampaignParticipantDataByType(userid);
   }

   public CampaignParticipantData getCampaignParticipantDataByMobilePhone(String mobilePhone, int campaignId) throws DAOException {
      return this.readChain.getCampaignParticipantDataByMobilePhone(mobilePhone, campaignId);
   }

   public void joinCampaign(CampaignParticipantData campaignUserData) throws DAOException {
      this.writeChain.joinCampaign(campaignUserData);
   }
}
