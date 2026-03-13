package com.projectgoth.fusion.common;

import Ice.Communicator;
import Ice.InitializationData;
import Ice.ObjectAdapter;
import Ice.Util;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class BasicIceServerApp {
   private final Communicator communicator;
   private final LinkedHashMap<String, ObjectAdapter> endPointToObjectAdapterMap;
   private final Logger logger;

   private static Logger getLogger(String name) {
      return Logger.getLogger(ConfigUtils.getLoggerName(BasicIceServerApp.class) + (StringUtil.isBlank(name) ? "" : "|" + name));
   }

   private static Communicator buildCommunicator(String configFile, Properties overrideProperties) {
      Ice.Properties iceProperties = Util.createProperties();
      if (!StringUtil.isBlank(configFile)) {
         iceProperties.load(configFile);
      }

      if (overrideProperties != null && !overrideProperties.isEmpty()) {
         Iterator i$ = overrideProperties.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Object, Object> entry = (Entry)i$.next();
            if (entry.getKey() != null) {
               String overridenKey = entry.getKey().toString();
               if (entry.getValue() != null) {
                  String overridenValue = entry.getValue().toString();
                  iceProperties.setProperty(overridenKey, overridenValue);
               }
            }
         }
      }

      InitializationData iceInitData = new InitializationData();
      iceInitData.properties = iceProperties;
      return Util.initialize(iceInitData);
   }

   public BasicIceServerApp(String name, String configFile) {
      this(name, configFile, (Properties)null);
   }

   public BasicIceServerApp(String name, String configFile, Properties overrideProperties) {
      this.endPointToObjectAdapterMap = new LinkedHashMap();
      this.logger = getLogger(name);
      this.communicator = buildCommunicator(configFile, overrideProperties);
   }

   public BasicIceServerApp(String name, Communicator communicator) {
      this.endPointToObjectAdapterMap = new LinkedHashMap();
      this.logger = getLogger(name);
      this.communicator = communicator;
   }

   private ObjectAdapter addObjectAdapter(String endPointName) {
      ObjectAdapter adapter = this.communicator.createObjectAdapter(endPointName);
      this.endPointToObjectAdapterMap.put(endPointName, adapter);
      return adapter;
   }

   public void addServantObject(String endPointName, Ice.Object iceObject, String identityStr) {
      ObjectAdapter objectAdapter = (ObjectAdapter)this.endPointToObjectAdapterMap.get(endPointName);
      if (objectAdapter == null) {
         objectAdapter = this.addObjectAdapter(endPointName);
      }

      objectAdapter.add(iceObject, Util.stringToIdentity(identityStr));
   }

   private void activateAdapters() {
      Iterator i$ = this.endPointToObjectAdapterMap.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, ObjectAdapter> entry = (Entry)i$.next();
         String endPointName = (String)entry.getKey();
         ObjectAdapter objectAdapter = (ObjectAdapter)entry.getValue();
         this.logger.info("Activating endpoint adapter:[" + endPointName + "]");
         objectAdapter.activate();
         this.logger.info("Endpoint adapter:[" + endPointName + "] activated.");
      }

   }

   public void activateAdaptersAndWaitTillShutdown() {
      this.activateAdapters();
      this.logger.info("Services started.");
      this.communicator.waitForShutdown();
   }

   public void shutdown() {
      this.logger.info("Shutting down communicator");
      this.communicator.shutdown();
   }

   public static Thread startInANewThread(String threadName, boolean isDaemon, final BasicIceServerApp serverApp) {
      Thread thread = new Thread(new Runnable() {
         public void run() {
            serverApp.activateAdaptersAndWaitTillShutdown();
            serverApp.logger.info("Servicing thread terminating...");
         }
      }, threadName);
      thread.setDaemon(isDaemon);
      serverApp.logger.info("Starting initiator thread for ice app");
      thread.start();
      return thread;
   }
}
