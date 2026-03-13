package com.projectgoth.fusion.voiceengine;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.VoiceGatewayData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.CreateException;
import javax.naming.AuthenticationException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class VoiceEngine extends Application {
   private static final String APP_NAME = "VoiceEngine";
   private static final String CONFIG_FILE = "VoiceEngine.cfg";
   private Map<Integer, AsteriskGateway> gateways;
   private RoutingTable routingTable;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VoiceEngine.class));

   private Map<Integer, AsteriskGateway> loadGatewaysFromDB(int variableFormat) throws CreateException, RemoteException {
      Map<Integer, AsteriskGateway> gateways = new ConcurrentHashMap();
      Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
      List<VoiceGatewayData> gatewayData = voiceEJB.getVoiceGateways();
      if (gatewayData != null) {
         Iterator i$ = gatewayData.iterator();

         while(i$.hasNext()) {
            VoiceGatewayData data = (VoiceGatewayData)i$.next();
            gateways.put(data.id, new AsteriskGateway(data, variableFormat));
         }
      }

      List<CallData> callsInProgress = voiceEJB.getCallEntries((String)null, CallData.StatusEnum.IN_PROGRESS.value());
      if (callsInProgress != null) {
         Iterator i$ = callsInProgress.iterator();

         while(i$.hasNext()) {
            CallData callData = (CallData)i$.next();
            if (callData.gateway == null) {
               log.warn("Unable to add call in progress. ID: " + callData.id + ". Gateway: " + callData.gateway);
            } else {
               AsteriskGateway gateway = (AsteriskGateway)gateways.get(callData.gateway);
               if (gateway == null) {
                  log.warn("Unable to add call in progress. ID: " + callData.id + ". Gateway: " + callData.gateway);
               } else {
                  gateway.addCallInProgress(callData);
               }
            }
         }
      }

      return gateways;
   }

   private RoutingTable buildRoutingTable(Collection<AsteriskGateway> gateways) {
      RoutingTable routingTable = new RoutingTable();
      if (gateways != null) {
         Iterator i$ = gateways.iterator();

         while(i$.hasNext()) {
            AsteriskGateway gateway = (AsteriskGateway)i$.next();
            routingTable.add((Collection)gateway.getData().voiceRoutes);
         }
      }

      routingTable.print();
      return routingTable;
   }

   private void pingAsteriskGateways() {
      Iterator i$ = this.gateways.values().iterator();

      while(i$.hasNext()) {
         AsteriskGateway gateway = (AsteriskGateway)i$.next();

         try {
            log.debug("Pinging gateway " + gateway.getData().id);
            gateway.ping();
         } catch (Exception var4) {
            log.warn(var4.getClass().getName() + " occured while ping gateway " + gateway.getData().id + " - " + var4.getMessage());
         }
      }

   }

   public int run(String[] arg0) {
      try {
         int variableFormat = communicator().getProperties().getPropertyAsIntWithDefault("VariableFormat", 1);
         this.gateways = this.loadGatewaysFromDB(variableFormat);
         this.routingTable = this.buildRoutingTable(this.gateways.values());
         CallMakerI callMaker = new CallMakerI(this.gateways, this.routingTable);
         Iterator i$ = this.gateways.values().iterator();

         while(i$.hasNext()) {
            AsteriskGateway gateway = (AsteriskGateway)i$.next();

            try {
               gateway.setCallMaker(callMaker);
               gateway.connect();
            } catch (AuthenticationException var11) {
               log.warn("Unable to start gateway " + gateway.getData().id + " - " + var11.getMessage());
            } catch (IOException var12) {
               log.warn("Unable to start gateway " + gateway.getData().id + " - " + var12.getMessage());
            }
         }

         long keepAliveInterval = (long)(communicator().getProperties().getPropertyAsIntWithDefault("KeepAliveInterval", 60) * 1000);
         if (keepAliveInterval > 0L) {
            (new Timer()).schedule(new TimerTask() {
               public void run() {
                  VoiceEngine.this.pingAsteriskGateways();
               }
            }, keepAliveInterval, keepAliveInterval);
         }

         ObjectAdapter callMakerAdaptor = communicator().createObjectAdapter("CallMakerAdapter");
         callMakerAdaptor.add(callMaker, Util.stringToIdentity("CallMaker"));
         callMakerAdaptor.activate();
         int fastAGIPort = communicator().getProperties().getPropertyAsIntWithDefault("FastAGIPort", 4573);
         int fastAGIThreads = communicator().getProperties().getPropertyAsIntWithDefault("FastAGIThreads", 20);
         int callThroughValidPeriod = communicator().getProperties().getPropertyAsIntWithDefault("CallThroughValidPeriod", 300) * 1000;
         FastAGIServer fastAGIServer = new FastAGIServer(callMaker, fastAGIPort, fastAGIThreads);
         fastAGIServer.setCallThroughValidPeriod(callThroughValidPeriod);
         fastAGIServer.start();
         log.info("Ready to receive call requests");
         communicator().waitForShutdown();
      } catch (CreateException var13) {
         log.fatal("Failed to load voice gateways and calls in progress from database", var13);
      } catch (RemoteException var14) {
         log.fatal("EJB excepion - " + RMIExceptionHelper.getRootMessage(var14), var14);
      } catch (IOException var15) {
         log.fatal("IO exception", var15);
      }

      return 0;
   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("VoiceEngine version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      VoiceEngine voiceEngine = new VoiceEngine();
      String configFile = args.length > 0 ? args[0] : "VoiceEngine.cfg";
      int status = voiceEngine.main(voiceEngine.getClass().getName(), args, configFile);
      System.exit(status);
   }
}
