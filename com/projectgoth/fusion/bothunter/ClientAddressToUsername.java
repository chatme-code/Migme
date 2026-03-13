package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.HashMap;
import jpcap.packet.TCPPacket;
import org.apache.log4j.Logger;

public class ClientAddressToUsername extends HashMap<String, String> {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MonitorThread.class));
   private static final long serialVersionUID = 1L;
   private static final String COLON = ":";
   private static ClientAddressToUsername instance;
   private boolean debugEnabled;

   private ClientAddressToUsername() {
      this.debugEnabled = log.isDebugEnabled();
   }

   public static synchronized ClientAddressToUsername getInstance() {
      if (instance == null) {
         instance = new ClientAddressToUsername();
      }

      return instance;
   }

   public void putUsername(TCPPacket loginPacket, String username) {
      String key = loginPacket.src_ip.toString() + ":" + loginPacket.src_port;
      if (this.debugEnabled) {
         log.debug("Caching username=" + username + " against key=" + key);
      }

      this.put(key, username);
   }

   public String getUsername(TCPPacket packet) {
      String key = packet.src_ip.toString() + ":" + packet.src_port;
      if (this.debugEnabled) {
         log.debug("Retrieving username for key=" + key);
      }

      return (String)this.get(key);
   }

   public String getUsername(ClientAddress ca) {
      String key = ca.getClientIP() + ":" + ca.getClientPort();
      if (this.debugEnabled) {
         log.debug("Retrieving username for key=" + key);
      }

      return (String)this.get(key);
   }
}
