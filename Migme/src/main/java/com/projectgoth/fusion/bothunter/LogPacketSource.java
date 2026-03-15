package com.projectgoth.fusion.bothunter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class LogPacketSource implements PacketSource {
   private final String REGEXP = "IP ([a-zA-Z0-9\\.\\-\\_]+).*TS val\\s+([0-9]+)";
   private boolean finished = false;
   private FileInputStream fstream;
   private DataInputStream in;
   private BufferedReader br;
   private String ip;
   private long tcpTimestamp;
   private long timeReceived;

   public LogPacketSource(String logFilename) throws Exception {
      this.fstream = new FileInputStream(logFilename);
      this.in = new DataInputStream(this.fstream);
      this.br = new BufferedReader(new InputStreamReader(this.in));
   }

   public boolean finished() {
      return true;
   }

   public PacketDetails nextPacket() throws Exception {
      String strLine = this.br.readLine();
      if (strLine == null) {
         this.finished = true;
         return null;
      } else {
         Pattern p = Pattern.compile("[,\\s]+");
         String[] fields = p.split(strLine);
         DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
         Date d = df.parse(fields[0].substring(0, 12));
         long arrivalTime = d.getTime();
         String ipAndPort = fields[2];
         int lastPeriod = ipAndPort.lastIndexOf(46);
         String ip = ipAndPort.substring(0, lastPeriod);
         int destinationPort = Integer.parseInt(ipAndPort.substring(lastPeriod + 1));
         Long tcpTimestamp = null;

         for(int i = 3; i < fields.length; ++i) {
            if (fields[i].equals("val")) {
               tcpTimestamp = Long.parseLong(fields[i + 1]);
               break;
            }
         }

         return new PacketDetails(arrivalTime, ip, destinationPort, tcpTimestamp, (Byte)null);
      }
   }

   public void close() {
      try {
         if (this.br != null) {
            this.br.close();
         }

         if (this.fstream != null) {
            this.fstream.close();
         }

         if (this.in != null) {
            this.in.close();
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public String getSourceIP() {
      return this.ip;
   }

   public String getDestinationIP() {
      return null;
   }

   public long getTcpTimestamp() {
      return this.tcpTimestamp;
   }

   public long getTimeReceived() {
      return 0L;
   }
}
