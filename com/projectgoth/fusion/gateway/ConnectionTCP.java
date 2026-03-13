package com.projectgoth.fusion.gateway;

import Ice.Current;
import com.projectgoth.fusion.chatsync.ChatSyncStorageExecutor;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.chatsync.ServerGeneratedReceivedEventPusher;
import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.DeviceModeType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.packet.FusionPacketFactory;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;

public class ConnectionTCP extends ConnectionI implements PacketProcessorCallback {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ConnectionTCP.class));
   private static final int READ_BUFFER_SIZE = 1024;
   private static final int MAX_READ_BUFFER_SIZE = 358400;
   private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
   private ByteBuffer sendBuffer;
   private PacketProcessor packetProcessor = new PacketProcessor(this, this);
   private Queue<FusionPacket> outgoingQueue = new LinkedList();
   private AtomicBoolean writing = new AtomicBoolean(false);
   private boolean sessionTerminated;

   public ConnectionTCP(Gateway gateway, SocketChannel channel, GatewayContext gatewayContext) {
      super(gateway, channel, gatewayContext);
   }

   public Gateway.ServerType getServerType() {
      return Gateway.ServerType.TCP;
   }

   public void onSessionTerminated() {
      super.onSessionTerminated();
      this.sessionTerminated = true;
   }

   public void onChannelReadable() {
      try {
         if (this.readBuffer.remaining() == 0) {
            this.readBuffer = ByteBufferHelper.adjustSize(this.readBuffer, 1024, 358400, 2.0D);
         }

         int bytesRead = this.channel.read(this.readBuffer);
         if (log.isDebugEnabled()) {
            log.debug("Read " + bytesRead + " bytes from " + this.getDisplayName());
         }

         if (bytesRead == -1) {
            if (log.isDebugEnabled()) {
               log.debug("Terminating connection " + this.getDisplayName() + " - Socket closed (EOF)");
            }

            this.onSessionTerminated();
            this.disconnect();
            return;
         }

         if (bytesRead == 0) {
            log.info("onChannelReadable called with zero bytes on the channel??");
         }

         this.readBuffer.flip();

         try {
            while(FusionPacket.haveFusionPacket(this.readBuffer)) {
               FusionRequest request = FusionPacketFactory.readSpecificRequest(this.readBuffer);
               if (request == null) {
                  throw new Exception("Failed to convert Fusion packet into a request");
               }

               if (log.isDebugEnabled()) {
                  log.debug("Received packet " + request.getType() + " from " + this.getDisplayName() + "\n" + request.toString() + "\n");
               }

               this.accessed();
               this.packetProcessor.queueFusionRequest(request);
            }
         } finally {
            this.readBuffer.compact();
         }

         this.gateway.registerConnection(this, 1);
      } catch (BufferOverflowException var11) {
         log.warn("Terminating connection " + this.getDisplayName() + " - Read buffer is full");
         this.onSessionTerminated();
         this.disconnect();
      } catch (IOException var12) {
         log.warn("Terminating connection " + this.getDisplayName() + " - " + var12.getMessage(), var12);
         this.onSessionTerminated();
         this.disconnect();
      } catch (FusionException var13) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se493WebSocketsEnabled.getValue()) {
            log.warn("Terminating connection " + this.getDisplayName() + " - Trying to make invalid connection to gateway", var13);
            this.disconnect();
         }
      } catch (Exception var14) {
         log.error("Terminating connection " + this.getDisplayName() + " - " + var14.getMessage(), var14);
         this.onSessionTerminated();
         this.disconnect();
      }

   }

   public void onChannelWritable() {
      synchronized(this) {
         try {
            if (this.sendBuffer != null && this.sendBuffer.hasRemaining()) {
               this.channel.write(this.sendBuffer);
               if (this.sendBuffer.hasRemaining()) {
                  this.gateway.registerConnection(this, 4);
                  return;
               }
            }

            FusionPacket pkt;
            synchronized(this.outgoingQueue) {
               pkt = (FusionPacket)this.outgoingQueue.poll();
            }

            while(pkt != null) {
               if (log.isDebugEnabled()) {
                  log.debug("Sending packet " + pkt.getType() + " to " + this.getDisplayName() + "\n" + pkt.toString() + "\n");
               }

               this.sendBuffer = ByteBuffer.wrap(pkt.toByteArray());
               this.channel.write(this.sendBuffer);
               this.sendServerGeneratedReceivedEventIfNeeded(pkt);
               if (this.sendBuffer.hasRemaining()) {
                  this.gateway.registerConnection(this, 4);
                  return;
               }

               synchronized(this.outgoingQueue) {
                  pkt = (FusionPacket)this.outgoingQueue.poll();
               }
            }

            this.writing.set(false);
         } catch (Exception var9) {
            if (log.isDebugEnabled()) {
               log.debug("Terminating " + this.getDisplayName() + " - " + var9.getMessage());
            }

            this.onSessionTerminated();
            this.disconnect();
            this.writing.set(false);
         }

      }
   }

   private void sendServerGeneratedReceivedEventIfNeeded(FusionPacket pkt) {
      try {
         if ((Boolean)SystemPropertyEntities.MessageStatusEventSettings.Cache.enabled.getValue() && pkt.getType() == 500 && ((FusionPktMessage)pkt).getMessageTypeEnum() == MessageType.FUSION && ((FusionPktMessage)pkt).isIndividualMessage() && !MessageStatusEvent.isClientMessageStatusEventCapable(this.deviceType, this.clientVersion)) {
            ServerGeneratedReceivedEventPusher receivedEvent = new ServerGeneratedReceivedEventPusher((FusionPktMessage)pkt, Enums.MessageStatusEventTypeEnum.RECEIVED, true, this.gatewayContext.getRegistryPrx());
            ChatSyncStorageExecutor.getInstance().scheduleStorage(receivedEvent);
         }
      } catch (Exception var3) {
         log.error("Exception processing server-generated RECEIVED event, event sender=" + this.getUsername() + ": e=" + var3, var3);
      }

   }

   public void onCreatingSession() {
      super.onCreatingSession();
      this.addConnectionToIceAdaptor(true);
   }

   public void sendFusionPacket(FusionPacket packet) throws FusionException {
      try {
         if (!this.channel.isOpen()) {
            throw new Exception("Connection closed");
         } else {
            if (this.isDeviceAwake()) {
               synchronized(this.outgoingQueue) {
                  this.outgoingQueue.add(packet);
               }

               if (this.writing.compareAndSet(false, true)) {
                  this.onChannelWritable();
               }
            } else {
               this.queueForDelivery(new FusionPacket[]{packet});
            }

         }
      } catch (Exception var5) {
         throw new FusionException(var5.getMessage());
      }
   }

   public void onFusionRequestProcessed(FusionRequest request, FusionPacket[] responses) {
      if (responses != null) {
         if (this.isDeviceAwake()) {
            this.streamPackets(responses);
         } else {
            this.queueForDelivery(responses);
         }
      }

      if (this.sessionTerminated) {
         this.disconnect();
      }

   }

   private void streamPackets(FusionPacket[] packets) {
      synchronized(this.outgoingQueue) {
         this.outgoingQueue.addAll(Arrays.asList(packets));
      }

      this.onChannelWritable();
   }

   public void onFusionRequestDiscarded(FusionRequest request) {
   }

   public void onFusionRequestException(FusionRequest request, Exception e) {
      this.onSessionTerminated();
      this.disconnect();
   }

   public boolean processPacket(ConnectionPrx requestingConnection, byte[] packet, Current __current) throws FusionException {
      return false;
   }

   public void packetProcessed(byte[] result, Current __current) {
   }

   public void silentlyDropIncomingPackets(Current __current) {
      log.warn("No longer processing incoming packets for this connection for user [" + this.username + "]");
      this.packetProcessor.stopProcessing();
   }

   public synchronized void onDeviceModeChanged(DeviceModeType mode) {
      switch(mode) {
      case AWAKE:
         if (log.isDebugEnabled()) {
            log.debug("Device " + this.getDisplayName() + " woke up...");
         }
         break;
      case SLEEP:
         if (log.isDebugEnabled()) {
            log.debug("Device " + this.getDisplayName() + " went into sleep mode...");
         }
      }

   }

   private void queueForDelivery(FusionPacket[] packets) {
      FusionPacket[] arr$ = packets;
      int len$ = packets.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         FusionPacket pkt = arr$[i$];
         if (pkt != null) {
            try {
               if (!packetTypeIsBlockedInSleepMode(pkt.getType())) {
                  if (500 != pkt.getType() || ((FusionPktMessage)pkt).isPrivateOrGroupChatMessage()) {
                     this.streamPackets(new FusionPacket[]{pkt});
                  }
               } else if (log.isDebugEnabled()) {
                  log.debug("Packet " + pkt.getType() + " dropped for " + this.getDisplayName() + "...");
               }
            } catch (Exception var7) {
               log.warn("Failed to send packet: " + pkt.getType());
            }
         }
      }

   }
}
