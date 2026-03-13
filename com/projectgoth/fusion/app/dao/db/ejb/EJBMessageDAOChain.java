package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.MessageDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import org.apache.log4j.Logger;

public class EJBMessageDAOChain extends MessageDAOChain {
   private static final Logger log = Logger.getLogger(EJBMessageDAOChain.class);

   public String getInfoText(int infoID) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETINFOTEXT)) {
         return super.getInfoText(infoID);
      } else {
         try {
            MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
            return misEJB.getInfoText(infoID);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get InfoText for infoID:%s", infoID), var3);
            return super.getInfoText(infoID);
         }
      }
   }
}
