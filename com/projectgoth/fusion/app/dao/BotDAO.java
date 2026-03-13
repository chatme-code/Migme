package com.projectgoth.fusion.app.dao;

import com.projectgoth.fusion.app.dao.base.BotDAOChain;
import com.projectgoth.fusion.data.BotData;

public class BotDAO {
   private BotDAOChain readChain;
   private BotDAOChain writeChain;

   public BotDAO(BotDAOChain readChain, BotDAOChain writeChain) {
      this.readChain = readChain;
      this.writeChain = writeChain;
   }

   public BotData getBot(int botID) throws DAOException {
      return this.readChain.getBot(botID);
   }
}
