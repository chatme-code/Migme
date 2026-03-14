package com.projectgoth.fusion.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostUtils {
   public static String getHostname() {
      return (String)HostUtils.SingletonHolder.getHostnameLoader().getValue();
   }

   private static class SingletonHolder {
      private static final long CACHETIME_MS = 60000L;
      private static LazyLoader<String> HOSTNAME_LOADER = new LazyLoader<String>("HOSTNAME_LOADER", 60000L) {
         protected String fetchValue() throws Exception {
            try {
               return InetAddress.getLocalHost().getHostName().toUpperCase();
            } catch (UnknownHostException var2) {
               return "UNKNOWN";
            } catch (Throwable var3) {
               this.getLogger().error("Unable to resolve hostname", var3);
               return "_ERROR_";
            }
         }
      };

      public static LazyLoader<String> getHostnameLoader() {
         return HOSTNAME_LOADER;
      }
   }
}
