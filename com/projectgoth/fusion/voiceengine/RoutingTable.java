package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.VoiceRouteData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class RoutingTable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RoutingTable.class));
   private Map<Integer, List<VoiceRouteData>> countryRoutes = new ConcurrentHashMap();
   private Map<Integer, Set<String>> areaCodes = new ConcurrentHashMap();

   private String getAreaCode(Integer iddCode, String phoneNumber) {
      String areaCode = "";
      Set<String> codes = (Set)this.areaCodes.get(iddCode);
      if (codes != null) {
         phoneNumber = phoneNumber.substring(iddCode.toString().length());
         Iterator i$ = codes.iterator();

         while(i$.hasNext()) {
            String code = (String)i$.next();
            if (phoneNumber.startsWith(code) && code.length() > areaCode.length()) {
               areaCode = code;
            }
         }
      }

      return areaCode;
   }

   private List<VoiceRouteData> getRoutes(Integer iddCode, String phoneNumber) throws Exception {
      String areaCode = this.getAreaCode(iddCode, phoneNumber);
      List<VoiceRouteData> result = new ArrayList();
      List<VoiceRouteData> routes = (List)this.countryRoutes.get(iddCode);
      if (routes != null) {
         Iterator i$ = routes.iterator();

         while(i$.hasNext()) {
            VoiceRouteData route = (VoiceRouteData)i$.next();
            if (areaCode.equals(route.areaCode)) {
               result.add(route);
            }
         }
      }

      if (result.size() == 0) {
         if (areaCode == null) {
            throw new Exception("No route(s) available for IDD code " + iddCode);
         } else {
            throw new Exception("No route(s) available for IDD code " + iddCode + ", area code " + areaCode);
         }
      } else {
         return result;
      }
   }

   public void add(VoiceRouteData route) {
      Object routes;
      if (route.areaCode.length() > 0) {
         routes = (Set)this.areaCodes.get(route.iddCode);
         if (routes == null) {
            routes = new HashSet();
            this.areaCodes.put(route.iddCode, routes);
         }

         ((Set)routes).add(route.areaCode);
      }

      routes = (List)this.countryRoutes.get(route.iddCode);
      if (routes == null) {
         routes = new ArrayList();
         this.countryRoutes.put(route.iddCode, routes);
      }

      ((List)routes).add(route);
   }

   public void add(Collection<VoiceRouteData> routes) {
      if (routes != null) {
         Iterator i$ = routes.iterator();

         while(i$.hasNext()) {
            VoiceRouteData route = (VoiceRouteData)i$.next();
            this.add(route);
         }
      }

   }

   public List<VoiceRouteData> getSourceRoutes(CallData callData) throws Exception {
      if (callData.sourceType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
         if (callData.sourceIDDCode == null) {
            throw new Exception("Source IDD Code must be specified");
         } else if (callData.source == null) {
            throw new Exception("Source must be specified");
         } else {
            List<VoiceRouteData> routes = this.getRoutes(callData.sourceIDDCode, callData.source);
            List<VoiceRouteData> routes = new ArrayList(routes);
            Collections.shuffle(routes);
            Collections.sort(routes);
            if (routes != null && routes.size() > 0) {
               return routes;
            } else {
               Exception e = new Exception("No source route(s) available to source " + callData.source);
               log.error(e.getMessage());
               throw e;
            }
         }
      } else if (callData.sourceType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
         VoiceRouteData route = new VoiceRouteData();
         route.gatewayID = callData.gateway;
         route.dialCommand = callData.sourceProtocol + "/%n";
         route.priority = 1;
         List<VoiceRouteData> routes = new ArrayList();
         routes.add(route);
         return routes;
      } else {
         throw new Exception("Unsupported source type " + callData.sourceType);
      }
   }

   public VoiceRouteData getSourceNextRoute(CallData callData, int gatewayId, int lastProviderId) throws Exception {
      if (callData.sourceType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
         if (callData.sourceIDDCode == null) {
            throw new Exception("Source IDD Code must be specified");
         } else if (callData.source == null) {
            throw new Exception("Source must be specified");
         } else {
            List<VoiceRouteData> routes = this.getRoutes(callData.sourceIDDCode, callData.source);
            List<VoiceRouteData> routes = new ArrayList(routes);
            Collections.shuffle(routes);
            Collections.sort(routes);
            boolean found = false;
            Iterator i$ = routes.iterator();

            while(i$.hasNext()) {
               VoiceRouteData route = (VoiceRouteData)i$.next();
               if (route.gatewayID == gatewayId) {
                  if (route.providerID == lastProviderId) {
                     found = true;
                  } else if (found) {
                     return route;
                  }
               }
            }

            if (found) {
               return null;
            } else {
               Exception e = new Exception("No more source route(s) available on voice gateway " + gatewayId + " to source " + callData.source);
               log.error(e.getMessage());
               throw e;
            }
         }
      } else if (callData.sourceType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
         return null;
      } else {
         throw new Exception("Unsupported source type " + callData.sourceType);
      }
   }

   public VoiceRouteData getDestinationRoute(CallData callData, int gatewayId) throws Exception {
      if (callData.destinationType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
         if (callData.destinationIDDCode == null) {
            throw new Exception("Destination IDD Code must be specified");
         } else if (callData.destination == null) {
            throw new Exception("Destination must be specified");
         } else {
            List<VoiceRouteData> routes = this.getRoutes(callData.destinationIDDCode, callData.destination);
            List<VoiceRouteData> routes = new ArrayList(routes);
            Collections.shuffle(routes);
            Collections.sort(routes);
            Iterator i$ = routes.iterator();

            VoiceRouteData route;
            do {
               if (!i$.hasNext()) {
                  Exception e = new Exception("No destination route(s) available on voice gateway " + gatewayId + " to destination " + callData.destination);
                  log.error(e.getMessage());
                  throw e;
               }

               route = (VoiceRouteData)i$.next();
            } while(route.gatewayID != gatewayId);

            return route;
         }
      } else if (callData.destinationType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
         VoiceRouteData route = new VoiceRouteData();
         route.gatewayID = callData.gateway;
         route.dialCommand = callData.destinationProtocol + "/%n";
         route.priority = 1;
         return route;
      } else {
         throw new Exception("Unsupported destination type " + callData.destinationType);
      }
   }

   public VoiceRouteData getDestinationNextRoute(CallData callData, int gatewayId, int lastProviderId) throws Exception {
      if (callData.destinationType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
         if (callData.destinationIDDCode == null) {
            throw new Exception("Destination IDD Code must be specified");
         } else if (callData.destination == null) {
            throw new Exception("Destination must be specified");
         } else {
            List<VoiceRouteData> routes = this.getRoutes(callData.destinationIDDCode, callData.destination);
            List<VoiceRouteData> routes = new ArrayList(routes);
            Collections.shuffle(routes);
            Collections.sort(routes);
            boolean found = false;
            Iterator i$ = routes.iterator();

            while(i$.hasNext()) {
               VoiceRouteData route = (VoiceRouteData)i$.next();
               if (route.gatewayID == gatewayId) {
                  if (route.providerID == lastProviderId) {
                     found = true;
                  } else if (found) {
                     return route;
                  }
               }
            }

            if (found) {
               return null;
            } else {
               Exception e = new Exception("No more destination route(s) available on voice gateway " + gatewayId + " to destination " + callData.destination);
               log.error(e.getMessage());
               throw e;
            }
         }
      } else if (callData.destinationType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
         return null;
      } else {
         throw new Exception("Unsupported destination type " + callData.destinationType);
      }
   }

   public void print() {
      int count = 0;
      Iterator i$ = this.countryRoutes.values().iterator();

      while(i$.hasNext()) {
         List<VoiceRouteData> l = (List)i$.next();

         for(Iterator i$ = l.iterator(); i$.hasNext(); ++count) {
            VoiceRouteData r = (VoiceRouteData)i$.next();
            if (r.areaCode.length() == 0) {
               log.debug("IDD = " + r.iddCode + ", gateway = " + r.gatewayID + ", provider = " + r.providerID + ", priority = " + r.priority);
            } else {
               log.debug("IDD = " + r.iddCode + ", area code = " + r.areaCode + ", gateway = " + r.gatewayID + ", provider = " + r.providerID + ", priority = " + r.priority);
            }
         }
      }

      log.debug(count + " routes");
   }
}
