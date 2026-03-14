package com.projectgoth.fusion.bothunter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketsPerIP {
   private boolean underAnalysis = false;
   private String ip;
   private long lastAddedTo = System.currentTimeMillis();
   private PacketsPerIP.ConcurrentHashMapOfArrayLists<Integer, PacketsPerClientSocket> contents;

   public PacketsPerIP(String ip) {
      this.ip = ip;
      this.contents = new PacketsPerIP.ConcurrentHashMapOfArrayLists();
   }

   public PacketsPerIP(String ip, PacketsPerIP.ConcurrentHashMapOfArrayLists<Integer, PacketsPerClientSocket> pContents) {
      this.ip = ip;
      this.contents = (PacketsPerIP.ConcurrentHashMapOfArrayLists)pContents.clone();
   }

   public String getIP() {
      return this.ip;
   }

   public Object clone() {
      PacketsPerIP copy = new PacketsPerIP(this.ip, this.contents);
      return copy;
   }

   public synchronized void addPacket(PacketDetails p) {
      PacketsPerClientSocket pps = (PacketsPerClientSocket)this.contents.get(p.getClientPort());
      if (pps == null) {
         pps = new PacketsPerClientSocket();
         this.contents.put(p.getClientPort(), pps);
      }

      pps.add(p);
      if (pps.size() > Params.MAX_PACKETS_PER_SOCKET) {
         pps.remove(0);
      }

      this.lastAddedTo = System.currentTimeMillis();
   }

   public PacketsPerClientSocket getPacketsPerClientSocket(int clientSocket) {
      return (PacketsPerClientSocket)this.contents.get(clientSocket);
   }

   public int[] getPortAndPacketCount() {
      int portCount = 0;
      int packetCount = 0;

      PacketsPerClientSocket ppcs;
      for(Iterator i$ = this.contents.keySet().iterator(); i$.hasNext(); packetCount += ppcs.size()) {
         int socket = (Integer)i$.next();
         ppcs = (PacketsPerClientSocket)this.contents.get(socket);
         ++portCount;
      }

      int[] result = new int[]{portCount, packetCount};
      return result;
   }

   public synchronized void setUnderAnalysis() throws PacketsPerIP.BeingAnalyzedException, PacketsPerIP.TimedOutException {
      if ((System.currentTimeMillis() - this.lastAddedTo) / 1000L > (long)Params.CLIENT_IP_TIMEOUT_SECS) {
         throw new PacketsPerIP.TimedOutException();
      } else if (this.underAnalysis) {
         throw new PacketsPerIP.BeingAnalyzedException();
      } else {
         this.underAnalysis = true;
      }
   }

   public void doCleanupDuringAnalysis() {
      Set<Integer> keys = this.contents.keySet();
      Iterator i = keys.iterator();

      while(i.hasNext()) {
         Integer clientPort = (Integer)i.next();
         PacketsPerClientSocket ppcs = (PacketsPerClientSocket)this.contents.get(clientPort);
         if (ppcs.isTimedOut()) {
            this.contents.remove(clientPort);
         }
      }

   }

   public synchronized void clearUnderAnalysis() {
      this.underAnalysis = false;
   }

   public Set<Integer> getSockets() {
      return this.contents.keySet();
   }

   private class ConcurrentHashMapOfArrayLists<K, V extends ArrayList> extends ConcurrentHashMap<K, V> {
      private static final long serialVersionUID = 1L;

      private ConcurrentHashMapOfArrayLists() {
      }

      public Object clone() {
         PacketsPerIP.ConcurrentHashMapOfArrayLists<K, V> copy = PacketsPerIP.this.new ConcurrentHashMapOfArrayLists();
         Set<K> keys = this.keySet();

         Object key;
         ArrayList value;
         for(Iterator i = keys.iterator(); i.hasNext(); copy.put(key, value)) {
            key = i.next();
            value = (ArrayList)this.get(key);
            if (value != null) {
               value = (ArrayList)value.clone();
            }
         }

         return copy;
      }

      // $FF: synthetic method
      ConcurrentHashMapOfArrayLists(Object x1) {
         this();
      }
   }

   public class TimedOutException extends Exception {
   }

   public class BeingAnalyzedException extends Exception {
   }
}
