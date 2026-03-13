package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.SMSGatewayData;
import com.projectgoth.fusion.data.SMSRouteData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class RoutingTable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RoutingTable.class));
   private static Map<Integer, SMSGateway> gateways = null;
   private static Map<String, List<SMSRouteData>> routes = null;
   private static Map<Integer, Set<String>> areaCodes = null;

   private static String getAreaCode(Integer iddCode, String phoneNumber) {
      String areaCode = "";
      Set<String> codes = (Set)areaCodes.get(iddCode);
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

   private static String constructKey(SMSRouteData.TypeEnum type, int iddCode, String areaCode) {
      return type.toString() + ";" + Integer.toString(iddCode) + ";" + areaCode;
   }

   public static synchronized void load() throws CreateException, RemoteException {
      Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
      List<SMSGatewayData> gatewayList = messageEJB.getSMSGateways();
      if (gatewayList != null && gatewayList.size() != 0) {
         gateways = new ConcurrentHashMap();
         routes = new ConcurrentHashMap();
         areaCodes = new ConcurrentHashMap();
         Iterator i$ = gatewayList.iterator();

         while(true) {
            Iterator i$;
            SMSRouteData routeData;
            while(i$.hasNext()) {
               SMSGatewayData gatewayData = (SMSGatewayData)i$.next();
               if (gatewayData.type == SMSGatewayData.TypeEnum.HTTP) {
                  gateways.put(gatewayData.id, new HTTPGateway(gatewayData));
               } else {
                  if (gatewayData.type != SMSGatewayData.TypeEnum.SMPP_TRANSMITTER && gatewayData.type != SMSGatewayData.TypeEnum.SMPP_TRANSCEIVER) {
                     log.warn("Gateway " + gatewayData.id + " is using unsupported type " + gatewayData.type);
                     continue;
                  }

                  gateways.put(gatewayData.id, new SMPPGateway(gatewayData));
               }

               i$ = gatewayData.smsRoutes.iterator();

               while(i$.hasNext()) {
                  routeData = (SMSRouteData)i$.next();
                  String key = constructKey(routeData.type, routeData.iddCode, routeData.areaCode);
                  List<SMSRouteData> route = (List)routes.get(key);
                  if (route == null) {
                     List<SMSRouteData> route = new LinkedList();
                     route.add(routeData);
                     routes.put(key, route);
                  } else {
                     route.add(routeData);
                  }

                  if (routeData.areaCode.length() > 0) {
                     Set<String> codes = (Set)areaCodes.get(routeData.iddCode);
                     if (codes == null) {
                        codes = new HashSet();
                        areaCodes.put(routeData.iddCode, codes);
                     }

                     ((Set)codes).add(routeData.areaCode);
                  }
               }
            }

            i$ = gateways.values().iterator();

            while(i$.hasNext()) {
               SMSGateway gateway = (SMSGateway)i$.next();
               log.debug(gateway);
            }

            i$ = routes.values().iterator();

            while(i$.hasNext()) {
               List<SMSRouteData> routeQueue = (List)i$.next();
               i$ = routeQueue.iterator();

               while(i$.hasNext()) {
                  routeData = (SMSRouteData)i$.next();
                  log.debug(routeData);
               }
            }

            return;
         }
      } else {
         log.warn("No SMS routing table in database");
      }
   }

   public static List<SMSRouteData> getRoutes(SMSRouteData.TypeEnum type, int iddCode, String phoneNumber) {
      if (routes == null) {
         return Collections.EMPTY_LIST;
      } else {
         List<SMSRouteData> list = (List)routes.get(constructKey(type, iddCode, getAreaCode(iddCode, phoneNumber)));
         if (list == null) {
            list = (List)routes.get(constructKey(type, iddCode, ""));
            if (list == null) {
               return Collections.EMPTY_LIST;
            }
         }

         List<SMSRouteData> list = new LinkedList(list);
         Collections.shuffle(list);
         Collections.sort(list);
         return list;
      }
   }

   public static SMSGateway getGateway(int id) {
      return gateways == null ? null : (SMSGateway)gateways.get(id);
   }
}
