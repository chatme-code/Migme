package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.MessageDAOChain;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class FusionDbMessageDAOChain extends MessageDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbMessageDAOChain.class);

   public Map<Integer, String> loadHelpTexts() throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from clienttext");
         rs = ps.executeQuery();
         HashMap helpTexts = new HashMap();

         while(rs.next()) {
            int type = rs.getInt("Type");
            if (type == 1) {
               helpTexts.put(rs.getInt("ID"), rs.getString("Text"));
            }
         }

         HashMap var13 = helpTexts;
         return var13;
      } catch (SQLException var10) {
         log.error("Failed to loadHelpTexts", var10);
         var5 = super.loadHelpTexts();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   public Map<Integer, String> loadInfoTexts() throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var5;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from clienttext");
         rs = ps.executeQuery();
         HashMap infoTexts = new HashMap();

         while(rs.next()) {
            int type = rs.getInt("Type");
            if (type == 2) {
               infoTexts.put(rs.getInt("ID"), rs.getString("Text"));
            }
         }

         HashMap var13 = infoTexts;
         return var13;
      } catch (SQLException var10) {
         log.error("Failed to loadInfoTexts", var10);
         var5 = super.loadInfoTexts();
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var5;
   }

   public List<AlertMessageData> getLatestAlertMessageList(int midletVersion, AlertMessageData.TypeEnum type, int countryId, Date minimumDate, AlertContentType alertContentType, int clientType) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var11;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "select * from alertmessage where MinMidletVersion <= ? and MaxMidletVersion >= ? and Type = ? and (CountryID = ? or CountryID is null) and StartDate <= now() and ExpiryDate > now() and Status = ? and clientType = ?";
         if (alertContentType != null) {
            sql = sql + " and ContentType = ?";
         }

         sql = sql + " order by CountryID";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, midletVersion);
         ps.setInt(2, midletVersion);
         ps.setInt(3, type.value());
         ps.setInt(4, countryId);
         ps.setInt(5, AlertMessageData.StatusEnum.ACTIVE.value());
         ps.setInt(6, clientType);
         if (alertContentType != null) {
            ps.setInt(7, alertContentType.value());
         }

         rs = ps.executeQuery();
         ArrayList alertMessages = new ArrayList();

         while(rs.next()) {
            alertMessages.add(new AlertMessageData(rs));
         }

         ArrayList var12 = alertMessages;
         return var12;
      } catch (SQLException var17) {
         log.error(String.format("Unable to get LatestAlertMessage List for midletVersion:%s, type:%s, country:%s, date:%s, contentype:%s, clientType:%s ", midletVersion, type, countryId, minimumDate, alertContentType, clientType), var17);
         var11 = super.getLatestAlertMessageList(midletVersion, type, countryId, minimumDate, alertContentType, clientType);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var11;
   }
}
