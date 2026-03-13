package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.GuardsetDAOChain;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.GuardsetCapabilityTypeEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

public class FusionDbGuardsetDAOChain extends GuardsetDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbGuardsetDAOChain.class);

   public Short getMinimumClientVersionForAccess(int clientType, int guardCapability) throws DAOException {
      if (ClientType.fromValue(clientType) == null) {
         return null;
      } else if (GuardCapabilityEnum.fromValue(guardCapability) == null) {
         return null;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         Short var8;
         try {
            conn = DBUtils.getFusionReadConnection();
            String sql = "SELECT cv.clientversion FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs, clientversion cv WHERE gsc.capabilitytype= ? AND gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gsm.membertype = ? AND gc.id = ? AND gsm.memberid = cv.id AND cv.clienttype = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, GuardsetCapabilityTypeEnum.GUARD_BY_MIN_CLIENT_VERSION.value());
            ps.setInt(2, MigboAccessMemberTypeEnum.MIN_VERSION.value());
            ps.setInt(3, guardCapability);
            ps.setInt(4, clientType);
            rs = ps.executeQuery();
            Short minClientVersion;
            if (rs.next()) {
               minClientVersion = (short)rs.getInt(1);
            } else {
               minClientVersion = 32767;
            }

            return minClientVersion != null && minClientVersion >= 32767 ? null : minClientVersion;
         } catch (Exception var13) {
            log.error("Unable to retrieve getMinimumClientVersionForAccess", var13);
            var8 = super.getMinimumClientVersionForAccess(clientType, guardCapability);
         } finally {
            DBUtils.closeResource(rs, ps, conn, log);
         }

         return var8;
      }
   }
}
