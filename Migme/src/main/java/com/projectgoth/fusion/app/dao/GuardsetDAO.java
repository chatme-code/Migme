package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.GuardsetDAOChain;

public class GuardsetDAO {
   private GuardsetDAOChain readChain;
   private GuardsetDAOChain writeChain;

   public GuardsetDAO(GuardsetDAOChain readChain, GuardsetDAOChain writeChain) {
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public Short getMinimumClientVersionForAccess(int clientType, int guardCapability) throws DAOException {
      return this.readChain.getMinimumClientVersionForAccess(clientType, guardCapability);
   }
}
