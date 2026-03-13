package com.projectgoth.fusion.voiceengine;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.VoiceRouteData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.slice.CallDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._CallMakerDisp;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class CallMakerI extends _CallMakerDisp implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CallMakerI.class));
   private Map<Integer, AsteriskGateway> gateways;
   private RoutingTable routingTable;
   private Set<String> usersInCall;
   private List<CallData> callsCompleted;
   private Map<Integer, CallMakerI.CallParam> callParams;

   public CallMakerI(Map<Integer, AsteriskGateway> gateways, RoutingTable routingTable) {
      this.gateways = gateways;
      this.routingTable = routingTable;
      this.usersInCall = Collections.synchronizedSet(new HashSet());
      this.callsCompleted = Collections.synchronizedList(new LinkedList());
      this.callParams = new ConcurrentHashMap();
      Iterator i$ = gateways.values().iterator();

      while(i$.hasNext()) {
         AsteriskGateway gateway = (AsteriskGateway)i$.next();
         Iterator i$ = gateway.getCallsInProgress().iterator();

         while(i$.hasNext()) {
            CallData callData = (CallData)i$.next();
            this.usersInCall.add(callData.username);
         }
      }

      (new Thread(this)).start();
   }

   public boolean shouldRetry(int isdnCause) {
      return isdnCause == 8 || isdnCause == 34 || isdnCause == 38 || isdnCause == 41 || isdnCause == 42 || isdnCause == 47 || isdnCause == 55 || isdnCause == 57 || isdnCause == 58 || isdnCause == 88;
   }

   public CallDataIce requestCallback(CallDataIce call, int maxDuration, int retries, Current __current) throws FusionException {
      boolean userAdded = false;

      try {
         CallData callData = new CallData(call);
         if (callData.username == null) {
            throw new Exception("Invalid or null username for callback");
         } else {
            CallMakerI.CallParam callParam = (CallMakerI.CallParam)this.callParams.get(callData.id);
            if (callParam == null) {
               ++retries;
               callParam = new CallMakerI.CallParam(maxDuration, retries);
               this.callParams.put(callData.id, callParam);
               log.info(callData.username + " requesting a callback. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ". Retries+1: " + retries + ".");
               userAdded = this.usersInCall.add(callData.username);
               if (!userAdded) {
                  throw new Exception(callData.username + " already in another call for callback");
               }
            } else {
               ++callParam.retryAttempt;
               if (retries > 0) {
                  log.info(callData.username + " retrying callback with same route. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ". Attempt: " + callParam.retryAttempt + ".");
               } else if (retries == 0) {
                  log.info(callData.username + " retrying callback with next provider. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ". Attempt: " + callParam.retryAttempt + ".");
               }
            }

            if (retries == 0 && callParam.retryAttempt > 0) {
               int isdnCause = callData.failReasonCode != null ? callData.failReasonCode : 16;
               if (!this.shouldRetry(isdnCause)) {
                  throw new Exception(callData.username + " exhausted all retries - " + callData.failReason);
               }
            }

            List<VoiceRouteData> routes = this.routingTable.getSourceRoutes(callData);
            if (retries == 0 && callParam.retryAttempt > 0) {
               VoiceRouteData nextRoute = this.routingTable.getSourceNextRoute(callData, callData.gateway, callData.sourceProvider);
               if (nextRoute == null || nextRoute.providerID == Integer.MIN_VALUE) {
                  log.warn("Exhausted all callback routes for source failover. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ". Attempt: " + callParam.retryAttempt + ".");
                  throw new Exception(callData.username + " exhausted all retries - " + callData.failReason);
               }

               routes.add(0, nextRoute);
            }

            Iterator i$ = routes.iterator();

            while(i$.hasNext()) {
               VoiceRouteData route = (VoiceRouteData)i$.next();
               VoiceRouteData dRoute = this.routingTable.getDestinationRoute(callData, route.gatewayID);
               VoiceRouteData dNextRoute = this.routingTable.getDestinationNextRoute(callData, dRoute.gatewayID, dRoute.providerID);
               if (dNextRoute == null || dNextRoute.providerID == Integer.MIN_VALUE) {
                  log.warn("Exhausted all callback routes for destination failover. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ". Attempt: " + callParam.retryAttempt + ".");
                  dNextRoute = new VoiceRouteData();
                  dNextRoute.gatewayID = Integer.MIN_VALUE;
                  dNextRoute.providerID = Integer.MIN_VALUE;
                  dNextRoute.dialCommand = "";
               }

               callData.gateway = route.gatewayID;
               callData.sourceProvider = route.providerID;
               callData.destinationProvider = dRoute.providerID;
               callData.destinationFirstProvider = dRoute.providerID;
               callData.destinationNextProvider = dNextRoute.providerID;
               callData.sourceDialCommand = route.dialCommand;
               callData.destinationDialCommand = dRoute.dialCommand;
               callData.destinationFirstDialCommand = dRoute.dialCommand;
               callData.destinationNextDialCommand = dNextRoute.dialCommand;
               callData.maxDuration = maxDuration;
               callData = ((AsteriskGateway)this.gateways.get(route.gatewayID)).requestCallback(callData, callParam.retryAttempt > 0, false);
               if (callData.status == CallData.StatusEnum.IN_PROGRESS) {
                  log.info("Successfully initiated a callback. ID: " + callData.id + ". Gateway: " + callData.gateway + ". SP: " + callData.sourceProvider + ". DP: " + callData.destinationProvider + ". DNP: " + callData.destinationNextProvider + ".");
                  return callData.toIceObject();
               }

               log.warn("Trying next route for callback. ID: " + callData.id + ". Gateway: " + callData.gateway + ". SP: " + callData.sourceProvider + ". DP: " + callData.destinationProvider + ". DNP: " + callData.destinationNextProvider + ". Error: " + callData.failReason + ".");
            }

            throw new Exception(callData.failReason);
         }
      } catch (Exception var13) {
         log.warn("Callback request failed. ID: " + call.id + ". Error: " + var13.getMessage() + ".");
         if (userAdded) {
            this.usersInCall.remove(call.username);
         }

         this.callParams.remove(call.id);
         FusionException fe = new FusionException();
         fe.message = var13.getMessage();
         throw fe;
      }
   }

   public CallData requestCall(CallData callData, int gatewayId) throws Exception {
      boolean userAdded = false;

      try {
         if (callData == null) {
            throw new Exception("Invalid or null call data for request call");
         } else if (callData.username == null) {
            throw new Exception("Invalid or null username for request call");
         } else {
            log.info(callData.username + " requesting a request call. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ".");
            AsteriskGateway gateway = (AsteriskGateway)this.gateways.get(gatewayId);
            if (gateway == null) {
               throw new Exception("Invalid gateway ID " + gatewayId + " for request call");
            } else {
               callData.gateway = gateway.getData().id;
               userAdded = this.usersInCall.add(callData.username);
               if (!userAdded) {
                  throw new Exception(callData.username + " already in another call for request call");
               } else {
                  if (callData.destination != null && callData.destinationIDDCode != null) {
                     VoiceRouteData dRoute = this.routingTable.getDestinationRoute(callData, gatewayId);
                     VoiceRouteData dNextRoute = this.routingTable.getDestinationNextRoute(callData, dRoute.gatewayID, dRoute.providerID);
                     if (dNextRoute == null || dNextRoute.providerID == Integer.MIN_VALUE) {
                        log.warn("Exhausted all request call routes for destination failover. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ".");
                        dNextRoute = new VoiceRouteData();
                        dNextRoute.gatewayID = Integer.MIN_VALUE;
                        dNextRoute.providerID = Integer.MIN_VALUE;
                        dNextRoute.dialCommand = "";
                     }

                     callData.destinationProvider = dRoute.providerID;
                     callData.destinationFirstProvider = dRoute.providerID;
                     callData.destinationNextProvider = dNextRoute.providerID;
                     callData.destinationDialCommand = dRoute.dialCommand;
                     callData.destinationFirstDialCommand = dRoute.dialCommand;
                     callData.destinationNextDialCommand = dNextRoute.dialCommand;
                  }

                  callData = gateway.requestCall(callData, false);
                  if (callData.status == CallData.StatusEnum.IN_PROGRESS) {
                     log.info("Successfully initiated a request call. ID: " + callData.id + ". Gateway: " + callData.gateway + ". SP: " + callData.sourceProvider + ". DP: " + callData.destinationProvider + ". DNP: " + callData.destinationNextProvider + ".");
                     return callData;
                  } else {
                     throw new Exception(callData.failReason);
                  }
               }
            }
         }
      } catch (Exception var7) {
         log.warn("Request call request failed. ID: " + callData.id + ". Error: " + var7.getMessage() + ".");
         if (userAdded) {
            this.usersInCall.remove(callData.username);
         }

         throw var7;
      }
   }

   public void cancelCall(CallData callData) {
      if (callData.gateway != null) {
         AsteriskGateway gateway = (AsteriskGateway)this.gateways.get(callData.gateway);
         if (gateway != null) {
            gateway.cancelCall(callData);
            this.usersInCall.remove(callData.username);
         }
      }

   }

   public void callCompleted(CallData callData, boolean retry) {
      if (retry && (callData.initialLeg == CallData.InitialLegEnum.SOURCE && (callData.sourceDuration == null || callData.sourceDuration == 0L) || callData.initialLeg == CallData.InitialLegEnum.DESTINATION && (callData.destinationDuration == null || callData.destinationDuration == 0L))) {
         CallMakerI.CallParam callParam = (CallMakerI.CallParam)this.callParams.get(callData.id);
         if (callParam != null && callParam.retries > 0) {
            try {
               this.requestCallback(callData.toIceObject(), callParam.maxDuration, --callParam.retries);
               return;
            } catch (Exception var5) {
            }
         }
      }

      this.callsCompleted.add(callData);
   }

   public CallRequest fillInCallRequest(CallData callData, int gatewayId) {
      try {
         if (callData == null) {
            log.warn("Invalid or null call data for call request.");
            return null;
         } else if (callData.username == null) {
            log.warn("Invalid or null username for call request. ID: " + callData.id + ".");
            return null;
         } else {
            log.info(callData.username + " filling in call request. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ".");
            AsteriskGateway gateway = (AsteriskGateway)this.gateways.get(gatewayId);
            if (gateway == null) {
               log.warn("Invalid gateway ID " + gatewayId + " for call request. ID: " + callData.id + ".");
               return null;
            } else {
               callData.gateway = gateway.getData().id;
               String dialCommand = null;
               String dialNextCommand = null;
               if (callData.destination != null && callData.destinationIDDCode != null) {
                  VoiceRouteData dRoute = this.routingTable.getDestinationRoute(callData, gatewayId);
                  VoiceRouteData dNextRoute = this.routingTable.getDestinationNextRoute(callData, dRoute.gatewayID, dRoute.providerID);
                  if (dNextRoute == null || dNextRoute.providerID == Integer.MIN_VALUE) {
                     log.warn("Exhausted all request call routes for destination failover. ID: " + callData.id + ". Source: " + callData.source + ". Destination: " + callData.destination + ".");
                     dNextRoute = new VoiceRouteData();
                     dNextRoute.gatewayID = Integer.MIN_VALUE;
                     dNextRoute.providerID = Integer.MIN_VALUE;
                     dNextRoute.dialCommand = "";
                  }

                  callData.destinationProvider = dRoute.providerID;
                  callData.destinationFirstProvider = dRoute.providerID;
                  callData.destinationNextProvider = dNextRoute.providerID;
                  callData.destinationDialCommand = dRoute.dialCommand;
                  callData.destinationFirstDialCommand = dRoute.dialCommand;
                  callData.destinationNextDialCommand = dNextRoute.dialCommand;
                  dialCommand = dRoute.dialCommand != null ? dRoute.dialCommand.replaceAll("%n", callData.destination) : null;
                  dialNextCommand = dNextRoute.dialCommand != null ? dNextRoute.dialCommand.replaceAll("%n", callData.destination) : null;
               }

               CallRequest callRequest = new CallRequest(callData, dialCommand, dialNextCommand, (long)callData.maxCallDuration * 1000L, callData.rate != null ? callData.rate : 0.0D, gateway.getData().timeoutWarning != null ? (long)gateway.getData().timeoutWarning : 0L, gateway.getData().timeoutWarningRepeat != null ? (long)gateway.getData().timeoutWarningRepeat : 0L, callData.id != null ? callData.id : 0);
               log.debug("Successfully filled in call request. ID: " + callData.id + ".");
               return callRequest;
            }
         }
      } catch (Exception var8) {
         log.warn("Fill in call request failed. ID: " + callData.id + ". Error: " + var8.getMessage() + ".");
         return null;
      }
   }

   public void run() {
      AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(VoiceEngine.class);

      while(true) {
         Voice voiceEJB;
         while(true) {
            try {
               Thread.sleep(1000L);
            } catch (Exception var5) {
            }

            try {
               voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
               break;
            } catch (Exception var6) {
               log.warn("Failed to create Voice EJB for charging calls - " + var6.getMessage() + ".");
            }
         }

         while(!this.callsCompleted.isEmpty()) {
            CallData callData = (CallData)this.callsCompleted.get(0);

            try {
               voiceEJB.chargeCall(callData, accountEntrySourceData);
               this.callsCompleted.remove(callData);
               this.usersInCall.remove(callData.username);
               this.callParams.remove(callData.id);
               log.info("Call completed. ID: " + callData.id + ". Source Duration: " + callData.sourceDuration + ". Destination Duration: " + callData.destinationDuration + ".");
            } catch (RemoteException var7) {
               log.warn("Failed to charge " + callData.username + " for call ID " + callData.id + " - " + RMIExceptionHelper.getRootMessage(var7) + ".");
               break;
            } catch (Exception var8) {
               log.warn("Failed to charge " + callData.username + " for call ID " + callData.id + " - " + var8.getMessage() + ".");
               break;
            }
         }
      }
   }

   private class CallParam {
      public int maxDuration;
      public int retries;
      public int retryAttempt;

      public CallParam(int maxDuration, int retries) {
         this.maxDuration = maxDuration;
         this.retries = retries;
      }
   }
}
