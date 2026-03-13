package com.projectgoth.fusion.chat.external.msn;

import com.projectgoth.fusion.common.ByteBufferHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public abstract class Connection implements Runnable {
   protected static final String DEFAULT_CHARSET = "UTF-8";
   protected static final int COMMAND_TIMEOUT = 20000;
   protected static final int MIN_BUFFER_SIZE = 1024;
   protected static final int MAX_BUFFER_SIZE = 10240;
   private static Logger log = Logger.getLogger(Connection.class);
   private static SelectorThread selector;
   private static Object selectorLock = new Object();
   private SocketChannel channel;
   private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
   private int transactionId;
   private Map<Integer, Command> commandsSent = new ConcurrentHashMap();
   private Object connectionLock = new Object();
   protected int connectionTimeout;

   public static Logger getLogger() {
      return log;
   }

   public SocketChannel getSocketChannel() {
      return this.channel;
   }

   protected void connect(String server, int port) throws MSNException {
      synchronized(this.connectionLock) {
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
               } catch (Exception var7) {
               }

               ++hundrethsWaited;
               if (hundrethsWaited >= this.connectionTimeout * 10) {
                  throw new Exception("Forced timeout");
               }
            }

            selector.registerConnection(this);
            this.transactionId = 0;
            this.commandsSent.clear();
            this.readBuffer.clear();
         } catch (Exception var9) {
            log.warn("Failed to connect to " + server + ":" + port + " - " + var9.getMessage());
            this.disconnect(var9.getMessage());
            throw new MSNException(var9.getMessage());
         }

      }
   }

   protected void disconnect(String reason) {
      synchronized(this.connectionLock) {
         if (this.channel != null) {
            try {
               selector.deregisterConnection(this);
            } catch (Exception var20) {
               var20.printStackTrace();
            }

            try {
               Thread.sleep(100L);
            } catch (Exception var19) {
            }

            try {
               this.channel.socket().shutdownOutput();
            } catch (Exception var18) {
            }

            try {
               this.channel.socket().shutdownInput();
            } catch (Exception var17) {
            }

            try {
               this.channel.socket().close();
            } catch (Exception var16) {
               var16.printStackTrace();
            }

            try {
               this.channel.close();
            } catch (Exception var14) {
               var14.printStackTrace();
            } finally {
               this.channel = null;
            }

            selector.wakeup();
            this.onDisconnect(reason);
         }

      }
   }

   public boolean isConnected() {
      return this.channel != null && this.channel.isConnected();
   }

   protected Command sendCommand(Command command) throws MSNException {
      if (!this.isConnected()) {
         throw new MSNException("Not connected to MSN server");
      } else {
         synchronized(command) {
            this.sendAsyncCommand(command);
            if (this.isConnected()) {
               try {
                  command.wait(20000L);
               } catch (Exception var5) {
               }
            }

            Command reply = command.getReply();
            if (reply == null) {
               if (this.isConnected()) {
                  throw new MSNException("Time out while waiting for reply from MSN server");
               } else {
                  throw new MSNException("Connection to MSN server is lost");
               }
            } else if (reply.getType() == Command.Type.ERROR) {
               throw new MSNException(reply.getErrorCode());
            } else {
               return reply;
            }
         }
      }
   }

   protected void sendAsyncCommand(Command command) {
      try {
         synchronized(this.channel) {
            command.setTransactionId(++this.transactionId);
            this.commandsSent.put(this.transactionId, command);
            ByteBuffer buffer = ByteBuffer.wrap(command.getBytes("UTF-8"));

            while(buffer.hasRemaining()) {
               this.channel.write(buffer);
            }

            if (log.isDebugEnabled()) {
               log.debug(">>> " + command.toString());
            }
         }
      } catch (Exception var6) {
         log.warn("Error in sendAsyncCommand()", var6);
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
               log.warn("0 byte read on port " + this.channel.socket().getLocalPort());
               selector.registerConnection(this);
            } else if (bytesRead > 0) {
               while(true) {
                  Command command = this.readCommand();
                  Integer transactionId = command.getTransactionId();
                  Command originalCommand = null;
                  if (transactionId != null) {
                     originalCommand = (Command)this.commandsSent.remove(transactionId);
                     if (originalCommand != null) {
                        synchronized(originalCommand) {
                           originalCommand.setReply(command);
                           originalCommand.notifyAll();
                        }
                     }
                  }

                  this.onIncomingCommand(command, originalCommand);
               }
            }
         }
      } catch (BufferUnderflowException var8) {
         this.readBuffer = ByteBufferHelper.adjustSize(this.readBuffer, 1024, 10240, 2.0D);
         selector.registerConnection(this);
      } catch (IOException var9) {
         this.disconnect("Failed to read MSN command - " + var9.getMessage());
      } catch (NotYetConnectedException var10) {
         selector.registerConnection(this);
      } catch (Exception var11) {
         this.disconnect("Connection to MSN server is lost - " + var11.getMessage());
      }

   }

   protected Command readCommand() throws IOException {
      ByteBuffer buffer = (ByteBuffer)this.readBuffer.duplicate().flip();
      String line = ByteBufferHelper.readLine(buffer, "UTF-8").trim();
      if (line.length() == 0) {
         this.readBuffer = buffer.compact();
         throw new BufferUnderflowException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("<<< " + line);
         }

         Command command = new Command(line);
         int payloadSize = command.getPayloadSize();
         if (payloadSize > 0) {
            byte[] payload = ByteBufferHelper.readBytes(buffer, payloadSize);
            command.setPayload(payload);
         }

         this.readBuffer = buffer.compact();
         return command;
      }
   }

   protected abstract void onIncomingCommand(Command var1, Command var2);

   protected abstract void onDisconnect(String var1);
}
