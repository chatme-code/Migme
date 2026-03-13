package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MerchantLocationData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.merchant.MerchantPointsLogData;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class MerchantsBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantsBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private DataSource userRegistrationMaster;
   private DataSource userRegistrationSlave;
   private SecureRandom randomGen;
   private SessionContext context;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         this.userRegistrationMaster = LookupUtil.getRegistrationMasterDataSource();
         this.userRegistrationSlave = LookupUtil.getRegistrationSlaveDataSource();
         this.randomGen = new SecureRandom();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create RewardSystem EJB", var2);
         throw new CreateException("Unable to create RewardSystem EJB: " + var2.getMessage());
      }
   }

   public MerchantPointsLogData insertMerchantPoints(MerchantPointsLogData data) {
      try {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            String paymentsInsertSQL = "INSERT INTO merchantpointslog(datecreated,userid, points,type) VALUES( ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement("INSERT INTO merchantpointslog(datecreated,userid, points,type) VALUES( ?, ?, ?, ?)", 1);

            try {
               ps.setTimestamp(1, new Timestamp(data.getDateCreated().getTime()));
               ps.setInt(2, data.getUserid());
               ps.setInt(3, data.getPoints());
               ps.setShort(4, data.getType().code());
               if (ps.executeUpdate() != 1) {
                  throw new EJBException("Unable to insert merchant point reward record");
               }

               ResultSet rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  throw new EJBException("Failed to get id for the inserted mechant point reward record");
               }

               data.setId(rs.getLong(1));
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         return data;
      } catch (Exception var18) {
         throw new EJBException("failed to add merchant points.Exception:" + var18, var18);
      }
   }

   public List<MerchantLocationData> getMerchantsByCountry(int userId, int countryID, int offset, int limit, boolean retrieveProfile) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from merchantlocation ml left join location l on ml.locationid=l.id where l.countryid=? LIMIT ? , ?");
         ps.setInt(1, countryID);
         ps.setInt(2, offset);
         ps.setInt(3, limit != 0 ? limit : 20);
         rs = ps.executeQuery();
         var9 = this.getMerchantLocationData(userId, rs, retrieveProfile);
      } catch (MigboApiUtil.MigboApiException var25) {
         throw new EJBException("Exception occured getting migbo profiles", var25);
      } catch (Exception var26) {
         throw new EJBException(var26.getMessage(), var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

      return var9;
   }

   private List<MerchantLocationData> getMerchantLocationData(int userId, ResultSet rs, boolean retrieveProfile) throws Exception {
      MigboApiUtil api = MigboApiUtil.getInstance();
      String migboUsernames = "";
      int i = 0;
      HashMap<String, Integer> userIndex = new HashMap();
      ArrayList result = new ArrayList();

      while(rs.next()) {
         MerchantLocationData mld = new MerchantLocationData(rs);
         result.add(mld);
         if (retrieveProfile && !StringUtil.isBlank(mld.getUsername())) {
            String trimStr = mld.getUsername().trim();
            userIndex.put(trimStr, i);
            migboUsernames = migboUsernames + trimStr;
            if (rs.next()) {
               migboUsernames = migboUsernames + ",";
            }

            rs.previous();
            ++i;
         }
      }

      if (!StringUtil.isBlank(migboUsernames)) {
         log.info(String.format("Retrieving list of UserProfile from Migbo Dataservice for userid [%s]", migboUsernames));
         JSONObject obj = api.get(String.format("/user/batch/profile?useridOrUsernameOrAlias=%s&requestingUserid=%d&useUsername=1", migboUsernames, userId));
         JSONArray data = obj.getJSONArray("data");

         for(int j = 0; j < data.length(); ++j) {
            int index = (Integer)userIndex.get(data.getJSONObject(j).get("username"));
            if (index != -1) {
               JsonNode node = (new ObjectMapper()).readTree(data.getJSONObject(j).toString());
               ((MerchantLocationData)result.get(index)).setUserData(node);
            }
         }
      }

      return result;
   }

   public List<MerchantLocationData> getMerchantsByCountry(int userId, String countryName, int offset, int limit, boolean retrieveProfile) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from merchantlocation ml left join location l on ml.locationid=l.id inner join country c on l.countryid = c.id where LOWER(c.name)=? LIMIT ? , ?");
         ps.setString(1, countryName == null ? "" : countryName.toLowerCase());
         ps.setInt(2, offset);
         ps.setInt(3, limit != 0 ? limit : 20);
         rs = ps.executeQuery();
         var9 = this.getMerchantLocationData(userId, rs, retrieveProfile);
      } catch (MigboApiUtil.MigboApiException var25) {
         throw new EJBException("Exception occured getting migbo profiles", var25);
      } catch (Exception var26) {
         throw new EJBException(var26.getMessage(), var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

      return var9;
   }
}
