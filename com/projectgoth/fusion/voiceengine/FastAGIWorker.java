package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FastAGIWorker implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FastAGIWorker.class));
   private FastAGIServer server;
   private CallMakerI callMaker;
   private Socket socket;

   public FastAGIWorker(FastAGIServer server, CallMakerI callMaker, Socket socket) {
      this.server = server;
      this.callMaker = callMaker;
      this.socket = socket;
   }

   public void run() {
      try {
         FastAGICommand command = new FastAGICommand(this.socket.getInputStream());
         log.debug(command.getRawCommand());
         String request = command.getRequest();
         if ("callingcard".equalsIgnoreCase(request)) {
            this.startCallingCard(command);
         }
      } catch (CreateException var14) {
         log.warn("Unable to create EJB for database operations", var14);
      } catch (Exception var15) {
         log.warn(var15.getClass().getName() + " occured while processing FastAGI request", var15);
      } finally {
         try {
            this.socket.close();
         } catch (Exception var13) {
         }

      }

   }

   private void startCallingCard(FastAGICommand command) throws CreateException, RemoteException, IOException {
      FastAGIChannel channel = null;
      ExtendedControl control = null;
      LogicHelper helper = null;
      CallingCard callingCard = null;

      try {
         log.warn("Starting the calling card system");
         channel = new FastAGIChannel(this, this.server, command, this.socket);
         control = new ExtendedControl(channel);
         helper = new LogicHelper(this, this.server, command, control, this.callMaker);
         callingCard = new CallingCard(helper, control);
         callingCard.execute();
      } catch (CreateException var13) {
         log.warn("Error running the calling card system (ex1); " + var13.getMessage());
      } catch (RemoteException var14) {
         log.warn("Error running the calling card system (ex2); " + var14.getMessage());
      } catch (IOException var15) {
         log.warn("Error running the calling card system (ex3); " + var15.getMessage());
      } finally {
         log.info("Completed the calling card system");
      }

   }
}
