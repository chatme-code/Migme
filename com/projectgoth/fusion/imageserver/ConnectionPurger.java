package com.projectgoth.fusion.imageserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class ConnectionPurger extends TimerTask {
   private Logger logger;
   private Set<Connection> connectionQueue = new HashSet();
   private int timeoutInterval;

   public ConnectionPurger(int timeoutInterval, Logger logger) {
      this.timeoutInterval = timeoutInterval;
      this.logger = logger;
   }

   public void add(Connection connection) {
      synchronized(this.connectionQueue) {
         this.connectionQueue.add(connection);
      }
   }

   public void remove(Connection connection) {
      synchronized(this.connectionQueue) {
         this.connectionQueue.remove(connection);
      }
   }

   public void monitor(Connection connection) {
      synchronized(this.connectionQueue) {
         this.connectionQueue.add(connection);
      }
   }

   private void purge(Connection connection) {
      try {
         connection.disconnect();
         synchronized(this.connectionQueue) {
            this.connectionQueue.remove(connection);
         }
      } catch (Exception var5) {
         this.logger.warn(var5.getClass().getName() + " occured when purging connection - " + var5.getMessage());
      }

   }

   public void run() {
      this.logger.debug("Purging idle connections ...");
      long cutoffTime;
      if (this.timeoutInterval > 0) {
         Set<Connection> connectionsToPurge = new HashSet();
         cutoffTime = System.currentTimeMillis() - (long)this.timeoutInterval;
         synchronized(this.connectionQueue) {
            Iterator i$ = this.connectionQueue.iterator();

            while(true) {
               if (!i$.hasNext()) {
                  break;
               }

               Connection connection = (Connection)i$.next();
               if (connection.getLastAccessed() < cutoffTime) {
                  connectionsToPurge.add(connection);
               }
            }
         }

         Iterator i$ = connectionsToPurge.iterator();

         while(i$.hasNext()) {
            Connection connection = (Connection)i$.next();
            this.purge(connection);
         }
      }

      Runtime runtime = Runtime.getRuntime();
      cutoffTime = runtime.totalMemory() / 1024L;
      long memoryUsed = cutoffTime - runtime.freeMemory() / 1024L;
      this.logger.debug("Purging task completed. " + this.connectionQueue.size() + " connection(s) left on server");
      this.logger.debug("JVM Memory: Total Allocated: " + cutoffTime + "KB. Used: " + memoryUsed + "KB\n");
   }
}
