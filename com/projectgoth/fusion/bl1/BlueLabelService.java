package com.projectgoth.fusion.bl1;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class BlueLabelService extends Application {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BlueLabelService.class));
   public static ObjectAdapter BlueLabelServiceAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private BlueLabelServiceI blueLabelServiceServant;

   @Required
   public void setBlueLabelServiceServant(BlueLabelServiceI blueLabelServiceServant) {
      this.blueLabelServiceServant = blueLabelServiceServant;
   }

   private void configureServant(Properties properties) {
      this.blueLabelServiceServant.setCreateAccountEntries(Boolean.parseBoolean(properties.getPropertyWithDefault("createAccountEntries", "false")));
      this.blueLabelServiceServant.setMibliUsername(properties.getProperty("mibli.username"));
      this.blueLabelServiceServant.setMibliPassword(properties.getProperty("mibli.password"));
      this.blueLabelServiceServant.setMibliWSDLURL(properties.getProperty("mibli.wsdlURL"));
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();
      this.configureServant(properties);

      try {
         this.blueLabelServiceServant.configureService();
      } catch (Exception var5) {
         log.fatal("failed to configure bluelabel service servant", var5);
         communicator().shutdown();
         return 1;
      }

      log.info("Configured endpoint [" + properties.getProperty("BlueLabelServiceAdapter.Endpoints") + "]");

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var4) {
         this.hostName = "UNKNOWN";
      }

      log.debug("Initialising BlueLabelService interface");
      BlueLabelServiceAdapter = communicator().createObjectAdapter("BlueLabelServiceAdapter");
      BlueLabelServiceAdapter.add(this.blueLabelServiceServant, Util.stringToIdentity("BlueLabelService"));
      IceStats.getInstance().setIceObjects(communicator(), BlueLabelServiceAdapter, (ConfigurableExecutor)null);
      log.debug("Initialising BlueLabelServiceAdmin interface");
      ObjectAdapter BlueLabelServiceAdminAdapter = communicator().createObjectAdapter("BlueLabelServiceAdminAdapter");
      BlueLabelServiceAdminI blueLabelServiceAdmin = new BlueLabelServiceAdminI(this.blueLabelServiceServant);
      BlueLabelServiceAdminAdapter.add(blueLabelServiceAdmin, Util.stringToIdentity("BlueLabelServiceAdmin"));
      BlueLabelServiceAdminAdapter.activate();
      BlueLabelServiceAdapter.activate();
      log.info("Service started");
      communicator().waitForShutdown();
      if (interrupted()) {
         log.fatal("BlueLabelService " + this.hostName + ": terminating");
         this.blueLabelServiceServant.shutdown();
      }

      return 0;
   }
}
