package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class PacketProcessor implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PacketProcessor.class));
   private PacketProcessorCallback callback;
   private Gateway gateway;
   private ConnectionI connection;
   private Queue<FusionRequest> requestQueue = new LinkedList();
   private long lastThrottledExecution;
   private boolean stop;

   public PacketProcessor(PacketProcessorCallback callback, ConnectionI connection) {
      this.callback = callback;
      this.gateway = connection.getGateway();
      this.connection = connection;
   }

   public void run() {
      this.processIncomingQueue();
   }

   public void stopProcessing() {
      this.stop = true;
   }

   public int getQueueSize() {
      synchronized(this.requestQueue) {
         return this.requestQueue.size();
      }
   }

   public boolean queueFusionRequest(FusionRequest request) {
      synchronized(this.requestQueue) {
         if (this.stop) {
            return false;
         } else if (this.gateway.getMinIntervalBetweenPackets() > 0 && this.requestQueue.size() >= this.gateway.getMaxPacketsToBuffer()) {
            String infractionNote = this.logInfraction();
            Iterator i$ = this.requestQueue.iterator();

            while(i$.hasNext()) {
               FusionRequest r = (FusionRequest)i$.next();
               this.callback.onFusionRequestDiscarded(r);
            }

            this.requestQueue.clear();
            this.stop = true;
            UserPrx userPrx = this.connection.getUserPrx();
            if (userPrx != null) {
               userPrx.disconnectFlooder(infractionNote);
            }

            return false;
         } else {
            this.requestQueue.add(request);
            if (log.isDebugEnabled()) {
               log.debug("Request " + request.getType() + " from " + this.connection.getDisplayName() + " queued. " + this.requestQueue.size() + " requests on queue");
            }

            if (this.requestQueue.size() == 1) {
               this.schedule();
            }

            return true;
         }
      }
   }

   private void processIncomingQueue() {
      FusionRequest request;
      synchronized(this.requestQueue) {
         request = (FusionRequest)this.requestQueue.peek();
         if (request == null) {
            return;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug(String.format("Processing packet: %d\n", request.getType()));
      }

      try {
         List<String> discardedPackets = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DISCARDED_FUSION_PACKETS));
         String packetSimpleName = request.getSimpleName();
         if (!this.stop && !discardedPackets.contains(packetSimpleName)) {
            List<String> blockedPackets = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.BLOCKED_FUSION_PACKETS));
            FusionPacket[] responses;
            if (blockedPackets.contains(packetSimpleName)) {
               responses = (new FusionPktError(request.getTransactionId(), FusionPktError.Code.UNDEFINED, packetSimpleName + " disabled for now. Please try again later.")).toArray();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INSTRUMENT_BLOCKED_PACKETS)) {
                  this.gateway.getSampler().add(request.getSimpleName() + "_blocked", 0L);
               }
            } else {
               try {
                  long startTime = System.nanoTime();
                  responses = request.process(this.connection);
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INSTRUMENT_PACKETS)) {
                     this.gateway.getSampler().add(request.getSimpleName(), (System.nanoTime() - startTime) / 1000L);
                  }
               } catch (FusionRequestException var11) {
                  responses = (new FusionPktError(request.getTransactionId(), FusionPktError.Code.UNDEFINED, var11.getMessage())).toArray();
               }
            }

            List<String> delayedPackets = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DELAYED_FUSION_PACKETS));
            if (delayedPackets.contains(packetSimpleName)) {
               long delayInMilli = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.DEFAULT_RESPONSE_DELAY);
               request.setResponseDelay(delayInMilli);
            }

            if (request.getResponseDelay() > 0L) {
               this.gateway.scheduleTask(request.getThreadPool(), new PacketProcessor.DelayedNotificationTask(request, responses), request.getResponseDelay(), TimeUnit.MILLISECONDS);
            } else {
               this.callback.onFusionRequestProcessed(request, responses);
            }
         } else {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INSTRUMENT_DISCARDED_PACKETS)) {
               this.gateway.getSampler().add(request.getSimpleName() + "_discarded", 0L);
            }

            this.callback.onFusionRequestDiscarded(request);
         }
      } catch (Exception var12) {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.se606NpeFixEnabled.getValue()) {
            log.warn("Unexpected exception caught while processing packet " + request.getType() + " e=" + var12, var12);
         } else {
            log.warn("Unexpected exception caught while processing packet " + request.getType(), var12);
         }

         this.callback.onFusionRequestException(request, var12);
      }

      synchronized(this.requestQueue) {
         this.requestQueue.poll();
         this.schedule();
      }
   }

   private void schedule() {
      FusionRequest request = (FusionRequest)this.requestQueue.peek();
      if (request != null) {
         try {
            long interval = (long)this.gateway.getMinIntervalBetweenPackets();
            if (interval != 0L && request.throttlingRequired()) {
               if (System.currentTimeMillis() - this.lastThrottledExecution > interval) {
                  this.gateway.executeTask(request.getThreadPool(), this);
                  this.lastThrottledExecution = System.currentTimeMillis();
               } else {
                  this.gateway.scheduleTask(request.getThreadPool(), this, interval, TimeUnit.MILLISECONDS);
                  this.lastThrottledExecution = System.currentTimeMillis() + interval;
               }
            } else {
               this.gateway.executeTask(request.getThreadPool(), this);
            }
         } catch (RejectedExecutionException var4) {
            FusionPacket error = request.getErrorPacket("Server too busy");
            if (error != null) {
               this.callback.onFusionRequestProcessed(request, error.toArray());
            }
         }

      }
   }

   private String logInfraction() {
      StringBuilder builder = new StringBuilder();
      Iterator i$ = this.requestQueue.iterator();

      while(i$.hasNext()) {
         FusionRequest requestOnQueue = (FusionRequest)i$.next();
         if (builder.length() == 0) {
            builder.append(requestOnQueue.getType());
         } else {
            builder.append(",").append(requestOnQueue.getType());
         }
      }

      String infractionNote = this.connection.getDisplayName() + " exceeded throttling rate. Packets [" + builder.toString() + "]";
      log.warn(infractionNote);
      return infractionNote;
   }

   private class DelayedNotificationTask implements Runnable {
      private FusionRequest request;
      private FusionPacket[] responses;

      public DelayedNotificationTask(FusionRequest request, FusionPacket[] responses) {
         this.request = request;
         this.responses = responses;
      }

      public void run() {
         PacketProcessor.this.callback.onFusionRequestProcessed(this.request, this.responses);
      }
   }
}
