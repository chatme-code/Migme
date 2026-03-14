package com.projectgoth.fusion.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public enum PortRegistry {
   REGISTRY(10000),
   REGISTRY_ADMIN(12000),
   GATEWAY_HTTP(9999),
   GATEWAY_HTTP_ADMIN(9998),
   GATEWAY_TCP(19999),
   GATEWAY_TCP_ADMIN(19998),
   GATEWAY_TCP9119(29999),
   GATEWAY_TCP9119_ADMIN(29998),
   OBJECT_CACHE(11000),
   OBJECT_CACHE_ADMIN(9000),
   EMAIL_ALERT(11345),
   EMAIL_ALERT_ADMIN(9345),
   IMAGE_SERVER_ADMIN(39998),
   MESSAGE_LOGGER(18888),
   MESSAGE_LOGGER_ADMIN(18889),
   SESSION_CACHE(11023),
   SESSION_CACHE_ADMIN(9023),
   SMS_ENGINE(9996),
   VOICE_ENGINE(15000),
   EVENT_SYSTEM(21500),
   EVENT_SYSTEM_ADMIN(21550),
   EVENT_STORE(21000),
   EVENT_STORE_ADMIN(21050),
   BLUE_LABEL_SERVICE(22000),
   BLUE_LABEL_SERVICE_ADMIN(22050),
   USER_NOTIFICATION_SERVICE(22500),
   USER_NOTIFICATION_SERVICE_ADMIN(22550),
   JOB_SCHEDULING_SERVICE(23000),
   JOB_SCHEDULING_SERVICE_ADMIN(23050),
   AUTHENTICATION_SERVICE(23500),
   AUTHENTICATION_SERVICE_ADMIN(23550),
   REPUTATION_SERVICE(24000),
   REPUTATION_SERVICE_ADMIN(24050);

   private final int port;

   private PortRegistry(int port) {
      this.port = port;
   }

   public int getPort() {
      return this.port;
   }

   public static String getHostname() {
      String hostName;
      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var2) {
         hostName = "UNKNOWN";
      }

      return hostName;
   }
}
