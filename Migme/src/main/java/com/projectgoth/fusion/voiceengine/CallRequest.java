package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.data.CallData;

public class CallRequest {
   protected CallData callData = null;
   protected String dialCommand = null;
   protected String dialNextCommand = null;
   protected long limitDuration = 0L;
   protected double limitRate = 0.0D;
   protected long limitTimeoutWarning = 0L;
   protected long limitTimeoutRepeat = 0L;
   protected int callId = 0;

   public CallRequest() {
   }

   public CallRequest(CallData callData, String dialCommand, String dialNextCommand, long limitDuration, double limitRate, long limitTimeoutWarning, long limitTimeoutRepeat, int callId) {
      this.callData = callData;
      this.dialCommand = dialCommand;
      this.dialNextCommand = dialNextCommand;
      this.limitDuration = limitDuration;
      this.limitRate = limitRate;
      this.limitTimeoutWarning = limitTimeoutWarning;
      this.limitTimeoutRepeat = limitTimeoutRepeat;
      this.callId = callId;
   }

   public void setCallData(CallData callData) {
      this.callData = callData;
   }

   public CallData getCallData() {
      return this.callData;
   }

   public void setDialCommand(String dialCommand) {
      this.dialCommand = dialCommand;
   }

   public String getDialCommand() {
      return this.dialCommand;
   }

   public void setDialNextCommand(String dialNextCommand) {
      this.dialNextCommand = dialNextCommand;
   }

   public String getDialNextCommand() {
      return this.dialNextCommand;
   }

   public void setLimitDuration(long limitDuration) {
      this.limitDuration = limitDuration;
   }

   public long getLimitDuration() {
      return this.limitDuration;
   }

   public void setLimitRate(double limitRate) {
      this.limitRate = limitRate;
   }

   public double getLimitRate() {
      return this.limitRate;
   }

   public void setLimitTimeoutWarning(long limitTimeoutWarning) {
      this.limitTimeoutWarning = limitTimeoutWarning;
   }

   public long getLimitTimeoutWarning() {
      return this.limitTimeoutWarning;
   }

   public void setLimitTimeoutRepeat(long limitTimeoutRepeat) {
      this.limitTimeoutRepeat = limitTimeoutRepeat;
   }

   public long getLimitTimeoutRepeat() {
      return this.limitTimeoutRepeat;
   }

   public void setCallId(int callId) {
      this.callId = callId;
   }

   public int getCallId() {
      return this.callId;
   }

   public void setDestination(String destination) {
      this.callData.destination = destination;
      this.callData.destinationType = destination == null ? null : CallData.SourceDestinationTypeEnum.PSTN_PHONE;
   }

   public String getDestination() {
      return this.callData == null ? null : this.callData.destination;
   }
}
