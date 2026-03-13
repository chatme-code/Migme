package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.GuardsetDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Guardset;
import com.projectgoth.fusion.interfaces.GuardsetHome;
import org.apache.log4j.Logger;

public class EJBGuardsetDAOChain extends GuardsetDAOChain {
   private static final Logger log = Logger.getLogger(EJBGuardsetDAOChain.class);

   public Short getMinimumClientVersionForAccess(int clientType, int guardCapability) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETMINCLIENTVERSIONFORACCESS)) {
         return super.getMinimumClientVersionForAccess(clientType, guardCapability);
      } else {
         try {
            Guardset guardsetEJB = (Guardset)EJBHomeCache.getObject("ejb/Guardset", GuardsetHome.class);
            return guardsetEJB.getMinimumClientVersionForAccess(clientType, guardCapability);
         } catch (Exception var4) {
            log.warn(String.format("Unable to retrieve min client version for access, clientType:%s, guardCapability:%s", clientType, guardCapability));
            return super.getMinimumClientVersionForAccess(clientType, guardCapability);
         }
      }
   }
}
