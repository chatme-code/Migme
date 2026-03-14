package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.GroupDAOChain;
import com.projectgoth.fusion.data.GroupData;
import java.util.Set;

public class GroupDAO {
   private GroupDAOChain readChain;
   private GroupDAOChain writeChain;

   public GroupDAO(GroupDAOChain readChain, GroupDAOChain writeChain) {
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public Set<String> getModeratorUserNames(int groupId, boolean fromMasterDB) throws DAOException {
      return this.readChain.getModeratorUserNames(groupId, fromMasterDB);
   }

   public GroupData getGroup(int groupID) throws DAOException {
      return this.readChain.getGroup(groupID);
   }
}
