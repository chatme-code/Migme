package com.projectgoth.fusion.bothunter;

public class ClientAddress {
   private static final String COLON = ":";
   protected String clientIP;
   protected int clientPort;

   public ClientAddress(String clientIP, int clientPort) {
      this.clientIP = clientIP;
      this.clientPort = clientPort;
   }

   public String getClientIP() {
      return this.clientIP;
   }

   public int getClientPort() {
      return this.clientPort;
   }

   public String toString() {
      return this.clientIP + ":" + this.clientPort;
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public boolean equals(Object other) {
      ClientAddress caOther = (ClientAddress)other;
      return caOther.getClientIP().equals(this.clientIP) && caOther.getClientPort() == this.clientPort;
   }
}
