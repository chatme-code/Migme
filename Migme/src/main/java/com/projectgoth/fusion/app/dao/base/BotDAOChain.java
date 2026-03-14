package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.data.BotData;

public class BotDAOChain implements DAOChain {
   private BotDAOChain nextRead;
   private BotDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (BotDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (BotDAOChain)a;
   }

   public BotData getBot(int botID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getBot(botID);
      } else {
         throw new DAOException(String.format("Failed to get BotData for bot:", botID));
      }
   }
}
