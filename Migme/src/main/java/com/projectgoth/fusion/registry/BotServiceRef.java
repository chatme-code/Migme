package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.slice.BotServiceAdminPrx;
import com.projectgoth.fusion.slice.BotServicePrx;

public class BotServiceRef {
   private String hostName;
   private int load;
   private boolean online;
   private BotServicePrx serviceProxy;
   private BotServiceAdminPrx adminProxy;

   public BotServiceRef(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy) {
      this.hostName = hostName;
      this.load = load;
      this.online = true;
      this.serviceProxy = serviceProxy;
      this.adminProxy = adminProxy;
   }

   public String getHostName() {
      return this.hostName;
   }

   public int getLoad() {
      return this.load;
   }

   public void setLoad(int load) {
      this.load = load;
   }

   public boolean isOnline() {
      return this.online;
   }

   public void setOnline(boolean online) {
      this.online = online;
   }

   public BotServicePrx getServiceProxy() {
      return this.serviceProxy;
   }

   public BotServiceAdminPrx getAdminProxy() {
      return this.adminProxy;
   }
}
