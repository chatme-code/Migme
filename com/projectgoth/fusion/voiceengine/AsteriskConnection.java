package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.naming.AuthenticationException;
import org.apache.log4j.Logger;

public class AsteriskConnection {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AsteriskConnection.class));
   private static final String DEFAULT_CHARSET = "UTF-8";
   private static final String LINE_TERMINATOR = "\r\n";
   private static final int SOCKET_TIMEOUT = 10000;
   private AsteriskListener listener;
   private Socket socket;
   private BufferedReader reader;
   private Thread readingThread;
   private Thread writingThread;
   private BlockingQueue<AsteriskCommand> commandQueue = new LinkedBlockingQueue();
   private Set<AsteriskCommand> commandSent = Collections.synchronizedSet(new HashSet());
   private AtomicBoolean isLoggedIn = new AtomicBoolean();

   public AsteriskConnection(AsteriskListener listener) {
      this.listener = listener;
      this.writingThread = new Thread() {
         public void run() {
            AsteriskConnection.this.sendQueuedCommands();
         }
      };
      this.writingThread.start();
      this.readingThread = new Thread() {
         public void run() {
            AsteriskConnection.this.readCommands();
         }
      };
      this.readingThread.start();
   }

   public void connect(String server, int port) throws IOException {
      this.isLoggedIn.set(false);
      this.socket = new Socket();
      this.socket.connect(new InetSocketAddress(server, port), 10000);
      this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
      this.reader.readLine();
   }

   public void disconnect() {
      this.isLoggedIn.set(false);

      try {
         this.socket.close();
      } catch (Exception var2) {
      }

   }

   public boolean isConnected() {
      return this.socket != null && this.readingThread.isAlive() && this.writingThread.isAlive() && this.isLoggedIn.get();
   }

   public void login(String username, String password) throws AuthenticationException, IOException {
      AsteriskCommand command = new AsteriskCommand(AsteriskCommand.Type.ACTION, "Login");
      command.setProperty("Username", username);
      command.setProperty("Secret", password);
      this.sendCommand(command);

      try {
         AsteriskCommand response = this.readCommand();
         if (!"success".equalsIgnoreCase(response.getName())) {
            throw new AuthenticationException(response.getProperty("Message"));
         }
      } catch (ParseException var5) {
         throw new AuthenticationException(var5.getMessage());
      }

      this.isLoggedIn.set(true);
   }

   public void sendCommand(AsteriskCommand command) throws IOException {
      synchronized(command) {
         this.commandQueue.add(command);

         try {
            command.wait(10000L);
         } catch (Exception var5) {
         }
      }

      if (!this.commandSent.remove(command)) {
         log.warn("Failed to send command. Closing connection");
         this.disconnect();
         throw new IOException("Failed to send command");
      }
   }

   private void sendQueuedCommands() {
      while(true) {
         AsteriskCommand command = null;

         try {
            command = (AsteriskCommand)this.commandQueue.take();
            log.debug("\n" + command.toString());
            OutputStream out = this.socket.getOutputStream();
            out.write(command.toString().getBytes("UTF-8"));
            out.write("\r\n".getBytes("UTF-8"));
            this.commandSent.add(command);
         } catch (Exception var13) {
            log.warn(var13.getClass().getName() + " occured in AsteriskConnection.sendQueuedCommands()", var13);
         } finally {
            synchronized(command) {
               command.notifyAll();
            }
         }
      }
   }

   private void readCommands() {
      while(true) {
         try {
            if (this.isLoggedIn.get()) {
               AsteriskCommand command = this.readCommand();
               switch(command.getType()) {
               case EVENT:
                  this.listener.asteriskEventReceived(command);
               case RESPONSE:
               }
            } else {
               Thread.sleep(100L);
            }
         } catch (IOException var2) {
            this.disconnect();
            this.listener.asteriskDisconnected(var2.getMessage());
         } catch (Exception var3) {
            log.error(var3.getClass().getName() + " occured in AsteriskConnection.run()", var3);
         }
      }
   }

   private AsteriskCommand readCommand() throws EOFException, ParseException, IOException {
      StringBuilder builder = new StringBuilder();

      AsteriskCommand var3;
      try {
         do {
            while(true) {
               String line = this.reader.readLine();
               if (line == null) {
                  throw new EOFException();
               }

               line = line.trim();
               if (line.length() == 0) {
                  break;
               }

               builder.append(line).append("\r\n");
            }
         } while(builder.length() <= 0);

         var3 = new AsteriskCommand(builder.toString());
      } finally {
         log.debug("\n" + builder.toString());
      }

      return var3;
   }
}
