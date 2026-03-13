package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.GroupDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Group;
import com.projectgoth.fusion.interfaces.GroupHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.util.Set;
import org.apache.log4j.Logger;

public class EJBGroupDAOChain extends GroupDAOChain {
   private static final Logger log = Logger.getLogger(EJBGroupDAOChain.class);

   public Set<String> getModeratorUserNames(int groupId, boolean fromMasterDB) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETMODERATORUSERNAMES)) {
         return super.getModeratorUserNames(groupId, fromMasterDB);
      } else {
         try {
            Group groupEJB = (Group)EJBHomeCache.getObject("ejb/Group", GroupHome.class);
            return groupEJB.getModeratorUserNames(groupId, fromMasterDB);
         } catch (Exception var4) {
            log.warn(String.format("Failed to get ModeratorUserNames for group:%s, fromMasterDB:%s", groupId, fromMasterDB), var4);
            return super.getModeratorUserNames(groupId, fromMasterDB);
         }
      }
   }

   public GroupData getGroup(int groupID) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETGROUPDATA)) {
         return super.getGroup(groupID);
      } else {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            return userEJB.getGroup(groupID);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get GroupData for groupid:%s", groupID), var3);
            return super.getGroup(groupID);
         }
      }
   }
}
