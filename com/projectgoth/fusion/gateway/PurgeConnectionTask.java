package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class PurgeConnectionTask extends TimerTask {
   private final Gateway gateway;
   private final GatewayContext gatewayContext;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Gateway.class));
   private Set<ConnectionI> connections = new HashSet();

   public PurgeConnectionTask(Gateway gateway, GatewayContext gatewayContext) {
      this.gateway = gateway;
      this.gatewayContext = gatewayContext;
   }

   public void add(ConnectionI connection) {
      synchronized(this.connections) {
         this.connections.add(connection);
      }
   }

   public void remove(ConnectionI connection) {
      synchronized(this.connections) {
         this.connections.remove(connection);
      }
   }

   public void monitor(ConnectionI connection) {
      synchronized(this.connections) {
         this.connections.add(connection);
      }
   }

   private void purge(ConnectionI connection) {
      boolean var13 = false;

      label87: {
         try {
            var13 = true;
            connection.onSessionTerminated();
            connection.disconnect();
            var13 = false;
            break label87;
         } catch (Exception var17) {
            log.warn(var17.getClass().getName() + " occured when purging connection " + connection.getSessionID() + " [" + connection.getUsername() + "] - ", var17);
            var13 = false;
         } finally {
            if (var13) {
               synchronized(this.connections) {
                  this.connections.remove(connection);
               }
            }
         }

         synchronized(this.connections) {
            this.connections.remove(connection);
            return;
         }
      }

      synchronized(this.connections) {
         this.connections.remove(connection);
      }

   }

   private Set<ConnectionI> getIdleConnectionsToPurge(long cutoffTime) {
      Set<ConnectionI> connectionsToPurge = new HashSet();
      synchronized(this.connections) {
         Iterator i$ = this.connections.iterator();

         while(i$.hasNext()) {
            ConnectionI connection = (ConnectionI)i$.next();
            if (connection.getLastAccessed() < cutoffTime) {
               connectionsToPurge.add(connection);
            }
         }

         return connectionsToPurge;
      }
   }

   public void run() {
      log.debug("Purging idle " + this.gateway.getServerType() + " connections ...");
      if (this.gateway.getTimeoutInterval() > 0L) {
         long cutoffTime = System.currentTimeMillis() - this.gateway.getTimeoutInterval();
         Set<ConnectionI> connectionsToPurge = this.getIdleConnectionsToPurge(cutoffTime);
         Iterator i$ = connectionsToPurge.iterator();

         while(i$.hasNext()) {
            ConnectionI connection = (ConnectionI)i$.next();
            this.purge(connection);
         }

         if (connectionsToPurge.size() > 0 && log.isDebugEnabled()) {
            log.debug("Purged [" + connectionsToPurge.size() + "] connections");
         }
      }

      try {
         this.gatewayContext.getIcePrxFinder().getRegistry(false).ice_ping();
      } catch (Exception var7) {
         Exception e = var7;

         try {
            log.warn("registry proxy not responding to ping", e);
            this.gatewayContext.getIcePrxFinder().getRegistry(true);
         } catch (Exception var6) {
            log.error("unable to relocate registry proxy", var6);
         }
      }

      if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
         this.gateway.logDebugPurgingCompleted();
      } else {
         log.debug("Purging task completed. " + this.gateway.connectionCount.get() + " " + this.gateway.getServerType() + " connection(s) left on the adaptor. " + this.gateway.getSelector().keys().size() + " key(s) left on selector");
      }

   }
}
