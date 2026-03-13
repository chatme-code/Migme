package com.projectgoth.fusion.emailalert;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SMTPServer implements Runnable {
   private Thread socketListenerThread;
   private ServerSocket serverSocket;

   public SMTPServer(int port) throws Exception {
      try {
         this.serverSocket = new ServerSocket(port);
      } catch (IOException var3) {
         throw new Exception("Could not listen on port " + port + ": " + var3.getMessage());
      }

      this.socketListenerThread = new Thread(this);
      this.socketListenerThread.start();
   }

   public void run() {
      EmailAlert.logger.info("Ready for a connection on port " + this.serverSocket.getLocalPort());

      while(true) {
         Socket clientSocket = null;

         try {
            clientSocket = this.serverSocket.accept();
         } catch (IOException var3) {
            EmailAlert.logger.warn("Socket accept failed on port " + this.serverSocket.getLocalPort() + ": " + var3.getMessage());
            continue;
         }

         EmailAlert.logger.debug("Accepted remote connection from " + clientSocket.getInetAddress().getHostAddress());
         EmailAlert.notificationsThreadPool.execute(new EmailNotificationProcessor(clientSocket));
      }
   }
}
