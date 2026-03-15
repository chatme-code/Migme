package com.projectgoth.fusion.chat.external.aim;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

public abstract class Connection implements Runnable {
   protected static final String DEFAULT_CHARSET = "UTF-8";
   protected static final int COMMAND_TIMEOUT = 5000;
   protected static final int BUFFER_SIZE = 4096;
   private static SelectorThread selector;
   private static Object selectorLock = new Object();
   private SocketChannel channel;
   private ByteBuffer readBuffer = ByteBuffer.allocate(4096);
   private short nextSequence;
   protected int connectionTimeout;

   public SocketChannel getSocketChannel() {
      return this.channel;
   }

   protected synchronized void connect(String server, int port) throws AIMException {
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

         selector.registerConnection(this);
      } catch (Exception var7) {
         System.out.println("Failed to connect to " + server + ":" + port + " - " + var7.getMessage());
         this.disconnect(var7.getMessage());
         throw new AIMException(var7.getMessage());
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

   public boolean isConnected() {
      return this.channel != null && this.channel.isConnected();
   }

   public void sendAsyncPacket(FLAP packet) {
      try {
         synchronized(this.channel) {
            short var10003 = this.nextSequence;
            this.nextSequence = (short)(var10003 + 1);
            packet.setSequence(var10003);
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
               this.disconnect("Connection closed by remote host");
            } else if (bytesRead == 0) {
               this.disconnect("Connection to AIM server is lost - 0 byte read");
            } else if (bytesRead > 0) {
               while(true) {
                  ByteBuffer buffer = (ByteBuffer)this.readBuffer.duplicate().flip();
                  FLAP packet = new FLAP(buffer);
                  this.readBuffer = buffer.compact();
                  this.onIncomingPacket(packet);
               }
            }
         }
      } catch (BufferUnderflowException var4) {
         selector.registerConnection(this);
      } catch (IOException var5) {
         this.disconnect("Failed to read AIM packet - " + var5.getMessage());
      } catch (NotYetConnectedException var6) {
         selector.registerConnection(this);
      } catch (Exception var7) {
         this.disconnect("Connection to AIM server is lost - " + var7.getMessage());
      }

   }

   protected abstract void onIncomingPacket(FLAP var1);

   protected abstract void onDisconnect(String var1);
}
