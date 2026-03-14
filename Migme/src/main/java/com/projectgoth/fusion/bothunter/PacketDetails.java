package com.projectgoth.fusion.bothunter;

public class PacketDetails {
   private final long arrivalTime;
   private final String clientIP;
   private final int clientPort;
   private final long tcpTimestamp;
   private final Byte tcpWinscale;
   private Byte claimedClientType;

   public PacketDetails(long arrivalTime, String clientIP, int clientPort, long tcpTimestamp, Byte tcpWinscale) {
      this.arrivalTime = arrivalTime;
      this.clientIP = clientIP;
      this.clientPort = clientPort;
      this.tcpTimestamp = tcpTimestamp;
      this.tcpWinscale = tcpWinscale;
   }

   public long getArrivalTime() {
      return this.arrivalTime;
   }

   public String getClientIP() {
      return this.clientIP;
   }

   public int getClientPort() {
      return this.clientPort;
   }

   public long getTcpTimestamp() {
      return this.tcpTimestamp;
   }

   public Byte getTcpWinscale() {
      return this.tcpWinscale;
   }

   public void setClaimedClientType(byte type) {
      this.claimedClientType = type;
   }

   public Byte getClaimedClientType() {
      return this.claimedClientType;
   }

   public double getTcpTimestampOverArrivalTime() {
      return (double)this.tcpTimestamp / (double)this.arrivalTime;
   }
}
