package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.GroupDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;

public class FusionDbGroupDAOChain extends GroupDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbGroupDAOChain.class);

   public Set<String> getModeratorUserNames(int groupId, boolean fromMasterDB) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Set var7;
      try {
         conn = fromMasterDB ? DBUtils.getFusionWriteConnection() : DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("SELECT gm.username as username FROM groupmember gm WHERE gm.groupid = ?  AND gm.status = ?  AND gm.type = ?");
         ps.setInt(1, groupId);
         ps.setInt(2, GroupMemberData.StatusEnum.ACTIVE.value());
         ps.setInt(3, GroupMemberData.TypeEnum.MODERATOR.value());
         rs = ps.executeQuery();
         TreeSet result = new TreeSet();

         while(rs.next()) {
            result.add(rs.getString("username"));
         }

         TreeSet var14 = result;
         return var14;
      } catch (SQLException var12) {
         log.error(String.format("Failed to get ModeratorUserNames for user:%s, fromMasterDB:%s", groupId, fromMasterDB));
         var7 = super.getModeratorUserNames(groupId, fromMasterDB);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var7;
   }

   public GroupData getGroup(int groupID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      GroupData groupData;
      try {
         GroupData var6;
         try {
            conn = DBUtils.getFusionReadConnection();
            ps = conn.prepareStatement("select groups.*, service.status vipservicestatus from groups LEFT OUTER JOIN service ON (groups.vipserviceid=service.id and service.status=1) where groups.id=? and groups.status=1");
            ps.setInt(1, groupID);
            rs = ps.executeQuery();
            if (rs.next()) {
               groupData = new GroupData(rs);
               rs.close();
               ps.close();
               var6 = groupData;
               return var6;
            }

            log.warn(String.format("FIXME: Failed to get group data for groupid:%s in fusion database", groupID));
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHAIN_ON_IF_SHOULD_NOT_REACH_HERE_HAPPENS)) {
               throw new DAOException(String.format("Failed to find group data for groupid:%s in fusion database", groupID));
            }

            groupData = super.getGroup(groupID);
         } catch (SQLException var11) {
            log.error(String.format("Failed to get group data for groupid:%s", groupID), var11);
            var6 = super.getGroup(groupID);
            return var6;
         }
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return groupData;
   }
}
