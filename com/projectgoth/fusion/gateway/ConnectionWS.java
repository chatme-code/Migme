package com.projectgoth.fusion.gateway;

import Ice.Current;
import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.fdl.enums.DeviceModeType;
import com.projectgoth.fusion.gateway.packet.FusionPktLogin;
import com.projectgoth.fusion.gateway.packet.FusionPktLoginOld;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.ConnectionWSPrx;
import com.projectgoth.fusion.slice.ConnectionWSPrxHelper;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.Handshakedata;
import org.json.JSONException;

public class ConnectionWS extends ConnectionI implements PacketProcessorCallback {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ConnectionWS.class));
   public static final int JSON_VERSION = 2;
   private PacketProcessor packetProcessor;
   private WebSocket webSocket;
   private ConnectionWS localParentConnection;
   private ConnectionWSPrx remoteParentConnection;
   private final ConcurrentMap<String, ConnectionWS> localChildConnections = new ConcurrentHashMap();
   private final ConcurrentMap<String, ConnectionWSPrx> remoteChildConnections = new ConcurrentHashMap();
   private final Object loginLogoutLock = new Object();
   private String objectID;

   public ConnectionWS(Gateway gateway, WebSocket ws, GatewayContext gatewayContext) {
      super(gateway, ws.getRemoteSocketAddress().getAddress().getHostAddress(), ws.getRemoteSocketAddress().getPort(), gatewayContext);
      this.webSocket = ws;
      this.packetProcessor = new PacketProcessor(this, this);
   }

   public Gateway.ServerType getServerType() {
      return Gateway.ServerType.WS;
   }

   public void onChannelReadable() {
   }

   public void onChannelWritable() {
   }

   public void sendFusionPacket(FusionPacket packet) throws FusionException {
      if (this.webSocket.isOpen()) {
         try {
            this.webSocket.send(packet.toJSON(2));
         } catch (JSONException var3) {
            this.logErrorCxn("Unable to convert Fusion packet into JSON" + var3, var3, this.sessionID, this.objectID);
            this.disconnect();
         }
      } else {
         this.sendFusionPacketViaChildConnection(packet);
      }

   }

   private void sendFusionPacketViaChildConnection(FusionPacket packet) throws FusionException {
      ConnectionWS[] locals = (ConnectionWS[])this.localChildConnections.values().toArray(new ConnectionWS[0]);
      if (0 != locals.length) {
         locals[0].sendFusionPacket(packet);
      } else {
         ConnectionWSPrx[] remotes = (ConnectionWSPrx[])this.remoteChildConnections.values().toArray(new ConnectionWSPrx[0]);
         if (0 != remotes.length) {
            remotes[0].putSerializedPacket(packet.toSerializedBytes());
         } else {
            this.logWarnCxn("Unable to send fusion packet to ConnectionWS=" + this + " with websocket=" + this.webSocket.getRemoteSocketAddress(), this.sessionID, this.objectID);
         }
      }

   }

   public void disconnect() {
      super.disconnect();
      this.webSocket.close();
   }

   protected void onDeviceModeChanged(DeviceModeType mode) {
   }

   public void onFusionRequestProcessed(FusionRequest request, FusionPacket[] responses) {
      for(int i = 0; i < responses.length; ++i) {
         try {
            this.sendFusionPacket(responses[i]);
         } catch (FusionException var5) {
            this.logWarnCxn("Exception sending response packet=" + responses[i] + " to websocket=" + this.webSocket.getRemoteSocketAddress(), var5, this.sessionID, this.objectID);
         }
      }

   }

   public void onFusionRequestDiscarded(FusionRequest request) {
   }

   public void onFusionRequestException(FusionRequest request, Exception e) {
      this.logWarnCxn("onFusionRequestException for request=" + (request != null ? request.getType() : "null"), e, this.sessionID, this.objectID);
   }

   public void onWebsocketMessage(String packetJSON) {
      try {
         this.accessed();
         if (null != this.localParentConnection) {
            this.localParentConnection.accessed();
         } else if (null != this.remoteParentConnection) {
            this.remoteParentConnection.accessed();
         }

         if (!StringUtil.isBlank(packetJSON)) {
            FusionRequest request = FusionRequest.parseJSONRequest(packetJSON, 2);
            this.packetProcessor.queueFusionRequest(request);
         } else if (log.isDebugEnabled()) {
            this.logDebugCxn("Received blank websocket message! for user=" + this.getUsername(), this.sessionID, this.objectID);
         }
      } catch (Exception var4) {
         this.logErrorCxn("Unable to process packet json from websocket, packetJSON=" + packetJSON, var4, this.sessionID, this.objectID);
      }

   }

   public void onWebsocketOpen(Handshakedata handshakedata) {
   }

   public void onWebsocketClose(int code, String reason, boolean remote) {
      synchronized(this.loginLogoutLock) {
         this.onWebsocketCloseInner(code, reason, remote);
      }
   }

   private void onWebsocketCloseInner(int code, String reason, boolean remote) {
      if (remote) {
         this.logWarnCxn("websocket closed by remote - " + reason + " (" + Integer.toString(code) + ")", this.sessionID, this.objectID);
      } else {
         this.logWarnCxn("websocket closed unexpectedly - " + reason + " (" + Integer.toString(code) + ")", this.sessionID, this.objectID);
      }

      if (null != this.objectID) {
         try {
            if (null != this.localParentConnection) {
               this.localParentConnection.removeLocalChildConnectionWS(this.objectID, this);
            } else if (null != this.remoteParentConnection) {
               ConnectionWSPrx cwsPrx = ConnectionWSPrxHelper.uncheckedCast(this.connectionPrx);
               this.remoteParentConnection.removeRemoteChildConnectionWS(this.objectID, cwsPrx);
            }
         } catch (Exception var5) {
            this.logErrorCxn("Unable to deregister ConnectionWS from parent ConnectionWS", var5, this.sessionID, this.objectID);
         }

         try {
            if (null != this.localParentConnection || null != this.remoteParentConnection) {
               this.gateway.removeConnection(this.objectID, reason);
            }
         } catch (Exception var6) {
            this.logErrorCxn("Unable to deregister connection from the gateway and/or registry", var6, this.sessionID, this.objectID);
         }
      }

   }

   public void onWebsocketError(Exception e) {
      this.logErrorCxn("Unexpected error encountered in websocket", e, this.sessionID, this.objectID);
   }

   /** @deprecated */
   @Deprecated
   public ConnectionI onLogin(FusionPktLoginOld loginPacket) throws Exception {
      synchronized(this.loginLogoutLock) {
         return this.onLoginInner(loginPacket);
      }
   }

   /** @deprecated */
   @Deprecated
   private ConnectionI onLoginInner(FusionPktLoginOld loginPacket) throws Exception {
      if (this.webSocket != null && !this.webSocket.isClosed()) {
         this.setLoginPacketOld(loginPacket);
         this.loginChallenge = this.newChallengeString();
         if (loginPacket.getSessionId() == null) {
            this.sessionID = ConnectionI.newSessionID();
         } else {
            this.sessionID = loginPacket.getSessionId();
         }

         this.gateway.onConnectionAccessed(this);
         ConnectionPrx remoteConnection = null;
         ConnectionWS localConnection = (ConnectionWS)this.gateway.findConnection(this.sessionID);
         if (localConnection == null) {
            RegistryPrx registryPrx = this.findRegistry();
            if (registryPrx == null) {
               throw new Exception("Failed to locate registry");
            }

            try {
               remoteConnection = registryPrx.findConnectionObject(this.sessionID);
            } catch (ObjectNotFoundException var14) {
            }
         }

         if (null != localConnection || null != remoteConnection) {
            this.objectID = this.sessionID + ":" + UUID.randomUUID().toString();
            this.connectionPrx = this.gateway.addConnection(this.objectID, this);
            this.logDebugCxn("Added child connection to gateway", this.sessionID, this.objectID);
         }

         if (null != localConnection) {
            this.username = localConnection.getUsername();
            this.sessionPrx = localConnection.getSessionPrx();
            this.userPrx = localConnection.getUserPrx();
            this.userID = localConnection.getUserID();
            this.localParentConnection = localConnection;
            localConnection.addLocalChildConnectionWS(this.objectID, this);
            this.logDebugCxn("Added child cxn to local parent cxn", this.sessionID, this.objectID);
         } else if (null != remoteConnection) {
            this.username = remoteConnection.getUsername();
            this.sessionPrx = remoteConnection.getSessionObject();
            this.userPrx = remoteConnection.getUserObject();
            this.userID = this.userPrx.getUserData().userID;
            ConnectionWSPrx cwsRemoteCxn = ConnectionWSPrxHelper.uncheckedCast(remoteConnection);
            this.remoteParentConnection = cwsRemoteCxn;
            ConnectionWSPrx cwsPrx = ConnectionWSPrxHelper.uncheckedCast(this.connectionPrx);
            cwsRemoteCxn.addRemoteChildConnectionWS(this.objectID, cwsPrx);
            this.logDebugCxn("Added child cxn to remote parent cxn", this.sessionID, this.objectID);
         } else {
            this.objectID = this.sessionID;
            ConnectionPrx connectionPrx = this.addConnectionToIceAdaptor(true);
            if (connectionPrx == null) {
               throw new Exception("Unable to add Connection to Ice adaptor");
            }

            this.logDebugCxn("Added parent cxn to ice adaptor", this.sessionID, this.objectID);
            RegistryPrx registryPrx = this.findRegistry();
            if (registryPrx == null) {
               throw new Exception("Failed to locate registry");
            }

            try {
               UserPrx up = registryPrx.findUserObject(loginPacket.getUsername());
               SessionPrx sp = up.findSession(this.sessionID);
               if (log.isDebugEnabled()) {
                  this.logDebugSession("Reuse existing SessionPrx", this.sessionID);
               }

               try {
                  sp.touch();
               } catch (Exception var10) {
                  this.logErrorCxn("Reuse existing SessionPrx: failed to touch it, e=" + var10, var10, this.sessionID, this.objectID);
                  throw new FusionException("[SID:" + this.sessionID + "] Unable to attach to existing session");
               }

               UserDataIce udata = up.getUserData();
               int uid = udata.userID;
               this.username = loginPacket.getUsername();
               this.userPrx = up;
               this.sessionPrx = sp;
               this.userID = uid;
            } catch (LocalException var11) {
            } catch (ObjectNotFoundException var12) {
            } catch (Exception var13) {
               log.warn("[SID:" + this.sessionID + "] Unexpected exception checking for existing SessionRpcI: e=" + var13, var13);
            }
         }

         if (null != this.userPrx && null != this.sessionPrx) {
            this.loginPacketOld = loginPacket;
            this.onSessionCreatedOld(this.userPrx, this.sessionPrx, new UserData(this.userPrx.getUserData()));
         }

         return this;
      } else {
         throw new FusionException("Websocket already closed");
      }
   }

   public ConnectionI onLogin(FusionPktLogin loginPacket) throws Exception {
      synchronized(this.loginLogoutLock) {
         return this.onLoginInner(loginPacket);
      }
   }

   private ConnectionI onLoginInner(FusionPktLogin loginPacket) throws Exception {
      if (this.webSocket != null && !this.webSocket.isClosed()) {
         this.setLoginPacket(loginPacket);
         this.loginChallenge = this.newChallengeString();
         if (loginPacket.getSessionId() == null) {
            this.sessionID = ConnectionI.newSessionID();
         } else {
            this.sessionID = loginPacket.getSessionId();
         }

         this.gateway.onConnectionAccessed(this);
         ConnectionPrx remoteConnection = null;
         ConnectionWS localConnection = (ConnectionWS)this.gateway.findConnection(this.sessionID);
         if (localConnection == null) {
            RegistryPrx registryPrx = this.findRegistry();
            if (registryPrx == null) {
               throw new Exception("Failed to locate registry");
            }

            try {
               remoteConnection = registryPrx.findConnectionObject(this.sessionID);
            } catch (ObjectNotFoundException var14) {
            }
         }

         if (null != localConnection || null != remoteConnection) {
            this.objectID = this.sessionID + ":" + UUID.randomUUID().toString();
            this.connectionPrx = this.gateway.addConnection(this.objectID, this);
            this.logDebugCxn("Added child connection to gateway", this.sessionID, this.objectID);
         }

         if (null != localConnection) {
            this.username = localConnection.getUsername();
            this.sessionPrx = localConnection.getSessionPrx();
            this.userPrx = localConnection.getUserPrx();
            this.userID = localConnection.getUserID();
            this.localParentConnection = localConnection;
            localConnection.addLocalChildConnectionWS(this.objectID, this);
            this.logDebugCxn("Added child cxn to local parent cxn", this.sessionID, this.objectID);
         } else if (null != remoteConnection) {
            this.username = remoteConnection.getUsername();
            this.sessionPrx = remoteConnection.getSessionObject();
            this.userPrx = remoteConnection.getUserObject();
            this.userID = this.userPrx.getUserData().userID;
            ConnectionWSPrx cwsRemoteCxn = ConnectionWSPrxHelper.uncheckedCast(remoteConnection);
            this.remoteParentConnection = cwsRemoteCxn;
            ConnectionWSPrx cwsPrx = ConnectionWSPrxHelper.uncheckedCast(this.connectionPrx);
            cwsRemoteCxn.addRemoteChildConnectionWS(this.objectID, cwsPrx);
            this.logDebugCxn("Added child cxn to remote parent cxn", this.sessionID, this.objectID);
         } else {
            this.objectID = this.sessionID;
            ConnectionPrx connectionPrx = this.addConnectionToIceAdaptor(true);
            if (connectionPrx == null) {
               throw new Exception("Unable to add Connection to Ice adaptor");
            }

            this.logDebugCxn("Added parent cxn to ice adaptor", this.sessionID, this.objectID);
            RegistryPrx registryPrx = this.findRegistry();
            if (registryPrx == null) {
               throw new Exception("Failed to locate registry");
            }

            try {
               UserPrx up = registryPrx.findUserObject(loginPacket.getUsername());
               SessionPrx sp = up.findSession(this.sessionID);
               if (log.isDebugEnabled()) {
                  this.logDebugSession("Reuse existing SessionPrx", this.sessionID);
               }

               try {
                  sp.touch();
               } catch (Exception var10) {
                  this.logErrorCxn("Reuse existing SessionPrx: failed to touch it, e=" + var10, var10, this.sessionID, this.objectID);
                  throw new FusionException("[SID:" + this.sessionID + "] Unable to attach to existing session");
               }

               UserDataIce udata = up.getUserData();
               int uid = udata.userID;
               this.username = loginPacket.getUsername();
               this.userPrx = up;
               this.sessionPrx = sp;
               this.userID = uid;
            } catch (LocalException var11) {
            } catch (ObjectNotFoundException var12) {
            } catch (Exception var13) {
               log.warn("[SID:" + this.sessionID + "] Unexpected exception checking for existing SessionRpcI: e=" + var13, var13);
            }
         }

         if (null != this.userPrx && null != this.sessionPrx) {
            this.loginPacket = loginPacket;
            this.onSessionCreated(this.userPrx, this.sessionPrx, new UserData(this.userPrx.getUserData()));
         }

         return this;
      } else {
         throw new FusionException("Websocket already closed");
      }
   }

   public void addLocalChildConnectionWS(String uuid, ConnectionWS localChildConnectionWS) {
      this.localChildConnections.put(uuid, localChildConnectionWS);
   }

   public void addRemoteChildConnectionWS(String uuid, ConnectionWSPrx remoteChildConnectionWS, Current __current) {
      this.remoteChildConnections.put(uuid, remoteChildConnectionWS);
   }

   public void removeLocalChildConnectionWS(String uuid, ConnectionWS localChildConnectionWS) {
      if (log.isDebugEnabled()) {
         this.logDebugCxn("Removing local child ConnectionWS=" + uuid + " from parent ConnectionWS", this.sessionID, this.objectID);
      }

      this.localChildConnections.remove(uuid, localChildConnectionWS);
   }

   public void removeRemoteChildConnectionWS(String uuid, ConnectionWSPrx remoteChildConnectionWS, Current __current) {
      if (log.isDebugEnabled()) {
         this.logDebugCxn("Removing remote child ConnectionWS=" + uuid + " from parent ConnectionWS", this.sessionID, this.objectID);
      }

      this.remoteChildConnections.remove(uuid, remoteChildConnectionWS);
   }

   public boolean processPacket(ConnectionPrx requestingConnection, byte[] packet, Current __current) throws FusionException {
      this.logWarnCxn("Mixture of ConnectionWS and ConnectionHTTP for single session encountered. Only expected to happen for existing live sessions at time WS is enabled on server! processPacket from requestingConnection=" + requestingConnection + " " + requestingConnection.ice_toString(), this.sessionID, this.objectID);
      return false;
   }

   public void silentlyDropIncomingPackets(Current __current) {
      log.warn("No longer processing incoming packets for this connection for user [" + this.username + "]");
      this.packetProcessor.stopProcessing();
   }

   public void packetProcessed(byte[] packets, Current __current) {
      if (this.webSocket.isOpen()) {
         this.webSocket.send(packets);
      } else if (this.localChildConnections.size() != 0) {
         ConnectionWS[] cxns = (ConnectionWS[])this.localChildConnections.values().toArray(new ConnectionWS[0]);
         cxns[0].packetProcessed(packets);
      } else if (this.remoteChildConnections.size() != 0) {
         ConnectionPrx[] cxns = (ConnectionPrx[])this.remoteChildConnections.values().toArray(new ConnectionPrx[0]);
         cxns[0].packetProcessed(packets);
      } else {
         log.warn("Unable to send responses to ConnectionWS=" + this + " with websocket=" + this.webSocket.getRemoteSocketAddress());
      }

   }

   protected String getObjectIdWithinGateway() {
      return this.objectID;
   }

   public void accessed(Current __current) {
      super.accessed();
   }

   private void logErrorCxn(String message, Exception e, String sessionID, String objectID) {
      log.error("[SID:" + sessionID + "] " + message + ", objectID=" + objectID + ", e=" + e, e);
   }

   private void logWarnCxn(String message, Exception e, String sessionID, String objectID) {
      log.warn("[SID:" + sessionID + "] " + message + ", objectID=" + objectID + ", e=" + e, e);
   }

   private void logWarnCxn(String message, String sessionID, String objectID) {
      log.warn("[SID:" + sessionID + "] " + message + ", objectID=" + objectID);
   }

   private void logDebugCxn(String message, String sessionID, String objectID) {
      if (log.isDebugEnabled()) {
         log.debug("[SID:" + sessionID + "] " + message + ", objectID=" + objectID);
      }

   }

   private void logDebugSession(String message, String sessionID) {
      if (log.isDebugEnabled()) {
         log.debug("[SID:" + sessionID + "] " + message);
      }

   }
}
