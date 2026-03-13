package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

public class FastAGIServer extends Thread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FastAGIServer.class));
   private ServerSocket serverSocket;
   private int port;
   private Executor pool;
   private CallMakerI callMaker;
   private int callThroughValidPeriod;

   public FastAGIServer(CallMakerI callMaker, int port, int threads) throws IOException {
      this.callMaker = callMaker;
      this.port = port;
      this.serverSocket = new ServerSocket(port);
      this.serverSocket.setReuseAddress(true);
      this.pool = Executors.newFixedThreadPool(threads);
   }

   public int getCallThroughValidPeriod() {
      return this.callThroughValidPeriod;
   }

   public void setCallThroughValidPeriod(int callThroughValidPeriod) {
      this.callThroughValidPeriod = callThroughValidPeriod;
   }

   public void run() {
      while(true) {
         try {
            this.pool.execute(new FastAGIWorker(this, this.callMaker, this.serverSocket.accept()));
         } catch (IOException var4) {
            log.warn("Failed to accept FastAGI client connections - " + var4.getClass().getName() + ":" + var4.getMessage());

            try {
               this.serverSocket.close();
               this.serverSocket = new ServerSocket(this.port);
               this.serverSocket.setReuseAddress(true);
            } catch (IOException var3) {
               log.fatal("Failed to restart FastAGI server socket - " + var4.getClass().getName() + ":" + var4.getMessage());
            }
         }
      }
   }
}
