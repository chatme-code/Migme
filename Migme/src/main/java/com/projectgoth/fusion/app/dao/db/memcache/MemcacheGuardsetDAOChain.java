package com.projectgoth.fusion.app.dao.db.memcache;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.GuardsetDAOChain;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;

public class MemcacheGuardsetDAOChain extends GuardsetDAOChain {
   private static final String MEMCACHE_KEY_SEP = ":";

   public Short getMinimumClientVersionForAccess(int clientType, int guardCapability) throws DAOException {
      Short minClientVersion = null;
      String cacheValue = (String)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.MINIMUM_CLIENT_VERSION, makeMemcacheKey(clientType, guardCapability));
      if (StringUtil.isBlank(cacheValue)) {
         minClientVersion = super.getMinimumClientVersionForAccess(clientType, guardCapability);
         Short savedValue = minClientVersion == null ? 32767 : minClientVersion;
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MINIMUM_CLIENT_VERSION, makeMemcacheKey(clientType, guardCapability), savedValue);
      } else if (Short.parseShort(cacheValue) == 32767) {
         minClientVersion = null;
      } else {
         minClientVersion = Short.parseShort(cacheValue);
      }

      return minClientVersion;
   }

   public static String makeMemcacheKey(int clientType, int guardCapability) {
      return Integer.toString(clientType) + ":" + Integer.toString(guardCapability);
   }
}
