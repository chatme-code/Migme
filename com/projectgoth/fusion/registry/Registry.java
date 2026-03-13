package com.projectgoth.fusion.registry;

import Ice.Application;
import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.slice.RegistryNodePrx;
import com.projectgoth.fusion.slice.RegistryNodePrxHelper;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Registry extends Application {
   private static final String APP_NAME = "Registry";
   private static final String CONFIG_FILE = "Registry.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Registry.class));
   private static ObjectAdapter registryAdapter = null;
   private static Properties properties = null;
   private static RegistryNodePrx thisNodePrx = null;
   private static RegistryI registry = null;
   private static RegistryNodeI registryNode = null;
   private static String hostName = null;
   public static long startTime = System.currentTimeMillis();
   private static Communicator communicator;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("Registry version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      Registry app = new Registry();
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("Registry", args, args[0]);
      } else {
         status = app.main("Registry", args, "Registry.cfg");
      }

      System.exit(status);
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var9) {
         hostName = "UNKNOWN";
      }

      RegistryContextBuilder applicationContext = new RegistryContextBuilder();
      applicationContext.setProperties(properties).setCommunicator(communicator());
      log.debug("Initialising RegistryNode interface");
      ObjectAdapter registryNodeAdapter = communicator().createObjectAdapter("RegistryNodeAdapter");
      registryNode = new RegistryNodeI(applicationContext);
      applicationContext.setRegistryNode(registryNode);
      ObjectPrx basePrx = registryNodeAdapter.add(registryNode, Util.stringToIdentity("RegistryNode"));
      thisNodePrx = RegistryNodePrxHelper.uncheckedCast(basePrx);
      applicationContext.setThisNodePrx(thisNodePrx);
      registryNodeAdapter.activate();
      log.debug("Initialising Registry interface");
      registryAdapter = communicator().createObjectAdapter("RegistryAdapter");
      IceStats.getInstance().setIceObjects(communicator(), registryAdapter, (ConfigurableExecutor)null);
      registry = new RegistryI(applicationContext);
      applicationContext.setRegistry(registry);
      registryAdapter.add(registry, Util.stringToIdentity("Registry"));
      applicationContext.build();
      log.debug("Initialising RegistryAdmin interface");
      ObjectAdapter registryAdminAdapter = communicator().createObjectAdapter("RegistryAdminAdapter");
      RegistryAdminI registryAdmin = new RegistryAdminI(applicationContext);
      registryAdminAdapter.add(registryAdmin, Util.stringToIdentity("RegistryAdmin"));
      registryAdminAdapter.activate();

      try {
         registryNode.convergeWithCluster();
      } catch (Exception var8) {
         return 0;
      }

      registryAdapter.activate();
      log.info("Service started");
      communicator().waitForShutdown();
      if (interrupted()) {
         log.fatal("Registry " + hostName + ": terminating");
      }

      return 0;
   }
}
