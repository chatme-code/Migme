package com.projectgoth.fusion.ejb.util;

import com.projectgoth.fusion.common.ConfigUtils;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class LookupUtil {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LookupUtil.class));

   public static String getResourcePath(String resourceName) {
      String prefix = System.getProperty("config.jndi_resourceprefix", "java:/");
      return prefix + resourceName;
   }

   private static DataSource getDataSource(String path) {
      DataSource dataSource = null;

      try {
         InitialContext ctx = new InitialContext();
         dataSource = (DataSource)ctx.lookup(getResourcePath(path));
         ctx.close();
      } catch (NamingException var3) {
         log.error("unable to lookup datasource " + path, var3);
      }

      return dataSource;
   }

   public static DataSource getFusionMasterDataSource() {
      return getDataSource("jdbc/FusionDB");
   }

   public static DataSource getFusionSlaveDataSource() {
      return getDataSource("jdbc/FusionDBSlave");
   }

   public static DataSource getRegistrationMasterDataSource() {
      return getDataSource("jdbc/UserRegistrationDB");
   }

   public static DataSource getRegistrationSlaveDataSource() {
      return getDataSource("jdbc/UserRegistrationDBSlave");
   }
}
