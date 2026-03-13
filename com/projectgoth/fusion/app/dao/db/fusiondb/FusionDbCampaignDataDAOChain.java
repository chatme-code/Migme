package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.CampaignDataDAOChain;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class FusionDbCampaignDataDAOChain extends CampaignDataDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbCampaignDataDAOChain.class);

   public CampaignData getCampaignData(int campaignid) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CampaignData var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT * FROM campaign WHERE id = ?");
         ps.setInt(1, campaignid);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = new CampaignData(rs);
      } catch (SQLException var11) {
         log.error(String.format("Failed to check getCampaignData for campaignid:%s", campaignid), var11);
         CampaignData var6 = super.getCampaignData(campaignid);
         return var6;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   public CampaignParticipantData getCampaignParticipantData(int userid, int campaignid) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CampaignParticipantData var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT * FROM campaignparticipant WHERE campaignid = ? and userid =?");
         ps.setInt(1, campaignid);
         ps.setInt(2, userid);
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = new CampaignParticipantData(rs);
            return var6;
         }

         var6 = null;
      } catch (SQLException var12) {
         log.error(String.format("Failed to check getCampaignUserData for campaignid:%s userid:%s", campaignid, userid), var12);
         CampaignParticipantData var7 = super.getCampaignParticipantData(campaignid, userid);
         return var7;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid) throws DAOException {
      int[] type = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Campaign.CAMPAIGN_TYPE_WHITELIST);
      List<CampaignParticipantData> results = new ArrayList();

      for(int i = 0; i < type.length; ++i) {
         results.addAll(this.getActiveCampaignParticipantDataByType(userid, type[i]));
      }

      return results;
   }

   public List<CampaignParticipantData> getActiveCampaignParticipantDataByType(int userid, int type) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList results = new ArrayList();

      List var8;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT cp.* FROM campaignparticipant cp join campaign c on c.id= cp.campaignid WHERE c.type=? and cp.userid =? and c.status=1 and c.startdate<now() and c.enddate>now()");
         ps.setInt(1, type);
         ps.setInt(2, userid);
         rs = ps.executeQuery();

         while(rs.next()) {
            results.add(new CampaignParticipantData(rs));
         }

         ArrayList var7 = results;
         return var7;
      } catch (SQLException var13) {
         log.error(String.format("Failed to check getCampaignUserData for campaignid:%s userid:%s", type, userid), var13);
         var8 = super.getActiveCampaignParticipantDataByType(type, userid);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var8;
   }

   public CampaignParticipantData getCampaignParticipantDataByMobilePhone(String mobilePhone, int campaignId) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CampaignParticipantData var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT * FROM campaignparticipant WHERE campaignid = ? and mobilephone =?");
         ps.setInt(1, campaignId);
         ps.setString(2, mobilePhone);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = null;
            return var6;
         }

         var6 = new CampaignParticipantData(rs);
      } catch (SQLException var12) {
         log.error(String.format("Failed to check getCampaignUserData for campaignid:%s mobilePhone:%s", campaignId, mobilePhone), var12);
         CampaignParticipantData var7 = super.getCampaignParticipantDataByMobilePhone(mobilePhone, campaignId);
         return var7;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public CampaignParticipantData joinCampaign(CampaignParticipantData campaignParticipantData) throws DAOException {
      return (Boolean)SystemPropertyEntities.Temp.Cache.se604UserAgentTrackingEnabled.getValue() ? this.insertCampaignParticipantData(campaignParticipantData) : this.joinCampaignPreSE604(campaignParticipantData);
   }

   private CampaignParticipantData insertCampaignParticipantData(CampaignParticipantData campaignParticipantData) throws DAOException {
      try {
         Connection conn = DBUtils.getFusionWriteConnection();

         CampaignParticipantData var9;
         try {
            PreparedStatement ps = conn.prepareStatement("INSERT  INTO campaignparticipant (campaignid,userid, datecreated, mobilephone,emailaddress,reference,useragent) values (?, ?, now(),?,?,?,?)", 1);

            try {
               ps.setInt(1, campaignParticipantData.getCampaignId());
               ps.setInt(2, campaignParticipantData.getUserId());
               ps.setString(3, campaignParticipantData.getMobilePhone());
               ps.setString(4, campaignParticipantData.getEmailAddress());
               ps.setString(5, campaignParticipantData.getReference());
               String userAgent = StringUtil.isBlank(campaignParticipantData.getUserAgent()) ? null : DataUtils.truncateUserAgent(campaignParticipantData.getUserAgent().trim());
               ps.setString(6, userAgent);
               ps.executeUpdate();
               ResultSet rs = ps.getGeneratedKeys();

               try {
                  if (!rs.next()) {
                     throw new SQLException("Failed to register user [" + campaignParticipantData.getUserId() + "] to campaign [" + campaignParticipantData.getCampaignId() + "]");
                  }

                  long campaignParticipantID = rs.getLong(1);
                  CampaignParticipantData insertedCampaignParticipantData = new CampaignParticipantData(campaignParticipantData);
                  insertedCampaignParticipantData.setId(campaignParticipantID);
                  insertedCampaignParticipantData.setUserAgent(userAgent);
                  var9 = insertedCampaignParticipantData;
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         return var9;
      } catch (SQLException var31) {
         log.error(String.format("Failed to check joinCampaign for campaignid:%s userid:%s", campaignParticipantData.getCampaignId(), campaignParticipantData.getUserId()), var31);
         throw new DAOException(String.format("Failed to join campaign for campaignid:%s userid:%s", campaignParticipantData.getCampaignId(), campaignParticipantData.getUserId()), var31);
      }
   }

   /** @deprecated */
   private CampaignParticipantData joinCampaignPreSE604(CampaignParticipantData campaignParticpantData) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = DBUtils.getFusionWriteConnection();
         ps = conn.prepareStatement("INSERT  INTO campaignparticipant (campaignid,userid, datecreated, mobilephone,emailaddress,reference) values (?, ?, now(),?,?,?)");
         ps.setInt(1, campaignParticpantData.getCampaignId());
         ps.setInt(2, campaignParticpantData.getUserId());
         ps.setString(3, campaignParticpantData.getMobilePhone());
         ps.setString(4, campaignParticpantData.getEmailAddress());
         ps.setString(5, campaignParticpantData.getReference());
         ps.executeUpdate();
      } catch (SQLException var10) {
         log.error(String.format("Failed to check joinCampaign for campaignid:%s userid:%s", campaignParticpantData.getCampaignId(), campaignParticpantData.getUserId()), var10);
         throw new DAOException(String.format("Failed to join campaign for campaignid:%s userid:%s", campaignParticpantData.getCampaignId(), campaignParticpantData.getUserId()), var10);
      } finally {
         DBUtils.closeResource((ResultSet)rs, ps, conn, log);
      }

      return campaignParticpantData;
   }
}
