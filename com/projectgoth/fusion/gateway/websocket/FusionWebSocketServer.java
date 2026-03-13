package com.projectgoth.fusion.gateway.websocket;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.ConnectionWS;
import com.projectgoth.fusion.gateway.GatewayWS;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class FusionWebSocketServer extends WebSocketServer {
   private GatewayWS gway;
   private ConcurrentHashMap<WebSocket, ConnectionWS> connectionMap;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionWebSocketServer.class));

   public FusionWebSocketServer(int port, GatewayWS gway) throws UnknownHostException {
      super(new InetSocketAddress(port));
      this.gway = gway;
      this.connectionMap = new ConcurrentHashMap();
      if (log.isDebugEnabled()) {
         log.debug("Starting the websocket server - OK");
      }

   }

   public void onOpen(WebSocket conn, ClientHandshake handshake) {
      if (log.isDebugEnabled()) {
         log.debug("got a new open connection from " + conn.getRemoteSocketAddress().getAddress());
      }

      ConnectionWS connection = this.gway.createAndRegisterWSConnection(conn);
      this.connectionMap.put(conn, connection);
   }

   public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("closing the connection for  " + conn.getRemoteSocketAddress().getAddress() + " because " + reason);
         }
      } catch (Exception var6) {
      }

      ConnectionWS connectionWS = (ConnectionWS)this.connectionMap.get(conn);
      if (connectionWS != null) {
         connectionWS.onWebsocketClose(code, reason, remote);
         this.connectionMap.remove(conn);
      }
   }

   public void onMessage(WebSocket conn, String message) {
      if (log.isDebugEnabled()) {
         log.debug("Got message - " + message + " ; from " + conn.getRemoteSocketAddress());
      }

      ConnectionWS connectionWS = (ConnectionWS)this.connectionMap.get(conn);
      if (connectionWS != null) {
         connectionWS.onWebsocketMessage(message);
      }
   }

   public void onFragment(WebSocket conn, Framedata fragment) {
   }

   public void onError(WebSocket conn, Exception ex) {
      try {
         log.warn("got error - " + ex.getMessage() + " ; from " + conn.getRemoteSocketAddress().getAddress());
      } catch (Exception var4) {
      }

      ConnectionWS connectionWS = (ConnectionWS)this.connectionMap.get(conn);
      if (connectionWS != null) {
         connectionWS.onWebsocketError(ex);
      }
   }
}
