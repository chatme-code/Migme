package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.BotDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import org.apache.log4j.Logger;

public class EJBBotDAOChain extends BotDAOChain {
   private static final Logger log = Logger.getLogger(EJBBotDAOChain.class);

   public BotData getBot(int botID) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETBOT)) {
         return super.getBot(botID);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getBot(botID);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get BotData for bot:%s", botID), var3);
            return super.getBot(botID);
         }
      }
   }
}
