package com.projectgoth.fusion.bothunter;

public interface PacketSource {
   PacketDetails nextPacket() throws Exception;

   boolean finished() throws Exception;

   void close();

   String getSourceIP();

   String getDestinationIP();

   long getTcpTimestamp();

   long getTimeReceived();
}
