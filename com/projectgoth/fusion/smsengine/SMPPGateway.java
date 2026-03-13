package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.SMSGatewayData;
import com.projectgoth.fusion.data.SystemSMSData;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SMPPGateway extends SMSGateway implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SMPPGateway.class));
   private static final int CONNECT_TIMEOUT = 10000;
   private static final int SEND_TIMEOUT = 5000;
   private static final int RECONNECTION_INTERVAL = 30000;
   private static final int HEART_BEAT_INTERVAL = 30000;
   private static final String DEFAULT_CHARSET = "UTF-8";
   private static MOSMSProcessor moSMSProcessor = new MOSMSProcessor();
   private Socket socket;
   private int sequence;
   private volatile boolean ready;
   private volatile boolean stop;
   private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
   private Map<Integer, SMPPPacket> packetLocker = new ConcurrentHashMap();

   public SMPPGateway(SMSGatewayData gatewayData) {
      super(gatewayData);
      this.scheduler.execute(this);
      this.scheduler.scheduleWithFixedDelay(new SMPPGateway.HeartBeat(), 10000L, 30000L, TimeUnit.MILLISECONDS);
   }

   public void run() {
      while(!this.stop) {
         try {
            this.connect();
            this.bind();
            this.ready = true;

            while(!this.stop) {
               this.onSMPPPacket(this.readSMPPPacket());
            }
         } catch (Exception var11) {
            log.warn("Exception occured in SMPP gateway " + this.getID(), var11);
         } finally {
            if (this.ready) {
               this.ready = false;
               this.unbind();
            }

            this.disconnect();

            try {
               Thread.sleep(30000L);
            } catch (Exception var10) {
            }

         }
      }

   }

   private void connect() throws IOException {
      log.debug("Connecting to SMPP gateway " + this.getID() + " at " + this.gatewayData.url + ":" + this.gatewayData.port);
      this.socket = new Socket();
      this.socket.connect(new InetSocketAddress(this.gatewayData.url, this.gatewayData.port), 10000);
      this.packetLocker.clear();
   }

   private void disconnect() {
      log.debug("Disconnecting SMPP gateway " + this.getID());

      try {
         this.socket.close();
      } catch (Exception var2) {
      }

   }

   private void bind() throws IOException {
      SMPPPacket pktBind;
      if (this.gatewayData.type == SMSGatewayData.TypeEnum.SMPP_TRANSMITTER) {
         log.debug("Binding as transmitter on SMPP gateway " + this.getID());
         pktBind = new SMPPPacket(SMPPID.BIND_TRANSMITTER);
      } else {
         log.debug("Binding as transceiver on SMPP gateway " + this.getID());
         pktBind = new SMPPPacket(SMPPID.BIND_TRANSCEIVER);
      }

      pktBind.put(this.gatewayData.usernameParam).put(this.gatewayData.passwordParam).put("").put(SMPPPacket.INTERFACE_VERSION).put((byte)0).put((byte)0).put("");
      this.sendSMPPPacket(pktBind, false);
      SMPPPacket pktResponse = this.readSMPPPacket();
      if (pktBind.getId() == SMPPID.BIND_TRANSMITTER && pktResponse.getId() != SMPPID.BIND_TRANSMITTER_RESP) {
         throw new IOException("Unexpected response command ID " + pktResponse.getId() + " while binding transmitter");
      } else if (pktBind.getId() == SMPPID.BIND_TRANSCEIVER && pktResponse.getId() != SMPPID.BIND_TRANSCEIVER_RESP) {
         throw new IOException("Unexpected response command ID " + pktResponse.getId() + " while binding transceiver");
      } else if (pktResponse.getStatus() != SMPPStatus.ESME_ROK) {
         throw new IOException(pktBind.getId() + " failed. SMPP status " + pktResponse.getStatus() + ": " + pktResponse.getStatus().description());
      }
   }

   private void unbind() {
      log.debug("Unbinding on SMPP gateway " + this.getID());

      try {
         this.sendSMPPPacket(new SMPPPacket(SMPPID.UNBIND), false);
      } catch (Exception var2) {
      }

   }

   private SMPPPacket readSMPPPacket() throws IOException {
      SMPPPacket packet = new SMPPPacket(this.socket.getInputStream());
      log.debug("Gateway " + this.getID() + " Received\t" + packet);
      return packet;
   }

   public SMPPPacket sendSMPPPacket(SMPPPacket packet, boolean waitForResponse) throws IOException {
      synchronized(packet) {
         OutputStream out = this.socket.getOutputStream();
         synchronized(out) {
            if (packet.getSequence() == 0) {
               packet.setSequence(++this.sequence);
            }

            this.socket.getOutputStream().write(packet.toByteArray());
         }

         log.debug("Gateway " + this.getID() + " Sent\t\t" + packet);
         if (!waitForResponse) {
            return null;
         } else {
            this.packetLocker.put(packet.getSequence(), packet);
            boolean interrupted = false;

            try {
               packet.wait(5000L);
            } catch (InterruptedException var8) {
               interrupted = true;
            }

            SMPPPacket pktResponse = (SMPPPacket)this.packetLocker.remove(packet.getSequence());
            if (pktResponse == packet) {
               if (interrupted) {
                  throw new IOException("Thread interrupted while waiting for response sequence number " + packet.getSequence());
               } else {
                  throw new IOException("Timeout while while waiting for response sequence number " + packet.getSequence());
               }
            } else {
               return pktResponse;
            }
         }
      }
   }

   private void onSMPPPacket(SMPPPacket packet) {
      SMPPID id = packet.getId();
      if (id.isResponse()) {
         SMPPPacket originalPacket = (SMPPPacket)this.packetLocker.get(packet.getSequence());
         if (originalPacket != null) {
            synchronized(originalPacket) {
               this.packetLocker.put(packet.getSequence(), packet);
               originalPacket.notifyAll();
            }
         }
      }

      switch(id) {
      case ENQUIRE_LINK:
         this.onEnquireLink(packet);
         break;
      case DELIVER_SM:
         this.onDeliverSM(packet);
      }

   }

   private void onEnquireLink(SMPPPacket packet) {
      SMPPPacket responsePacket = new SMPPPacket(SMPPID.ENQUIRE_LINK_RESP);
      responsePacket.setSequence(packet.getSequence());

      try {
         this.sendSMPPPacket(responsePacket, false);
      } catch (Exception var4) {
         log.warn("Unable to acknowledge ENQUIRE_LINK", var4);
      }

   }

   private void onDeliverSM(SMPPPacket packet) {
      try {
         SMPPBody body = packet.getBody();
         body.getCString();
         body.getByteArray(2);
         String source = body.getCString();
         body.getByteArray(2);
         String destination = body.getCString();
         body.getByteArray(9);
         int messageLen = body.getIntFromUnsignedByte();
         String message = body.getString(messageLen);
         log.info("DELIVER_SM gateway = " + this.getID() + ", source = " + source + ", destination = " + destination + ". " + message);
         moSMSProcessor.process(this, packet.getSequence(), source, destination, message);
      } catch (Exception var7) {
         log.warn("Expception occured while prossing DELIVER_SM", var7);
      }

   }

   public DispatchStatus dispatchMessage(SMSMessage message) {
      DispatchStatus dispatchStatus = new DispatchStatus(DispatchStatus.StatusEnum.TRY_OTHER_GATEWAYS, message);

      try {
         if (!this.ready) {
            return dispatchStatus;
         }

         String destination = message.getDestination();
         byte sourceTON = 0;
         byte sourceNPI = 0;
         byte destinationTON = 0;
         byte destinationNPI = 0;
         byte registeredDelivery = 0;
         if (this.gatewayData.name.toLowerCase().equals("smart telecom")) {
            sourceTON = 1;
            sourceNPI = 1;
            destinationTON = 49;
            destinationNPI = 2;
            registeredDelivery = 1;
            if (message.useSourceAsSourceAddress() && message.getIDDCode() != 91) {
               return dispatchStatus;
            }

            dispatchStatus.source = GenericSourceNumberPool.getSourceNumber("smarttelcom");
            if (this.gatewayData.iddPrefix != null) {
               destination = this.gatewayData.iddPrefix + destination;
               log.debug("SMPP gateway " + this.gatewayData.id + " modified destination from " + message.getDestination() + " to " + destination);
            }
         }

         if (this.gatewayData.name.toLowerCase().contains("iris")) {
            if (message.useSourceAsSourceAddress()) {
               return dispatchStatus;
            }

            dispatchStatus.source = GenericSourceNumberPool.getSourceNumber("iris");
            if (this.gatewayData.iddPrefix != null && !destination.startsWith("1")) {
               destination = this.gatewayData.iddPrefix + destination;
               log.debug("SMPP gateway " + this.gatewayData.id + " modified destination from " + message.getDestination() + " to " + destination);
            }
         } else if (this.gatewayData.name.toLowerCase().contains("banglalink")) {
            if (message.useSourceAsSourceAddress()) {
               return dispatchStatus;
            }

            dispatchStatus.source = "7575";
            destination = destination.substring(2);
         }

         byte[] bytesToSend = message.getMessageText().getBytes("UTF-8");
         SMPPPacket pktSubmit = new SMPPPacket(SMPPID.SUBMIT_SM);
         pktSubmit.put("").put(sourceTON).put(sourceNPI).put(dispatchStatus.source).put(destinationTON).put(destinationNPI).put(destination).put((byte)0).put((byte)0).put((byte)0).put("").put("").put(registeredDelivery).put((byte)0).put((byte)0).put((byte)0).put((byte)bytesToSend.length).put(bytesToSend);
         SMPPPacket pktResponse = this.sendSMPPPacket(pktSubmit, true);
         if (pktResponse.getId() != SMPPID.SUBMIT_SM_RESP) {
            dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
            dispatchStatus.failedReason = "Unexpected response command ID " + pktResponse.getId() + " while sending message";
            log.warn("Gateway " + this.gatewayData.id + ". Failed to dispatch " + message.getShortDescription() + " - " + dispatchStatus.failedReason);
         } else if (pktResponse.getStatus() == SMPPStatus.ESME_RINVSRCADR) {
            log.warn("Gateway " + this.gatewayData.id + ". Failed to dispatch " + message.getShortDescription() + " - Invalid source address " + dispatchStatus.source);
         } else if (pktResponse.getStatus() != SMPPStatus.ESME_ROK) {
            dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
            dispatchStatus.failedReason = pktResponse.getStatus().description();
            log.warn("Gateway " + this.gatewayData.id + ". Failed to dispatch " + message.getShortDescription() + " - " + dispatchStatus.failedReason);
         } else {
            SMPPBody body = pktResponse.getBody();
            String transactionID = body.getCString();
            dispatchStatus.status = DispatchStatus.StatusEnum.SUCCEEDED;
            dispatchStatus.transactionID = transactionID;
            dispatchStatus.billed = this.gatewayData.deliveryReporting == null || !this.gatewayData.deliveryReporting;
            log.info("Gateway " + this.gatewayData.id + ". Successfully dispatched " + message.getShortDescription() + ". Transaction ID = " + dispatchStatus.transactionID);
         }
      } catch (UnsupportedEncodingException var14) {
         dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
         dispatchStatus.failedReason = "Unsupported encoding character set UTF-8";
      } catch (IOException var15) {
         log.warn("IOException occured in SMPP gateway " + this.getID(), var15);
      } catch (Exception var16) {
         log.warn("Unexpected exception occured in SMPP gateway " + this.getID(), var16);
         dispatchStatus.status = DispatchStatus.StatusEnum.FAILED;
         dispatchStatus.failedReason = var16.getMessage();
      }

      return dispatchStatus;
   }

   public static void main(String[] args) {
      try {
         if (args.length == 0) {
            System.out.println("SMPPGateway Type Name Server Port Username Password Source Destination Message");
            return;
         }

         log.setLevel(Level.DEBUG);
         SMSGatewayData gatewayData = new SMSGatewayData();
         gatewayData.id = 1;
         if ("1".equals(args[0])) {
            gatewayData.type = SMSGatewayData.TypeEnum.SMPP_TRANSMITTER;
         } else {
            gatewayData.type = SMSGatewayData.TypeEnum.SMPP_TRANSCEIVER;
         }

         gatewayData.name = args[1];
         gatewayData.url = args[2];
         gatewayData.port = Integer.valueOf(args[3]);
         gatewayData.iddPrefix = null;
         gatewayData.usernameParam = args[4];
         gatewayData.passwordParam = args[5];
         SMSGateway gateway = new SMPPGateway(gatewayData);
         Thread.sleep(5000L);
         String[] arr$ = args[7].split(";");
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String destination = arr$[i$];
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.id = 1;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.BUZZ;
            systemSMSData.username = "koko";
            systemSMSData.source = args[6];
            systemSMSData.destination = destination;
            systemSMSData.IDDCode = 1;
            systemSMSData.messageText = args[8];
            List<SMSMessage> messages = SMSMessage.parse(systemSMSData);
            Iterator i$ = messages.iterator();

            while(i$.hasNext()) {
               SMSMessage message = (SMSMessage)i$.next();
               DispatchStatus status = gateway.dispatchMessage(message);
               if (status.status == DispatchStatus.StatusEnum.SUCCEEDED) {
                  log.info(status.status + ": " + status.transactionID + ", source = " + status.source);
               } else {
                  log.info(status.status + ": " + status.failedReason + ", source = " + status.source);
               }
            }

            Thread.sleep(2000L);
         }
      } catch (Exception var12) {
         var12.printStackTrace();
      }

   }

   private class HeartBeat implements Runnable {
      private HeartBeat() {
      }

      public void run() {
         try {
            if (SMPPGateway.this.ready) {
               SMPPGateway.this.sendSMPPPacket(new SMPPPacket(SMPPID.ENQUIRE_LINK), false);
            }
         } catch (Exception var2) {
         }

      }

      // $FF: synthetic method
      HeartBeat(Object x1) {
         this();
      }
   }
}
