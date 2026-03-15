package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.slice.SuspectIce;

public class Suspect extends ClientAddress {
   private long lastAddedTo;
   private double meanTcpTimestamp;
   private double meanTcpTimestampOverArrivalTime;
   private Byte lastWinscaleSeen;
   private Byte claimedClientType;
   private String username;

   public Suspect(String clientIP, int clientPort, PacketsPerClientSocket packets) {
      super(clientIP, clientPort);
      this.lastAddedTo = packets.getLastAddedTo();
      this.meanTcpTimestamp = packets.getMeanTcpTimestamp();
      this.meanTcpTimestampOverArrivalTime = packets.getMeanTcpTimestampOverArrivalTime();
      this.lastWinscaleSeen = packets.getLastWinscaleSeen();
      this.claimedClientType = packets.getClaimedClientType();
   }

   public Suspect(SuspectIce si) {
      super(si.clientIP, si.clientPort);
      this.lastAddedTo = si.lastAddedTo;
      this.meanTcpTimestamp = si.meanTcpTimestamp;
      this.meanTcpTimestampOverArrivalTime = si.meanTcpTimestampOverArrivalTime;
      this.username = si.username;
   }

   public long getLastAddedTo() {
      return this.lastAddedTo;
   }

   public double getMeanTcpTimestamp() {
      return this.meanTcpTimestamp;
   }

   public double getMeanTcpTimestampOverArrivalTime() {
      return this.meanTcpTimestampOverArrivalTime;
   }

   public String getUsername() {
      return this.username;
   }

   public Byte getLastWinscaleSeen() {
      return this.lastWinscaleSeen;
   }

   public Byte getClaimedClientType() {
      return this.claimedClientType;
   }

   public void setUsername(String s) {
      this.username = s;
   }

   public SuspectIce toIceObject() {
      SuspectIce si = new SuspectIce();
      si.clientIP = this.clientIP;
      si.clientPort = this.clientPort;
      si.lastAddedTo = this.lastAddedTo;
      si.meanTcpTimestamp = this.meanTcpTimestamp;
      si.meanTcpTimestampOverArrivalTime = this.meanTcpTimestampOverArrivalTime;
      si.username = this.username;
      return si;
   }
}
