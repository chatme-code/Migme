package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.EmailDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

public class FusionDbEmailDAOChain extends EmailDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbEmailDAOChain.class);
   private static final String TRANSIENT = "Transient";

   public boolean isBounceEmailAddress(String email) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT bounceType FROM bouncedb WHERE emailaddress = ? limit 1");
         ps.setString(1, email);
         rs = ps.executeQuery();
         boolean var5;
         if (!rs.next()) {
            var5 = false;
            return var5;
         }

         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.ENABLED_SEND_TO_TRANSIENT_EMAIL) || !"Transient".equalsIgnoreCase(rs.getString("bounceType"))) {
            var5 = true;
            return var5;
         }

         var5 = false;
         return var5;
      } catch (SQLException var11) {
         log.error(String.format("Failed to check isBounceEmailAddress for email:%s", email), var11);
         var6 = super.isBounceEmailAddress(email);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }
}
