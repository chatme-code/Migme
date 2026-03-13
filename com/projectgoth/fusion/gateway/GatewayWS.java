package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.websocket.FusionWebSocketServer;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

public class GatewayWS extends Gateway {
   private FusionWebSocketServer webSocketServer;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatewayWS.class));

   public GatewayWS(GatewayContext context) {
      super(context);
      this.serverType = Gateway.ServerType.WS;
   }

   public void registerConnection(ConnectionI connection, int interest) throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   protected void registerPendingConnections() throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   public void onConnectionDisconnected(ConnectionI connection) {
      if (log.isDebugEnabled()) {
         log.debug("Connection " + connection.getDisplayName() + " disconnected");
      }

      if (connection.getConnectionPrx() == null) {
         this.applicationContext.getPurger().remove(connection);
      }

   }

   protected void processSelections(Set<SelectionKey> selectedKeys) throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   protected void onKeyAcceptable(SelectionKey key) throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   public ConnectionWS createAndRegisterWSConnection(WebSocket conn) {
      ConnectionWS connection = new ConnectionWS(this, conn, this.applicationContext);
      if (log.isDebugEnabled()) {
         log.debug("Accepted connection " + connection.getDisplayName());
      }

      this.applicationContext.getPurger().add(connection);
      return connection;
   }

   protected void doAccepts() throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   protected void onKeyReadable(SelectionKey key) throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   protected void onKeyWritable(SelectionKey key) throws FusionException {
      throw new FusionException("Unexpected function called");
   }

   public void bindServerSocketWS() throws UnknownHostException {
      if (this.webSocketServer == null) {
         this.webSocketServer = new FusionWebSocketServer(this.port, this);
      }

      this.webSocketServer.start();
   }

   public void bindServerSocket() throws IOException {
      this.bindServerSocketWS();
   }

   public void run() {
   }

   public void closeServerChannel() throws IOException {
      try {
         this.webSocketServer.stop();
      } catch (InterruptedException var2) {
      }

   }

   public int getLocalPort() {
      return this.webSocketServer.getPort();
   }

   public Selector getSelector() {
      return null;
   }

   public void logDebugPurgingCompleted() {
      log.debug("Purging task completed. " + this.connectionCount.get() + " " + this.getServerType() + " connection(s) left on the adaptor.");
   }

   public static void main(String[] args) {
      GatewayMain.main(args);
   }
}
