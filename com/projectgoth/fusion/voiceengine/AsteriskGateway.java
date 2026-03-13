package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.VoiceGatewayData;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.naming.AuthenticationException;
import org.apache.log4j.Logger;

public class AsteriskGateway implements AsteriskListener {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AsteriskGateway.class));
   private static final boolean USING_ASTERISK_1_4_00_NEWER = false;
   private static final String STATIC_CID_ALEG = "16505239916";
   private static int CONNECTION_RETRY_INTERVAL = 5000;
   private Map<String, CallData> callsToReconcile = new HashMap();
   private Map<String, CallData> callsInProgress = new HashMap();
   private AsteriskConnection connection;
   private CallMakerI callMaker;
   private VoiceGatewayData gatewayData;
   private long actionID;
   private boolean tryReconnect;
   private int variableFormat;

   public AsteriskGateway(VoiceGatewayData gatewayData, int variableFormat) {
      this.gatewayData = gatewayData;
      this.variableFormat = variableFormat;
      this.connection = new AsteriskConnection(this);
   }

   public void connect() throws AuthenticationException, IOException {
      log.debug("Connecting to gateway " + this.gatewayData.id + ", " + this.gatewayData.server + ".");
      this.connection.connect(this.gatewayData.server, this.gatewayData.port);
      this.connection.login(this.gatewayData.username, this.gatewayData.password);
      this.tryReconnect = true;
      Set<CallData> callsToAdd = new HashSet();
      synchronized(this.callsInProgress) {
         callsToAdd.addAll(this.callsInProgress.values());
         this.callsInProgress.clear();
      }

      synchronized(this.callsToReconcile) {
         Iterator i$ = callsToAdd.iterator();

         while(true) {
            if (!i$.hasNext()) {
               break;
            }

            CallData callData = (CallData)i$.next();
            this.linkCallData(this.callsToReconcile, callData);
         }
      }

      try {
         this.requestStatus();
      } catch (Exception var6) {
      }

   }

   public void disconnect() {
      this.tryReconnect = false;
      this.connection.disconnect();
   }

   public void ping() throws IOException {
      AsteriskCommand command = new AsteriskCommand(AsteriskCommand.Type.ACTION, "Ping");
      this.connection.sendCommand(command);
   }

   public void requestStatus() throws IOException {
      AsteriskCommand command = new AsteriskCommand(AsteriskCommand.Type.ACTION, "Status");
      command.setProperty("ActionID", "-1");
      this.connection.sendCommand(command);
   }

   public void setGlobalVariable(String name, String value) throws IOException {
      this.setChanVariable((String)null, name, value);
   }

   public void setChanVariable(String channel, String name, String value) throws IOException {
      AsteriskCommand command = new AsteriskCommand(AsteriskCommand.Type.ACTION, "Setvar");
      if (channel != null && channel.length() > 0) {
         command.setProperty("Channel", channel);
      }

      command.setProperty("Variable", name);
      command.setProperty("Value", value);
      this.connection.sendCommand(command);
   }

   public void setCallMaker(CallMakerI callMaker) {
      this.callMaker = callMaker;
   }

   public VoiceGatewayData getData() {
      return this.gatewayData;
   }

   public Collection<CallData> getCallsInProgress() {
      synchronized(this.callsInProgress) {
         return this.callsInProgress.values();
      }
   }

   public void addCallInProgress(CallData callData) {
      synchronized(this.callsInProgress) {
         this.linkCallData(this.callsInProgress, callData);
      }
   }

   public CallData requestCallback(CallData callData, boolean retryLegA, boolean retryLegB) {
      try {
         if (callData == null) {
            throw new Exception("Call data is missing or invalid");
         }

         if (retryLegA || retryLegB) {
            if (retryLegA) {
               log.info("CALLBACK RETRY LEG A. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", UniqueID = " + callData.uniqueID + ", FailReason = " + callData.failReason + ", FailReasonCode = " + callData.failReasonCode + ", SourceChannel = " + callData.sourceChannel + ", SourceDuration = " + callData.sourceDuration + ", SourceHangupReason = " + callData.sourceHangupReason + ", SourceHangupCode = " + callData.sourceHangupCode + ", DestinationChannel = " + callData.destinationChannel + ", DestinationDuration = " + callData.destinationDuration + ", DestinationHangupReason = " + callData.destinationHangupReason + ", DestinationHangupCode = " + callData.destinationHangupCode + ".");
            } else if (retryLegB) {
               log.info("CALLBACK RETRY LEG B. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", UniqueID = " + callData.uniqueID + ", FailReason = " + callData.failReason + ", FailReasonCode = " + callData.failReasonCode + ", SourceChannel = " + callData.sourceChannel + ", SourceDuration = " + callData.sourceDuration + ", SourceHangupReason = " + callData.sourceHangupReason + ", SourceHangupCode = " + callData.sourceHangupCode + ", DestinationChannel = " + callData.destinationChannel + ", DestinationDuration = " + callData.destinationDuration + ", DestinationHangupReason = " + callData.destinationHangupReason + ", DestinationHangupCode = " + callData.destinationHangupCode + ".");
            }
         }

         if (!this.connection.isConnected()) {
            throw new Exception("Server offline for gateway ID " + this.gatewayData.id + ".");
         }

         if (!retryLegB) {
            synchronized(this.callsInProgress) {
               callData = this.fillCallDataID(this.callsInProgress, callData);
               if (this.findCallData(this.callsInProgress, callData) != null) {
                  callData.status = CallData.StatusEnum.FAILED;
                  callData.failReason = "Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is currently in use";
                  log.warn("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is currently in use.");
                  return callData;
               }

               this.linkCallData(this.callsInProgress, callData);
            }
         } else {
            synchronized(this.callsInProgress) {
               if (this.findCallData(this.callsInProgress, callData) == null) {
                  callData.status = CallData.StatusEnum.FAILED;
                  callData.failReason = "Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is not currently in use";
                  log.warn("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is not currently in use.");
                  return callData;
               }

               this.unlinkCallData(this.callsInProgress, callData);
               callData.destinationChannel = null;
               callData.destinationChannelName = null;
               callData.destinationHangupCode = null;
               callData.destinationHangupReason = null;
               this.linkCallData(this.callsInProgress, callData);
            }
         }

         log.debug("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " (Unique ID = " + callData.uniqueID + ", source channel = " + callData.sourceChannel + ").");
         long maxDurationInMS = (long)callData.maxDuration * 1000L - (long)this.gatewayData.connectionTimeout;
         if (maxDurationInMS < 1000L) {
            throw new Exception("Insufficient credits");
         }

         String bLegCallerID;
         if (callData.type == CallData.TypeEnum.MIDLET_ANONYMOUS_CALLBACK) {
            bLegCallerID = "16505239916";
         } else {
            bLegCallerID = callData.source;
         }

         callData.destinationProvider = !retryLegB ? callData.destinationFirstProvider : callData.destinationNextProvider;
         callData.destinationDialCommand = !retryLegB ? callData.destinationFirstDialCommand : callData.destinationNextDialCommand;
         String srcDialCommand = callData.sourceDialCommand != null ? callData.sourceDialCommand.replaceAll("%n", callData.source != null ? callData.source : "") : "";
         String destDialCommand = callData.destinationDialCommand != null ? callData.destinationDialCommand.replaceAll("%n", callData.destination != null ? callData.destination : "") : "";
         String destDialNextCommand = callData.destinationNextDialCommand != null ? callData.destinationNextDialCommand.replaceAll("%n", callData.destination != null ? callData.destination : "") : "";
         if (retryLegB) {
            throw new Exception("Cannot execute retry");
         }

         AsteriskCommand command = new AsteriskCommand(AsteriskCommand.Type.ACTION, "Originate");
         command.setProperty("Channel", srcDialCommand);
         command.setProperty("Context", this.gatewayData.callbackContext);
         command.setProperty("Exten", this.gatewayData.callbackExtension);
         command.setProperty("Priority", "1");
         command.setProperty("Timeout", String.valueOf(this.gatewayData.connectionTimeout));
         if (callData.sourceType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
            command.setProperty("CallerID", callData.source);
         } else {
            command.setProperty("CallerID", "16505239916");
         }

         command.setProperty("Account", callData.accountID);
         command.setProperty("AccountCode", callData.accountID);
         command.setProperty("ActionID", callData.actionID);
         command.setProperty("Async", "yes");
         String variable;
         if (this.variableFormat == 1) {
            variable = "source=" + bLegCallerID + "\r\nVariable: callid=" + (callData.id != null ? callData.id.toString() : 0) + "\r\nVariable: callmode=normal" + "\r\nVariable: first_attempt=true" + "\r\nVariable: last_attempt=false" + "\r\nVariable: destination=" + destDialCommand + "\r\nVariable: destination_provider=" + callData.destinationProvider + "\r\nVariable: destination_next=" + destDialNextCommand + "\r\nVariable: destination_next_provider=" + callData.destinationNextProvider + "\r\nVariable: timeout=" + maxDurationInMS + "\r\nVariable: timeout_warning=" + this.gatewayData.timeoutWarning + "\r\nVariable: timeout_warning_repeat=" + this.gatewayData.timeoutWarningRepeat;
         } else {
            variable = "source=" + bLegCallerID + "|callid=" + (callData.id != null ? callData.id.toString() : 0) + "|callmode=normal" + "|first_attempt=true" + "|last_attempt=false" + "|destination=" + destDialCommand + "|destination_provider=" + callData.destinationProvider + "|destination_next=" + destDialNextCommand + "|destination_next_provider=" + callData.destinationNextProvider + "|timeout=" + maxDurationInMS + "|timeout_warning=" + this.gatewayData.timeoutWarning + "|timeout_warning_repeat=" + this.gatewayData.timeoutWarningRepeat;
         }

         command.setProperty("Variable", variable);
         if (!this.connection.isConnected()) {
            throw new Exception("Server offline for gateway ID " + this.gatewayData.id + ".");
         }

         this.connection.sendCommand(command);
         synchronized(this.callsInProgress) {
            this.unlinkCallData(this.callsInProgress, callData);
            callData.status = CallData.StatusEnum.IN_PROGRESS;
            callData.failReasonCode = null;
            callData.failReason = null;
            callData.sourceChannel = null;
            callData.sourceChannelName = null;
            callData.sourceHangupCode = null;
            callData.sourceHangupReason = null;
            callData.destinationChannel = null;
            callData.destinationChannelName = null;
            callData.destinationHangupCode = null;
            callData.destinationHangupReason = null;
            this.linkCallData(this.callsInProgress, callData);
         }

         try {
            this.requestStatus();
         } catch (Exception var16) {
         }
      } catch (Exception var20) {
         if (!retryLegB) {
            synchronized(this.callsInProgress) {
               this.unlinkCallData(this.callsInProgress, callData);
            }
         }

         callData.status = CallData.StatusEnum.FAILED;
         callData.failReason = var20.getMessage();
      }

      return callData;
   }

   public CallData requestCall(CallData callData, boolean retryLegB) {
      try {
         if (callData == null) {
            throw new Exception("Call data is missing or invalid");
         }

         if (retryLegB && retryLegB) {
            log.info("CALL RETRY LEG B. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", UniqueID = " + callData.uniqueID + ", FailReason = " + callData.failReason + ", FailReasonCode = " + callData.failReasonCode + ", SourceChannel = " + callData.sourceChannel + ", SourceDuration = " + callData.sourceDuration + ", SourceHangupReason = " + callData.sourceHangupReason + ", SourceHangupCode = " + callData.sourceHangupCode + ", DestinationChannel = " + callData.destinationChannel + ", DestinationDuration = " + callData.destinationDuration + ", DestinationHangupReason = " + callData.destinationHangupReason + ", DestinationHangupCode = " + callData.destinationHangupCode + ".");
         }

         if (!this.connection.isConnected()) {
            throw new Exception("Server offline for gateway ID " + this.gatewayData.id + ".");
         }

         if (!retryLegB) {
            synchronized(this.callsInProgress) {
               callData = this.fillCallDataID(this.callsInProgress, callData);
               if (this.findCallData(this.callsInProgress, callData) != null) {
                  callData.status = CallData.StatusEnum.FAILED;
                  callData.failReason = "Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is currently in use";
                  log.warn("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is currently in use.");
                  return callData;
               }

               this.linkCallData(this.callsInProgress, callData);
            }
         } else {
            synchronized(this.callsInProgress) {
               if (this.findCallData(this.callsInProgress, callData) != null) {
                  callData.status = CallData.StatusEnum.FAILED;
                  callData.failReason = "Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is not currently in use";
                  log.warn("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " is not currently in use.");
                  return callData;
               }

               this.unlinkCallData(this.callsInProgress, callData);
               callData.destinationChannel = null;
               callData.destinationChannelName = null;
               callData.destinationHangupCode = null;
               callData.destinationHangupReason = null;
               this.linkCallData(this.callsInProgress, callData);
            }
         }

         log.debug("Call ID " + (callData.id != null ? callData.id.toString() : 0) + " (Unique ID = " + callData.uniqueID + ", source channel = " + callData.sourceChannel + ").");
         if (retryLegB) {
            throw new Exception("Cannot execute retry");
         }

         if (!this.connection.isConnected()) {
            throw new Exception("Server offline for gateway ID " + this.gatewayData.id + ".");
         }

         try {
            this.requestStatus();
         } catch (Exception var10) {
         }

         synchronized(this.callsInProgress) {
            this.unlinkCallData(this.callsInProgress, callData);
            callData.status = CallData.StatusEnum.IN_PROGRESS;
            callData.failReasonCode = null;
            callData.failReason = null;
            callData.sourceHangupCode = null;
            callData.sourceHangupReason = null;
            callData.destinationChannel = null;
            callData.destinationChannelName = null;
            callData.destinationHangupCode = null;
            callData.destinationHangupReason = null;
            this.linkCallData(this.callsInProgress, callData);
         }
      } catch (Exception var13) {
         if (!retryLegB) {
            synchronized(this.callsInProgress) {
               this.unlinkCallData(this.callsInProgress, callData);
            }
         }

         callData.status = CallData.StatusEnum.FAILED;
         callData.failReason = var13.getMessage();
      }

      return callData;
   }

   public void cancelCall(CallData callData) {
      synchronized(this.callsInProgress) {
         this.unlinkCallData(this.callsInProgress, callData);
      }
   }

   public void asteriskDisconnected(String reason) {
      log.warn("Connection to " + this.gatewayData.server + " is lost - " + reason + ".");

      while(this.tryReconnect && !this.connection.isConnected()) {
         try {
            Thread.sleep((long)CONNECTION_RETRY_INTERVAL);
            log.info("Reconnecting to " + this.gatewayData.server + ".");
            this.connect();
            log.info("Connection to " + this.gatewayData.server + " is restored.");
         } catch (Exception var3) {
            log.warn("Reconnecting to " + this.gatewayData.server + " failed - " + var3.getMessage() + ".");
         }
      }

   }

   public void asteriskEventReceived(AsteriskCommand event) {
      String eventName = event.getName();
      if ("Status".equalsIgnoreCase(eventName)) {
         this.onStatus(event);
      } else if ("StatusComplete".equalsIgnoreCase(eventName)) {
         this.onStatusComplete();
      } else if ("OriginateFailure".equalsIgnoreCase(eventName)) {
         this.onOriginateFailure(event);
      } else if ("OriginateSuccess".equalsIgnoreCase(eventName)) {
         this.onOriginateSuccess(event);
      } else if ("OriginateResponse".equalsIgnoreCase(eventName)) {
         if ("Failure".equalsIgnoreCase(event.getProperty("Response"))) {
            this.onOriginateFailure(event);
         } else if ("Success".equalsIgnoreCase(event.getProperty("Response"))) {
            this.onOriginateSuccess(event);
         }
      } else if ("NewCallerID".equalsIgnoreCase(eventName)) {
         this.onNewCallerID(event);
      } else if ("Dial".equalsIgnoreCase(eventName)) {
         this.onDial(event);
      } else if ("Link".equalsIgnoreCase(eventName)) {
         this.onLink(event);
      } else if ("Hangup".equalsIgnoreCase(eventName)) {
         this.onHangup(event);
      } else if ("CDR".equalsIgnoreCase(eventName)) {
         this.onCDR(event);
      }

   }

   private void completeCall(CallData callData) {
      log.info("COMPLETE CALL. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", UniqueID = " + callData.uniqueID + ", FailReason = " + callData.failReason + ", FailReasonCode = " + callData.failReasonCode + ", SourceChannel = " + callData.sourceChannel + ", SourceDuration = " + callData.sourceDuration + ", SourceHangupReason = " + callData.sourceHangupReason + ", SourceHangupCode = " + callData.sourceHangupCode + ", DestinationChannel = " + callData.destinationChannel + ", DestinationDuration = " + callData.destinationDuration + ", DestinationHangupReason = " + callData.destinationHangupReason + ", DestinationHangupCode = " + callData.destinationHangupCode + ".");
      synchronized(this.callsInProgress) {
         this.unlinkCallData(this.callsInProgress, callData);
      }

      if (callData.failReason == null && callData.failReasonCode == null) {
         if (callData.sourceDuration != null && callData.sourceDuration != 0L && callData.destinationChannel != null) {
            if (callData.destinationDuration == null || callData.destinationDuration == 0L) {
               callData.failReasonCode = callData.destinationHangupCode;
               callData.failReason = callData.destinationHangupReason;
            }
         } else {
            callData.failReasonCode = callData.sourceHangupCode;
            callData.failReason = callData.sourceHangupReason;
         }
      }

      if (this.callMaker != null) {
         this.callMaker.callCompleted(callData, true);
      }

   }

   private void checkAndCompleteCall(CallData callData, boolean failureKnown) {
      if (callData != null) {
         log.info("CHECK CALL. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", UniqueID = " + callData.uniqueID + ", FailReason = " + callData.failReason + ", FailReasonCode = " + callData.failReasonCode + ", SourceChannel = " + callData.sourceChannel + ", SourceDuration = " + callData.sourceDuration + ", SourceHangupReason = " + callData.sourceHangupReason + ", SourceHangupCode = " + callData.sourceHangupCode + ", DestinationChannel = " + callData.destinationChannel + ", DestinationDuration = " + callData.destinationDuration + ", DestinationHangupReason = " + callData.destinationHangupReason + ", DestinationHangupCode = " + callData.destinationHangupCode);
         if (failureKnown) {
            this.completeCall(callData);
         } else if (callData.sourceChannel != null && callData.sourceHangupCode != null && callData.destinationChannel == null) {
            this.completeCall(callData);
         } else if ((callData.sourceHangupCode != null || callData.destinationHangupCode == null || callData.destinationProvider == null || callData.destinationNextProvider == null || callData.destinationProvider == callData.destinationNextProvider) && callData.sourceHangupCode != null && callData.destinationHangupCode != null && callData.sourceDuration != null) {
            this.completeCall(callData);
         }

      }
   }

   private void onStatus(AsteriskCommand event) {
      String uniqueID = event.getProperty("UniqueID");
      String channel = event.getProperty("Channel");
      CallData callData;
      synchronized(this.callsToReconcile) {
         callData = this.findEventCallData(this.callsToReconcile, event);
      }

      if (callData == null) {
         synchronized(this.callsInProgress) {
            callData = this.findEventCallData(this.callsInProgress, event);
            if (callData != null && callData.sourceChannel != null && !callData.sourceChannel.equals(uniqueID)) {
               callData.destinationChannel = uniqueID;
               callData.destinationChannelName = channel;
               this.linkCallData(this.callsInProgress, callData);
            }
         }
      } else {
         synchronized(this.callsToReconcile) {
            this.unlinkCallData(this.callsToReconcile, callData);
            if (callData != null && (callData.destinationChannel == null || !callData.destinationChannel.equals(uniqueID))) {
               callData.sourceChannel = uniqueID;
               callData.sourceChannelName = channel;
            }
         }

         synchronized(this.callsInProgress) {
            this.linkCallData(this.callsInProgress, callData);
         }
      }

   }

   private void onStatusComplete() {
      Set<CallData> callsToComplete = new HashSet();
      synchronized(this.callsToReconcile) {
         callsToComplete.addAll(this.callsToReconcile.values());
         this.callsToReconcile.clear();
      }

      if (this.callMaker != null) {
         Iterator i$ = callsToComplete.iterator();

         while(i$.hasNext()) {
            CallData callData = (CallData)i$.next();
            this.callMaker.callCompleted(callData, false);
         }
      }

   }

   private void onOriginateFailure(AsteriskCommand event) {
      Integer cause;
      try {
         cause = Integer.valueOf(event.getProperty("Reason"));
      } catch (Exception var7) {
         cause = null;
      }

      CallData callData;
      synchronized(this.callsInProgress) {
         callData = this.findEventCallData(this.callsInProgress, event);
      }

      if (callData == null) {
         log.warn("Unable to retrieve phone call from Call ID 0.");
      } else {
         callData.failReasonCode = cause;
         callData.failReason = "Originate failed";
         log.warn("OriginateFailure. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ". Unique ID " + callData.uniqueID + ". Action ID " + callData.actionID + ". Source: " + callData.source + ".");
         this.checkAndCompleteCall(callData, true);
      }
   }

   private void onOriginateSuccess(AsteriskCommand event) {
      String uniqueID = event.getProperty("UniqueID");
      String channel = event.getProperty("Channel");
      synchronized(this.callsInProgress) {
         CallData callData = this.findEventCallData(this.callsInProgress, event);
         if (callData == null) {
            log.warn("Unable to retrieve phone call from Call ID 0.");
         } else {
            callData.uniqueID = uniqueID;
            callData.sourceChannel = uniqueID;
            callData.sourceChannelName = channel;
            this.linkCallData(this.callsInProgress, callData);
            log.debug("OriginateSuccess. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ". Unique ID " + callData.uniqueID + ". Action ID " + callData.actionID + ". Source: " + callData.source + ".");
         }
      }
   }

   private void onNewCallerID(AsteriskCommand event) {
      String uniqueID = event.getProperty("UniqueID");
      String channel = event.getProperty("Channel");
      synchronized(this.callsInProgress) {
         CallData callData = this.findEventCallData(this.callsInProgress, event);
         if (callData == null) {
            log.warn("Unable to retrieve phone call from Call ID 0.");
         } else {
            callData.sourceChannel = uniqueID;
            callData.sourceChannelName = channel;
            this.linkCallData(this.callsInProgress, callData);
            log.debug("NewCallerID. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ". Unique ID " + callData.uniqueID + ". Source Channel " + callData.sourceChannel + ". Action ID " + callData.actionID + ".");
         }
      }
   }

   private void onDial(AsteriskCommand event) {
      String srcUniqueID = event.getProperty("SrcUniqueID");
      String destUniqueID = event.getProperty("DestUniqueID");
      String destChannel = event.getProperty("Destination");
      if (srcUniqueID != null && destUniqueID != null) {
         synchronized(this.callsInProgress) {
            if (this.callsInProgress.containsKey(destUniqueID)) {
               log.warn("Destination channel " + destUniqueID + " already in hash map.");
            } else {
               CallData callData = this.findEventCallData(this.callsInProgress, event);
               if (callData == null) {
                  log.warn("Unable to retrieve phone call from Call ID 0.");
                  log.warn("Unable to retrieve phone call from source channel " + srcUniqueID + ".");
                  return;
               }

               callData.destinationChannel = destUniqueID;
               callData.destinationChannelName = destChannel;
               this.linkCallData(this.callsInProgress, callData);
               log.debug("Dial. Call ID " + (callData.id != null ? callData.id.toString() : 0) + ". Unique ID " + callData.uniqueID + ". Source Channel " + callData.sourceChannel + ". Dest Channel " + callData.destinationChannel + ". Action ID " + callData.actionID + ".");
            }
         }
      }

   }

   private void onLink(AsteriskCommand event) {
      log.debug("Call established " + event.getProperty("Uniqueid1") + " : " + event.getProperty("Uniqueid2") + ".");
   }

   private void onHangup(AsteriskCommand event) {
      String uniqueID = event.getProperty("UniqueID");
      CallData callData;
      synchronized(this.callsInProgress) {
         callData = this.findEventCallData(this.callsInProgress, event);
      }

      if (callData == null) {
         log.warn("Unable to retrieve phone call from Call ID 0.");
      } else {
         if (uniqueID.equals(callData.sourceChannel)) {
            try {
               callData.sourceHangupCode = Integer.valueOf(event.getProperty("Cause"));
            } catch (Exception var7) {
               callData.sourceHangupCode = null;
            }

            callData.sourceHangupReason = event.getProperty("Cause-txt");
            log.debug("Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", source hangup " + callData.sourceHangupCode + "-" + callData.sourceHangupReason + ".");
         } else if (uniqueID.equals(callData.destinationChannel)) {
            try {
               callData.destinationHangupCode = Integer.valueOf(event.getProperty("Cause"));
            } catch (Exception var6) {
               callData.destinationHangupCode = null;
            }

            callData.destinationHangupReason = event.getProperty("Cause-txt");
            log.debug("Call ID " + (callData.id != null ? callData.id.toString() : 0) + ", destination hangup " + callData.destinationHangupCode + "-" + callData.destinationHangupReason + ".");
         }

         this.checkAndCompleteCall(callData, false);
      }
   }

   private void onCDR(AsteriskCommand event) {
      CallData callData;
      synchronized(this.callsInProgress) {
         callData = this.findEventCallData(this.callsInProgress, event);
      }

      if (callData == null) {
         log.warn("Unable to retrieve phone call from Call ID 0.");
      } else {
         String userField = event.getProperty("UserField");
         log.info("UserField for call ID " + (callData.id != null ? callData.id.toString() : 0) + " is " + userField + ".");
         if (userField != null && userField.indexOf(":") >= 0) {
            String[] parts = userField.split(":");
            userField = parts.length > 0 && parts[0] != null ? parts[0] : "";
            Integer oldDestinationProvider = callData != null ? callData.destinationProvider : 0;
            if (parts.length > 1 && parts[1] != null && parts[1].startsWith("dp=") && parts[1].length() > 3) {
               boolean var6 = true;

               int dp;
               try {
                  dp = Integer.parseInt(parts[1].substring(3));
               } catch (Exception var8) {
                  dp = -1;
               }

               callData.destinationProvider = dp >= 0 ? dp : callData.destinationProvider;
            } else if (parts.length > 1 && parts[1] != null && parts[1].startsWith("fo=1")) {
               callData.destinationProvider = callData.destinationNextProvider;
            }

            log.info("Destination for call ID " + (callData.id != null ? callData.id.toString() : 0) + " is provider ID " + callData.destinationProvider + " (was provider ID " + oldDestinationProvider + ").");
         }

         if (!callData.id.toString().equals(userField)) {
            log.warn("UserField " + userField + " not matching Call ID " + (callData.id != null ? callData.id.toString() : 0) + ".");
         } else {
            long duration = Long.parseLong(event.getProperty("Duration"));
            long billableSeconds = Long.parseLong(event.getProperty("BillableSeconds"));
            log.info("CDR record. ID: " + (callData.id != null ? callData.id.toString() : 0) + ". Duration: " + duration + " seconds" + ". Billsec: " + billableSeconds + " seconds.");
            callData.sourceDuration = duration;
            callData.destinationDuration = billableSeconds;
            this.checkAndCompleteCall(callData, false);
         }

      }
   }

   private void linkCallData(Map<String, CallData> calls, CallData data) {
      if (calls != null && data != null) {
         if (data.id != null && data.id.toString().length() > 0 && data.id != 0 && !calls.containsKey(data.id.toString())) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Call ID " + data.id + ".");
            calls.put(data.id.toString(), data);
         }

         if (data.uniqueID != null && data.uniqueID.length() > 0 && !calls.containsKey(data.uniqueID)) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Unique ID " + data.uniqueID + ".");
            calls.put(data.uniqueID, data);
         }

         if (data.accountID != null && data.accountID.length() > 0 && !calls.containsKey(data.accountID)) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Account ID " + data.accountID + ".");
            calls.put(data.accountID, data);
         }

         if (data.sourceChannel != null && data.sourceChannel.length() > 0 && !calls.containsKey(data.sourceChannel)) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Source Channel " + data.sourceChannel + ".");
            calls.put(data.sourceChannel, data);
         }

         if (data.destinationChannel != null && data.destinationChannel.length() > 0 && !calls.containsKey(data.destinationChannel)) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Dest Channel " + data.destinationChannel + ".");
            calls.put(data.destinationChannel, data);
         }

         if (data.actionID != null && data.actionID.length() > 0 && !calls.containsKey(data.actionID)) {
            log.info("Linked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Action ID " + data.actionID + ".");
            calls.put(data.actionID, data);
         }

      }
   }

   private void unlinkCallData(Map<String, CallData> calls, CallData data) {
      if (calls != null && data != null) {
         if (data.id != null && data.id.toString().length() > 0 && data.id != 0 && calls.containsKey(data.id.toString())) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Call ID " + data.id + ".");
            calls.remove(data.id.toString());
         }

         if (data.uniqueID != null && data.uniqueID.length() > 0 && calls.containsKey(data.uniqueID)) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Unique ID " + data.uniqueID + ".");
            calls.remove(data.uniqueID);
         }

         if (data.accountID != null && data.accountID.length() > 0 && calls.containsKey(data.accountID)) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Account ID " + data.accountID + ".");
            calls.remove(data.accountID);
         }

         if (data.sourceChannel != null && data.sourceChannel.length() > 0 && calls.containsKey(data.sourceChannel)) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Source Channel " + data.sourceChannel + ".");
            calls.remove(data.sourceChannel);
         }

         if (data.destinationChannel != null && data.destinationChannel.length() > 0 && calls.containsKey(data.destinationChannel)) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Dest Channel " + data.destinationChannel + ".");
            calls.remove(data.destinationChannel);
         }

         if (data.actionID != null && data.actionID.length() > 0 && calls.containsKey(data.actionID)) {
            log.info("Unlinked Call ID " + (data.id != null ? data.id.toString() : 0) + " on Action ID " + data.actionID + ".");
            calls.remove(data.actionID);
         }

      }
   }

   private CallData findCallData(Map<String, CallData> calls, CallData data) {
      if (calls != null && data != null) {
         String id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
         log.debug("Trying to find call data [1]: " + id + "," + data.uniqueID + "," + data.accountID + "," + data.sourceChannel + "," + data.destinationChannel + "," + data.actionID + ".");
         if (data.id != null && data.id.toString().length() > 0 && data.id != 0 && calls.containsKey(data.id.toString())) {
            data = (CallData)calls.get(data.id.toString());
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Call ID " + data.id + ".");
            return data;
         } else if (data.uniqueID != null && data.uniqueID.length() > 0 && calls.containsKey(data.uniqueID)) {
            data = (CallData)calls.get(data.uniqueID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Unique ID " + data.uniqueID + ".");
            return data;
         } else if (data.accountID != null && data.accountID.length() > 0 && calls.containsKey(data.accountID)) {
            data = (CallData)calls.get(data.accountID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Account ID " + data.accountID + ".");
            return data;
         } else if (data.sourceChannel != null && data.sourceChannel.length() > 0 && calls.containsKey(data.sourceChannel)) {
            data = (CallData)calls.get(data.sourceChannel);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Source Channel " + data.sourceChannel + ".");
            return data;
         } else if (data.destinationChannel != null && data.destinationChannel.length() > 0 && calls.containsKey(data.destinationChannel)) {
            data = (CallData)calls.get(data.destinationChannel);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Dest Channel " + data.destinationChannel + ".");
            return data;
         } else if (data.actionID != null && data.actionID.length() > 0 && calls.containsKey(data.actionID)) {
            data = (CallData)calls.get(data.actionID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Action ID " + data.actionID + ".");
            return data;
         } else {
            log.warn("Could not find call [1]: " + id + "," + data.uniqueID + "," + data.accountID + "," + data.sourceChannel + "," + data.destinationChannel + "," + data.actionID + ".");
            return null;
         }
      } else {
         log.warn("Could not find call data due to null [1].");
         return null;
      }
   }

   private CallData findEventCallData(Map<String, CallData> calls, AsteriskCommand event) {
      String uniqueID = event.getProperty("UniqueID");
      String accountID = event.getProperty("Account") != null ? event.getProperty("Account") : event.getProperty("AccountCode");
      String srcUniqueID = event.getProperty("SrcUniqueID");
      String destUniqueID = event.getProperty("DestUniqueID");
      String actionID = event.getProperty("ActionID");
      if (calls != null && event != null) {
         CallData data = null;
         String id = "0";
         log.debug("Trying to find call data [2]: <none>," + uniqueID + "," + accountID + "," + srcUniqueID + "," + destUniqueID + "," + actionID + ".");
         if (uniqueID != null && uniqueID.length() > 0 && calls.containsKey(uniqueID)) {
            data = (CallData)calls.get(uniqueID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Unique ID " + uniqueID + ".");
            return data;
         } else if (accountID != null && accountID.length() > 0 && calls.containsKey(accountID)) {
            data = (CallData)calls.get(accountID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Account ID " + accountID + ".");
            return data;
         } else if (srcUniqueID != null && srcUniqueID.length() > 0 && calls.containsKey(srcUniqueID)) {
            data = (CallData)calls.get(srcUniqueID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Source Channel " + srcUniqueID + ".");
            return data;
         } else if (destUniqueID != null && destUniqueID.length() > 0 && calls.containsKey(destUniqueID)) {
            data = (CallData)calls.get(destUniqueID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Dest Channel " + destUniqueID + ".");
            return data;
         } else if (actionID != null && actionID.length() > 0 && calls.containsKey(actionID)) {
            data = (CallData)calls.get(actionID);
            id = data.id != null && data.id.toString().length() > 0 && data.id != 0 ? data.id.toString() : "0";
            log.info("Found Call ID " + id + " on Action ID " + actionID + ".");
            return data;
         } else {
            log.warn("Could not find call data event [2]: <none>," + uniqueID + "," + accountID + "," + srcUniqueID + "," + destUniqueID + "," + actionID + ".");
            return null;
         }
      } else {
         log.warn("Could not find call data event due to null [2].");
         return null;
      }
   }

   private CallData fillCallDataID(Map<String, CallData> calls, CallData data) {
      if (calls != null && data != null) {
         if (data.id != null && data.id.toString().length() > 0 && data.id != 0) {
            if (data.actionID == null || data.actionID.length() < 1) {
               data.actionID = data.id.toString();
            }

            if (data.accountID == null || data.accountID.length() < 1) {
               data.accountID = data.id.toString();
            }

            log.debug("Filling Call ID " + (data.id != null ? data.id.toString() : 0) + " with Action ID " + data.actionID + " and AccountID " + data.accountID + ".");
         } else {
            ++this.actionID;
            if (data.actionID == null || data.actionID.length() < 1) {
               data.actionID = "A" + String.valueOf(this.actionID);
            }

            if (data.accountID == null || data.accountID.length() < 1) {
               data.accountID = "A" + String.valueOf(this.actionID);
            }

            log.debug("Filling Call ID 0 with Action ID " + data.actionID + " and AccountID " + data.accountID + ".");
         }

         return data;
      } else {
         return data;
      }
   }
}
