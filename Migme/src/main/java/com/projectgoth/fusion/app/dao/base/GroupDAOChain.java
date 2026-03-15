package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.data.GroupData;
import java.util.Set;

public class GroupDAOChain implements DAOChain {
   private GroupDAOChain nextRead;
   private GroupDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (GroupDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (GroupDAOChain)a;
   }

   public Set<String> getModeratorUserNames(int groupId, boolean fromMasterDB) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getModeratorUserNames(groupId, fromMasterDB);
      } else {
         throw new DAOException(String.format("Unable to get ModeratorUserNames for groupid:%s, fromMasterDB:%s", groupId, fromMasterDB));
      }
   }

   public GroupData getGroup(int groupID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getGroup(groupID);
      } else {
         throw new DAOException(String.format("Unable to retrieve group data for group id:%s", groupID));
      }
   }
}
