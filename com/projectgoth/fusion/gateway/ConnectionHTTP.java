package com.projectgoth.fusion.gateway;

import Ice.Current;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.DeviceModeType;
import com.projectgoth.fusion.gateway.packet.FusionPacketFactory;
import com.projectgoth.fusion.gateway.packet.FusionPktCreateSession;
import com.projectgoth.fusion.gateway.packet.FusionPktCreateSessionOld;
import com.projectgoth.fusion.gateway.packet.FusionPktHTTPPollOld;
import com.projectgoth.fusion.gateway.packet.FusionPktHttpPoll;
import com.projectgoth.fusion.gateway.packet.FusionPktLogin;
import com.projectgoth.fusion.gateway.packet.FusionPktLoginOld;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.json.JSONException;

public class ConnectionHTTP extends ConnectionI implements PacketProcessorCallback {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ConnectionHTTP.class));
   public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
   private static final int READ_BUFFER_SIZE = 5120;
   private static final int MAX_READ_BUFFER_SIZE = 358400;
   private static final String HTTP_ERROR = "HTTP/1.1 500 ERROR\r\n\r\n";
   private ConnectionHTTP.Format format;
   private ByteBuffer readBuffer = ByteBuffer.allocate(5120);
   private ByteBuffer writeBuffer;
   private PacketProcessor packetProcessor = new PacketProcessor(this, this);
   private Queue<FusionPacket> outgoingQueue;
   private int jsonVersion = 1;
   private boolean isPermanent;

   public ConnectionHTTP(Gateway gateway, SocketChannel channel, GatewayContext gatewayContext) {
      super(gateway, channel, gatewayContext);
   }

   public ConnectionHTTP(ConnectionHTTP connection) {
      super(connection);
      this.format = connection.format;
      this.jsonVersion = connection.jsonVersion;
   }

   public Gateway.ServerType getServerType() {
      return Gateway.ServerType.HTTP;
   }

   public void onChannelReadable() {
      try {
         this.processHTTPRequest(this.readHTTPRequest());
      } catch (BufferUnderflowException var6) {
         try {
            this.readBuffer = ByteBufferHelper.adjustSize(this.readBuffer, 5120, 358400, 2.0D);
            this.gateway.registerConnection(this, 1);
         } catch (BufferOverflowException var4) {
            log.warn("Terminating connection " + this.getDisplayName() + " - Read buffer is full");
            this.disconnect();
         } catch (FusionException var5) {
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
               log.warn("Terminating connection " + this.getDisplayName() + " - Trying to make invalid connection to gateway", var5);
               this.disconnect();
            }
         }
      } catch (EOFException var7) {
         this.disconnect();
      } catch (Exception var8) {
         if (log.isDebugEnabled()) {
            log.debug(var8.getClass().getName() + " caught while reading on port " + this.getRemotePort(), var8);
            log.debug("Cause=" + var8.getCause());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            var8.printStackTrace(pw);
            log.debug(sw.toString());
         }

         this.writeHTTPResponse("HTTP/1.1 500 ERROR\r\n\r\n", true);
         this.disconnect();
      }

   }

   public void onChannelWritable() {
      try {
         this.channel.write(this.writeBuffer);
         if (this.writeBuffer.hasRemaining()) {
            this.gateway.registerConnection(this, 4);
         } else if (this.format == ConnectionHTTP.Format.BINARY) {
            this.disconnect();
         } else {
            this.gateway.registerConnection(this, 1);
         }
      } catch (Exception var2) {
         if (log.isDebugEnabled()) {
            log.debug(var2.getClass().getName() + " caught while writing to port " + this.getRemotePort(), var2);
         }

         this.disconnect();
      }

   }

   private HTTPRequest readHTTPRequest() throws IOException {
      int bytesRead = this.channel.read(this.readBuffer);
      if (bytesRead == -1) {
         throw new EOFException("Socket closed - EOF");
      } else if (bytesRead == 0) {
         throw new BufferUnderflowException();
      } else {
         ByteBuffer buffer = (ByteBuffer)this.readBuffer.duplicate().flip();
         HTTPRequest httpRequest = new HTTPRequest();
         httpRequest.read(buffer);
         if (log.isDebugEnabled()) {
            log.debug("Receiving request on port " + this.getRemotePort() + "\n" + httpRequest + "\n");
         }

         this.readBuffer = buffer.compact();
         String contentType = httpRequest.getProperty("Content-Type");
         if (contentType != null && contentType.regionMatches(true, 0, "text/xml", 0, 8)) {
            this.format = ConnectionHTTP.Format.XML;
         } else if (contentType != null && contentType.regionMatches(true, 0, "application/json", 0, 16)) {
            this.format = ConnectionHTTP.Format.JSON;
         } else {
            this.format = ConnectionHTTP.Format.BINARY;
         }

         String jsonVersionStr = httpRequest.getProperty("X-Mig33-JSON-Version");
         if (!StringUtil.isBlank(jsonVersionStr)) {
            try {
               this.jsonVersion = Integer.parseInt(jsonVersionStr);
            } catch (NumberFormatException var7) {
            }
         }

         if (log.isDebugEnabled()) {
            log.debug("Format is " + this.format.toString());
         }

         return httpRequest;
      }
   }

   private void processHTTPRequest(HTTPRequest httpRequest) throws Exception {
      byte[] content = httpRequest.getContent();
      FusionRequest request = null;
      this.updateRemoteAddress(httpRequest);
      if (content == null) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
            request = new FusionPktHttpPoll();
         } else {
            request = new FusionPktHTTPPollOld();
         }
      } else if (this.format == ConnectionHTTP.Format.BINARY) {
         request = FusionRequest.parseRequest(content);
      } else if (this.format == ConnectionHTTP.Format.XML) {
         request = FusionRequest.parseXMLRequest(new String(content, "UTF-8"));
      } else if (this.format == ConnectionHTTP.Format.JSON) {
         request = FusionRequest.parseJSONRequest(new String(content, "UTF-8"), this.jsonVersion);
      }

      if (request == null) {
         throw new Exception("Failed to convert HTTP packet into a Fusion request");
      } else {
         ((FusionRequest)request).setConnection(this);
         boolean requestQueued = false;
         ConnectionHTTP localConnection = null;
         ConnectionPrx remoteConnection = null;
         String sessionId = httpRequest.getSessionId();
         String encryptedSessionId = null;
         if (((FusionRequest)request).getType() == 211) {
            if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
               FusionPktCreateSession createSessionPacket = (FusionPktCreateSession)FusionPacketFactory.getSpecificRequest((FusionPacket)request);
               log.info(String.format("Getting sessionId from packet for CREATE_SESSION '%s' instead of from cookie '%s", createSessionPacket.getSessionId(), sessionId));
               sessionId = createSessionPacket.getSessionId();
            } else {
               FusionPktCreateSessionOld createSessionPacket = (FusionPktCreateSessionOld)FusionPacketFactory.getSpecificRequest((FusionPacket)request);
               log.info(String.format("Getting sessionId from packet for CREATE_SESSION '%s' instead of from cookie '%s", createSessionPacket.getSessionID(), sessionId));
               sessionId = createSessionPacket.getSessionID();
            }
         }

         if (!StringUtil.isBlank(sessionId)) {
            if (SSOLogin.isEncryptedSessionID(sessionId)) {
               sessionId = SSOLogin.getSessionIDFromEncryptedSessionID(sessionId);
               if (StringUtil.isBlank(sessionId)) {
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.LOG_EID_DECRYPTION_FAILED_REMOTE_ADDRESS_ENABLED)) {
                     log.error("Decryption failure of encrypted session id " + this.encryptedSessionID + " was from remote address=" + this.getRemoteAddress());
                  }

                  throw new Exception("Invalid session ID in HTTP request packet");
               }
            }

            if (((FusionRequest)request).sessionRequired() || ((FusionRequest)request).getType() == 211) {
               localConnection = (ConnectionHTTP)this.gateway.findConnection(sessionId);
               if (localConnection == null) {
                  RegistryPrx registryPrx = this.findRegistry();
                  if (registryPrx == null) {
                     throw new Exception("Failed to locate registry");
                  }

                  try {
                     remoteConnection = registryPrx.findConnectionObject(sessionId);
                  } catch (ObjectNotFoundException var11) {
                  }
               }
            }
         }

         if (!((FusionRequest)request).sessionRequired() && localConnection == null && remoteConnection == null) {
            if (log.isDebugEnabled()) {
               log.debug("Processing request");
            }

            this.accessed();
            requestQueued = this.packetProcessor.queueFusionRequest((FusionRequest)request);
         } else {
            if (sessionId == null || sessionId.length() == 0) {
               throw new Exception("No session ID in HTTP request packet");
            }

            if (localConnection != null) {
               if (SystemProperty.getBool("EnforceSessionOrigin", false)) {
                  this.enforceSessionOrigin(sessionId, localConnection.remoteAddress, this.remoteAddress, "local");
               }

               if (log.isDebugEnabled()) {
                  log.debug("Processing request on local server");
               }

               if (((FusionRequest)request).getType() == 13 && localConnection.packetProcessor.getQueueSize() > 0) {
                  requestQueued = false;
               } else {
                  localConnection.accessed();
                  requestQueued = localConnection.packetProcessor.queueFusionRequest((FusionRequest)request);
               }
            } else {
               if (remoteConnection == null) {
                  log.warn(String.format("Unable to find ConnectionHTTP object with session id %s request-type=%d", sessionId, ((FusionRequest)request).getType()));
                  throw new Exception("Could not find conenction object");
               }

               if (SystemProperty.getBool("EnforceSessionOrigin", false)) {
                  this.enforceSessionOrigin(sessionId, remoteConnection.getRemoteIPAddress(), this.remoteAddress, "registry");
               }

               if (log.isDebugEnabled()) {
                  log.debug("Processing request on remote server");
               }

               requestQueued = remoteConnection.processPacket(this.addConnectionToIceAdaptor(false), content);
            }
         }

         if (!requestQueued) {
            this.writeHTTPResponse((new HTTPResponse()).toString(), true);
         }

      }
   }

   private void updateRemoteAddress(HTTPRequest httpRequest) {
      String forwardedIp = httpRequest.getProperty("X-Forwarded-For");
      if (!StringUtil.isBlank(forwardedIp)) {
         String[] forwardedIps = forwardedIp.split(",");
         String xffIp = forwardedIps[forwardedIps.length - 1].trim();
         if (!this.remoteAddress.equals(xffIp)) {
            if (xffIp.contains(":")) {
               String[] ipPortPair = xffIp.split(":");
               if (ipPortPair.length == 2) {
                  xffIp = ipPortPair[0];
               }
            }

            if (!this.remoteAddress.equals(xffIp)) {
               this.remoteAddress = xffIp;
               log.info("RemoteAddress:" + this.remoteAddress + " assigned from HTTP XFF header:" + forwardedIp + " with socket IP:" + this.channel.socket().getInetAddress().getHostAddress());
               if (xffIp.startsWith("10.") || xffIp.startsWith("172.16.") || xffIp.startsWith("192.168.")) {
                  log.info("RemoteAddress ending with local IP:" + forwardedIp);
               }

            }
         }
      }
   }

   private void enforceSessionOrigin(String sessionId, String connectionRemoteAddress, String requestRemoteAddress, String connectionSource) throws Exception {
      if (!StringUtil.isBlank(requestRemoteAddress) && !StringUtil.isBlank(connectionRemoteAddress)) {
         int lastindex_connectionRemoteAddress = connectionRemoteAddress.lastIndexOf(46);
         int lastindex_requestRemoteAddress = requestRemoteAddress.lastIndexOf(46);
         if (lastindex_connectionRemoteAddress < 0 || lastindex_requestRemoteAddress < 0) {
            log.warn("Unable to determine network address for session[" + sessionId + "] source[" + connectionSource + "] connectionRemoteAddress[" + connectionRemoteAddress + "] requestRemoteAddress[" + requestRemoteAddress + "]");
            return;
         }

         String connectionNetworkAddress = connectionRemoteAddress.substring(0, lastindex_connectionRemoteAddress);
         String requestNetworkAddress = requestRemoteAddress.substring(0, lastindex_requestRemoteAddress);
         if (!connectionNetworkAddress.equals(requestNetworkAddress)) {
            log.info("Session was created from [" + connectionRemoteAddress + "] but was accessed from [" + requestRemoteAddress + "]");
            throw new Exception("Unable to process request right now. Please try again later");
         }
      } else {
         log.warn("Not able to enforce session origin for session[" + sessionId + "], source[" + connectionSource + "] connectionRemoteAddress[" + connectionRemoteAddress + "] requestRemoteAddress[" + requestRemoteAddress + "]");
      }

   }

   private void writeHTTPResponse(String response, boolean executeNow) {
      try {
         this.writeBuffer = ByteBuffer.wrap(response.getBytes("8859_1"));
         if (executeNow) {
            this.onChannelWritable();
         } else {
            this.gateway.registerConnection(this, 4);
         }

         if (log.isDebugEnabled()) {
            log.debug("Sending response to port " + this.getRemotePort() + "\n" + response + "\n");
         }
      } catch (UnsupportedEncodingException var4) {
         log.error("Unable to convert HTTP response into byte stream", var4);
         this.disconnect();
      } catch (FusionException var5) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
            log.error("Trying to call method from unsuppported gateway type", var5);
            this.disconnect();
         }
      }

   }

   private List<FusionPacket> poll() {
      ArrayList<FusionPacket> packets = new ArrayList();
      if (this.outgoingQueue != null) {
         synchronized(this.outgoingQueue) {
            packets.addAll(this.outgoingQueue);
            this.outgoingQueue.clear();
         }
      }

      return packets;
   }

   public boolean isOnLoginCalled() {
      return this.isPermanent;
   }

   public ConnectionI onLogin(FusionPktLogin loginPacket) throws Exception {
      if (this.format == ConnectionHTTP.Format.BINARY) {
         super.onLogin(loginPacket);
         ConnectionPrx connectionPrx = this.addConnectionToIceAdaptor(true);
         if (connectionPrx == null) {
            throw new Exception("Unable to add Connection to Ice adaptor");
         } else {
            return this;
         }
      } else {
         ConnectionHTTP connection = new ConnectionHTTP(this);
         connection.isPermanent = true;
         connection.setLoginPacket(loginPacket);
         connection.loginChallenge = connection.newChallengeString();
         if (loginPacket.getSessionId() == null) {
            connection.sessionID = ConnectionI.newSessionID();
         } else {
            connection.sessionID = loginPacket.getSessionId();
         }

         this.gateway.onConnectionAccessed(connection);
         ConnectionPrx connectionPrx = connection.addConnectionToIceAdaptor(true);
         if (connectionPrx == null) {
            throw new Exception("Unable to add Connection to Ice adaptor");
         } else {
            return connection;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public ConnectionI onLogin(FusionPktLoginOld loginPacket) throws Exception {
      if (this.format == ConnectionHTTP.Format.BINARY) {
         super.onLogin(loginPacket);
         ConnectionPrx connectionPrx = this.addConnectionToIceAdaptor(true);
         if (connectionPrx == null) {
            throw new Exception("Unable to add Connection to Ice adaptor");
         } else {
            return this;
         }
      } else {
         ConnectionHTTP connection = new ConnectionHTTP(this);
         connection.isPermanent = true;
         connection.setLoginPacketOld(loginPacket);
         connection.loginChallenge = connection.newChallengeString();
         if (loginPacket.getSessionId() == null) {
            connection.sessionID = ConnectionI.newSessionID();
         } else {
            connection.sessionID = loginPacket.getSessionId();
         }

         this.gateway.onConnectionAccessed(connection);
         ConnectionPrx connectionPrx = connection.addConnectionToIceAdaptor(true);
         if (connectionPrx == null) {
            throw new Exception("Unable to add Connection to Ice adaptor");
         } else {
            return connection;
         }
      }
   }

   public void onCreatingSession() {
      super.onCreatingSession();
      this.outgoingQueue = new LinkedList();
   }

   public void sendFusionPacket(FusionPacket packet) throws FusionException {
      synchronized(this.outgoingQueue) {
         this.outgoingQueue.add(packet);
      }
   }

   public boolean processPacket(ConnectionPrx requestingConnection, byte[] packet, Current __current) throws FusionException {
      try {
         FusionRequest request = null;
         if (packet != null && packet.length != 0) {
            if (this.format == ConnectionHTTP.Format.BINARY) {
               request = FusionRequest.parseRequest(packet);
            } else if (this.format == ConnectionHTTP.Format.XML) {
               request = FusionRequest.parseXMLRequest(new String(packet, "UTF-8"));
            } else if (this.format == ConnectionHTTP.Format.JSON) {
               request = FusionRequest.parseJSONRequest(new String(packet, "UTF-8"), this.jsonVersion);
            }
         } else {
            if (this.packetProcessor.getQueueSize() > 0) {
               return false;
            }

            if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week2UseAutogeneratedPacketsEnabled.getValue()) {
               request = new FusionPktHttpPoll();
            } else {
               request = new FusionPktHTTPPollOld();
            }
         }

         if (request == null) {
            throw new Exception("Failed to parse byte stream into a Fusion request");
         } else {
            ((FusionRequest)request).setConnectionPrx(requestingConnection);
            this.accessed();
            return this.packetProcessor.queueFusionRequest((FusionRequest)request);
         }
      } catch (Exception var5) {
         throw new FusionException(var5.getMessage());
      }
   }

   public void packetProcessed(byte[] result, Current __current) {
      if (this.connectionPrx != null && !this.registeredWithRegistry) {
         this.removeConnectionFromIceAdaptor();
      }

      HTTPResponse response;
      if (this.format == ConnectionHTTP.Format.BINARY) {
         response = new HTTPResponse("image/gif", result);
      } else if (this.format == ConnectionHTTP.Format.XML) {
         response = new HTTPResponse("text/xml", result);
      } else {
         if (this.format != ConnectionHTTP.Format.JSON) {
            log.error("packetProcessed(): Unknown format: " + this.format);
            return;
         }

         response = new HTTPResponse("application/json", result);
      }

      this.writeHTTPResponse(response.toString(), false);
   }

   public void onFusionRequestProcessed(FusionRequest request, FusionPacket[] responses) {
      try {
         List<FusionPacket> packetList = new ArrayList();
         if (responses != null) {
            ((List)packetList).addAll(Arrays.asList(responses));
         }

         ((List)packetList).addAll(this.poll());
         if (this.format == ConnectionHTTP.Format.JSON) {
            int chunkSize = SystemProperty.getInt((String)"HTTPChunkSize", 0);
            if (chunkSize > 0 && ((List)packetList).size() > chunkSize) {
               synchronized(this.outgoingQueue) {
                  List<FusionPacket> tmp = this.poll();
                  this.outgoingQueue.addAll(((List)packetList).subList(chunkSize, ((List)packetList).size()));
                  this.outgoingQueue.addAll(tmp);
               }

               packetList = ((List)packetList).subList(0, chunkSize);
            }
         }

         FusionPacket[] packetArray = (FusionPacket[])((List)packetList).toArray(new FusionPacket[((List)packetList).size()]);
         byte[] result = null;
         byte[] result;
         if (this.format == ConnectionHTTP.Format.BINARY) {
            result = FusionPacket.toByteArray(packetArray);
         } else if (this.format == ConnectionHTTP.Format.XML) {
            result = FusionPacket.toXML(packetArray).getBytes("UTF-8");
         } else {
            if (this.format != ConnectionHTTP.Format.JSON) {
               log.error("onFusionRequestProcessed(): Unknown format...: " + this.format);
               return;
            }

            result = FusionPacket.toJSON(packetArray, this.jsonVersion).getBytes("UTF-8");
         }

         if (request.getConnectionPrx() != null) {
            request.getConnectionPrx().packetProcessed(result);
         } else {
            request.getConnection().packetProcessed(result);
         }
      } catch (UnsupportedEncodingException var9) {
         log.error("Unable to convert Fusion packet(s) into byte stream", var9);
         this.writeHTTPResponse("HTTP/1.1 500 ERROR\r\n\r\n", true);
         this.disconnect();
      } catch (JSONException var10) {
         log.error("Unable to convert Fusion packet(s) into JSON", var10);
         this.writeHTTPResponse("HTTP/1.1 500 ERROR\r\n\r\n", true);
         this.disconnect();
      }

   }

   public void onFusionRequestDiscarded(FusionRequest request) {
      if (request.getConnectionPrx() != null) {
         request.getConnectionPrx().packetProcessed(new byte[0]);
      } else {
         request.getConnection().packetProcessed(new byte[0]);
      }

   }

   public void onFusionRequestException(FusionRequest request, Exception e) {
      if (request.getConnectionPrx() != null) {
         request.getConnectionPrx().packetProcessed(new byte[0]);
      } else {
         request.getConnection().packetProcessed(new byte[0]);
      }

   }

   public void silentlyDropIncomingPackets(Current __current) {
      log.warn("No longer processing incoming packets for this connection for user [" + this.username + "] session id [" + this.sessionID + "] and remote address [" + this.remoteAddress + "]");
      this.packetProcessor.stopProcessing();
   }

   public void onDeviceModeChanged(DeviceModeType mode) {
   }

   private static enum Format {
      BINARY,
      XML,
      JSON;
   }
}
