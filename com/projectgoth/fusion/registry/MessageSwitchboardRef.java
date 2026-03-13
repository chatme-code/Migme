package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.MessageSwitchboardAdminPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import org.apache.log4j.Logger;

public class MessageSwitchboardRef {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageSwitchboardRef.class));
   private String hostName;
   private boolean online;
   private MessageSwitchboardPrx msbProxy;
   private MessageSwitchboardAdminPrx adminProxy;

   public MessageSwitchboardRef(String hostName, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy) {
      this.hostName = hostName;
      this.online = true;
      this.msbProxy = msbProxy;
      this.adminProxy = adminProxy;
   }

   public String getHostName() {
      return this.hostName;
   }

   public MessageSwitchboardPrx getProxy() {
      return this.msbProxy;
   }

   public MessageSwitchboardAdminPrx getAdminProxy() {
      return this.adminProxy;
   }

   public synchronized boolean isOnline() {
      return this.online;
   }

   public synchronized void setOnline(boolean online) {
      this.online = online;
   }
}
