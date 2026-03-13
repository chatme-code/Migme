package com.projectgoth.fusion.chat.external.yahoo;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThread extends Thread {
   private static final int SELECT_TIMEOUT = 1000;
   private static final int PING_INTERVAL = 900000;
   private static final int THREAD_POOL_SIZE = 20;
   private Selector selector = Selector.open();
   private List<Connection> pendingConnections = new ArrayList();
   private ExecutorService pool = Executors.newFixedThreadPool(20);
   private long nextPing = System.currentTimeMillis() + 900000L;
   private AtomicInteger noOfKeys = new AtomicInteger();

   public SelectorThread() throws IOException {
   }

   public int getNoOfKeys() {
      return this.noOfKeys.get();
   }

   public void wakeup() {
      this.selector.wakeup();
   }

   public void registerConnection(Connection connection) {
      SocketChannel channel = connection.getSocketChannel();
      if (channel != null) {
         SelectionKey key = channel.keyFor(this.selector);
         if (key == null) {
            synchronized(this.pendingConnections) {
               if (!this.pendingConnections.contains(connection)) {
                  this.pendingConnections.add(connection);
                  this.selector.wakeup();
               }
            }
         } else {
            key.interestOps(1);
            this.selector.wakeup();
         }
      }

   }

   public void deregisterConnection(Connection connection) {
      SocketChannel channel = connection.getSocketChannel();
      if (channel != null) {
         SelectionKey key = channel.keyFor(this.selector);
         if (key != null) {
            key.cancel();
            this.selector.wakeup();
         }
      }

   }

   private void registerPendingConnections() {
      synchronized(this.pendingConnections) {
         Iterator i$ = this.pendingConnections.iterator();

         while(i$.hasNext()) {
            Connection connection = (Connection)i$.next();

            try {
               connection.getSocketChannel().register(this.selector, 1, connection);
            } catch (ClosedChannelException var6) {
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }

         this.pendingConnections.clear();
      }
   }

   private void processSelections(Set<SelectionKey> selectedKeys) {
      Iterator i$ = selectedKeys.iterator();

      while(i$.hasNext()) {
         SelectionKey key = (SelectionKey)i$.next();

         try {
            Connection conn = (Connection)key.attachment();
            if (conn != null) {
               if (key.isValid()) {
                  key.interestOps(0);
               }

               this.pool.execute(conn);
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      selectedKeys.clear();
   }

   private void keepConnectionsAlive() {
      if (System.currentTimeMillis() > this.nextPing) {
         this.nextPing += 900000L;
         List<YahooConnection> connections = new ArrayList();
         Iterator i$ = this.selector.keys().iterator();

         while(i$.hasNext()) {
            SelectionKey key = (SelectionKey)i$.next();
            if (key.isValid()) {
               YahooConnection conn = (YahooConnection)key.attachment();
               if (conn != null) {
                  connections.add(conn);
               }
            }
         }

         this.pool.execute(new SelectorThread.KeepAliveTask(connections));
      }

   }

   public void run() {
      while(true) {
         try {
            if (this.selector.select(1000L) > 0) {
               this.processSelections(this.selector.selectedKeys());
            }

            this.noOfKeys.set(this.selector.keys().size());
            this.keepConnectionsAlive();
            this.registerPendingConnections();
            Thread.sleep(10L);
         } catch (Exception var4) {
            var4.printStackTrace();

            try {
               this.selector.close();
            } catch (Exception var3) {
            }

            return;
         }
      }
   }

   private class KeepAliveTask implements Runnable {
      private List<YahooConnection> connections;

      public KeepAliveTask(List<YahooConnection> connections) {
         this.connections = connections;
      }

      public void run() {
         Iterator i$ = this.connections.iterator();

         while(i$.hasNext()) {
            YahooConnection conn = (YahooConnection)i$.next();
            conn.ping();
         }

      }
   }
}
