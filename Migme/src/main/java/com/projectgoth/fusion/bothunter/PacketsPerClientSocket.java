package com.projectgoth.fusion.bothunter;

import java.util.ArrayList;
import java.util.Iterator;

public class PacketsPerClientSocket extends ArrayList<PacketDetails> {
   private long lastAddedTo = System.currentTimeMillis();
   private Byte winscale = null;
   private Byte claimedClientType = null;

   public Object clone() {
      PacketsPerClientSocket clone = (PacketsPerClientSocket)super.clone();
      clone.setLastAddedTo(this.lastAddedTo);
      return clone;
   }

   public boolean add(PacketDetails pd) {
      if (pd == null) {
         throw new RuntimeException("Trying to add null PacketDetails!");
      } else {
         this.lastAddedTo = System.currentTimeMillis();
         if (pd.getTcpWinscale() != null) {
            this.winscale = pd.getTcpWinscale();
            return true;
         } else {
            if (pd.getClaimedClientType() != null) {
               this.claimedClientType = pd.getClaimedClientType();
            }

            return super.add(pd);
         }
      }
   }

   public boolean isTimedOut() {
      return (System.currentTimeMillis() - this.lastAddedTo) / 1000L > (long)Params.CLIENT_PORT_TIMEOUT_SECS;
   }

   public long getLastAddedTo() {
      return this.lastAddedTo;
   }

   public void setLastAddedTo(long value) {
      this.lastAddedTo = value;
   }

   public double getMeanTcpTimestamp() {
      double sum = 0.0D;

      PacketDetails pd;
      for(Iterator i$ = this.iterator(); i$.hasNext(); sum += (double)pd.getTcpTimestamp()) {
         pd = (PacketDetails)i$.next();
      }

      return sum / (double)this.size();
   }

   public double getMeanTcpTimestampOverArrivalTime() {
      double sum = 0.0D;

      PacketDetails pd;
      for(Iterator i$ = this.iterator(); i$.hasNext(); sum += pd.getTcpTimestampOverArrivalTime()) {
         pd = (PacketDetails)i$.next();
      }

      return sum / (double)this.size();
   }

   public double getTcpTimestampOverArrivalTimeStdError(double meanRatio) {
      double sumDeltaSquared = 0.0D;

      PacketDetails pd;
      for(Iterator i$ = this.iterator(); i$.hasNext(); sumDeltaSquared += Math.pow(pd.getTcpTimestampOverArrivalTime() - meanRatio, 2.0D)) {
         pd = (PacketDetails)i$.next();
      }

      double oneOverNMinusOne = 1.0D / ((double)this.size() - 1.0D);
      return Math.sqrt(oneOverNMinusOne * sumDeltaSquared);
   }

   public Byte getLastWinscaleSeen() {
      return this.winscale;
   }

   public Byte getClaimedClientType() {
      return this.claimedClientType;
   }
}
