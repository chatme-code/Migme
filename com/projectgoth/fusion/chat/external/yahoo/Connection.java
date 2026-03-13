package com.projectgoth.fusion.chat.external.yahoo;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Connection implements Runnable {
   private static final int MIN_BUFFER_SIZE = 2048;
   private static final int MAX_BUFFER_SIZE = 65536;
   private static final int BAD_IP_ADDRESS_PERIOD = 21600000;
   protected int connectionTimeout;
   protected int maxConcurrentConnections;
   private static SecureRandom secureRandom = new SecureRandom();
   private static List<String> goodIPAddresses = new LinkedList();
   private static Map<String, Long> badIPAddresses = new HashMap();
   private static SelectorThread selector;
   private static Object selectorLock = new Object();
   private SocketChannel channel;
   private ByteBuffer readBuffer = ByteBuffer.allocate(2048);

   public SocketChannel getSocketChannel() {
      return this.channel;
   }

   protected synchronized void connect(String server, int port) throws YahooException {
      try {
         synchronized(selectorLock) {
            if (selector == null || !selector.isAlive()) {
               selector = new SelectorThread();
               selector.start();
            }
         }

         if (this.channel != null) {
            this.disconnect("Reconnecting");
         }

         if (selector.getNoOfKeys() > this.maxConcurrentConnections) {
            throw new Exception("Exceeded maximum connection limit");
         } else {
            server = this.getGoodIPAddress(server, port);
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(new InetSocketAddress(server, port));
            int hundrethsWaited = 0;

            while(!this.channel.finishConnect()) {
               try {
                  Thread.sleep(100L);
               } catch (Exception var5) {
               }

               ++hundrethsWaited;
               if (hundrethsWaited >= this.connectionTimeout * 10) {
                  throw new Exception("Forced timeout");
               }
            }

            this.updateGoodIPAddresses(server);
            selector.registerConnection(this);
         }
      } catch (Exception var7) {
         System.out.println("Failed to connect to " + server + ":" + port + " - " + var7.getMessage());
         this.updateBadIPAddresses(server);
         this.disconnect(var7.getMessage());
         throw new YahooException(var7.getMessage());
      }
   }

   protected synchronized void disconnect(String reason) {
      if (this.channel != null) {
         try {
            selector.deregisterConnection(this);
         } catch (Exception var18) {
            var18.printStackTrace();
         }

         try {
            Thread.sleep(100L);
         } catch (Exception var17) {
         }

         try {
            this.channel.socket().shutdownOutput();
         } catch (Exception var16) {
         }

         try {
            this.channel.socket().shutdownInput();
         } catch (Exception var15) {
         }

         try {
            this.channel.socket().close();
         } catch (Exception var14) {
            var14.printStackTrace();
         }

         try {
            this.channel.close();
         } catch (Exception var12) {
            var12.printStackTrace();
         } finally {
            this.channel = null;
         }

         selector.wakeup();
         this.readBuffer.clear();
         this.onDisconnect(reason);
      }

   }

   private String getGoodIPAddress(String server, int port) {
      String ipAddress = (new InetSocketAddress(server, port)).getAddress().getHostAddress();
      synchronized(goodIPAddresses) {
         Long badServerExpiryTime = (Long)badIPAddresses.get(ipAddress);
         if (badServerExpiryTime != null && badServerExpiryTime > System.currentTimeMillis() && goodIPAddresses.size() > 0) {
            ipAddress = (String)goodIPAddresses.get(secureRandom.nextInt(goodIPAddresses.size()));
         }

         return ipAddress;
      }
   }

   private void updateGoodIPAddresses(String ipAddress) {
      synchronized(goodIPAddresses) {
         if (!goodIPAddresses.contains(ipAddress)) {
            goodIPAddresses.add(ipAddress);
         }

         badIPAddresses.remove(ipAddress);
      }
   }

   private void updateBadIPAddresses(String ipAddress) {
      synchronized(goodIPAddresses) {
         goodIPAddresses.remove(ipAddress);
         badIPAddresses.put(ipAddress, System.currentTimeMillis() + 21600000L);
      }
   }

   public boolean isConnected() {
      return this.channel != null && this.channel.isConnected();
   }

   public void sendAsyncPacket(YMSGPacket packet) {
      try {
         synchronized(this.channel) {
            ByteBuffer buffer = ByteBuffer.wrap(packet.toByteArray());

            while(buffer.hasRemaining()) {
               this.channel.write(buffer);
            }
         }
      } catch (Exception var6) {
         this.disconnect(var6.getMessage());
      }

   }

   public void run() {
      this.onChannelReadable();
   }

   public synchronized void onChannelReadable() {
      try {
         if (this.channel != null) {
            int bytesRead = this.channel.read(this.readBuffer);
            if (bytesRead == -1) {
               this.disconnect("Connection closed by Yahoo host - make sure you are logged in only once");
            } else if (bytesRead == 0) {
               this.disconnect("Connection to Yahoo host is lost - make sure you are logged in only once");
            } else if (bytesRead > 0) {
               while(true) {
                  ByteBuffer buffer = (ByteBuffer)this.readBuffer.duplicate().flip();
                  YMSGPacket packet = new YMSGPacket(buffer);
                  this.readBuffer = buffer.compact();
                  this.onIncomingPacket(packet);
               }
            }
         }
      } catch (BufferUnderflowException var4) {
         this.readBuffer = ByteBufferHelper.adjustSize(this.readBuffer, 2048, 65536, 2.0D);
         selector.registerConnection(this);
      } catch (IOException var5) {
         this.disconnect("Failed to read Yahoo packet - " + var5.getMessage());
      } catch (NotYetConnectedException var6) {
         selector.registerConnection(this);
      } catch (Exception var7) {
         this.disconnect("Connection to Yahoo host is lost - " + var7.getMessage());
      }

   }

   protected abstract void onIncomingPacket(YMSGPacket var1);

   protected abstract void onDisconnect(String var1);
}
