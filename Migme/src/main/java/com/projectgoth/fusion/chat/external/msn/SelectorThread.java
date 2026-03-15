package com.projectgoth.fusion.chat.external.msn;

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

public class SelectorThread extends Thread {
   private static final int THREAD_POOL_SIZE = 20;
   private Selector selector = Selector.open();
   private List<Connection> pendingConnections = new ArrayList();
   private ExecutorService pool = Executors.newFixedThreadPool(20);

   public SelectorThread() throws IOException {
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
         } else if (key.isValid()) {
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
               SocketChannel channel = connection.getSocketChannel();
               if (channel != null) {
                  channel.register(this.selector, 1, connection);
               }
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

   public void run() {
      while(true) {
         try {
            if (this.selector.select() > 0) {
               this.processSelections(this.selector.selectedKeys());
            }

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
}
