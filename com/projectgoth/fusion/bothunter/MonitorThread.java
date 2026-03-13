package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import org.apache.log4j.Logger;

public class MonitorThread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MonitorThread.class));
   private boolean debugEnabled;
   private String nicOrLog;
   private String pcapFilter;
   private int analysisThreads;
   private boolean integratedKick;
   private PacketSource packetSource;
   private ConcurrentHashMap<String, PacketsPerIP> ipBuckets = new ConcurrentHashMap();
   private int captureCount = 0;
   private long lTotalNanos = 0L;
   private long lStartTimeMillis;
   private long lLastStatsMs;

   public MonitorThread(String nicOrLog, String pcapFilter, int analysisThreads, boolean integratedKick) {
      log.info("java.library.path=" + System.getProperty("java.library.path"));
      this.debugEnabled = log.isDebugEnabled();
      this.nicOrLog = nicOrLog;
      this.pcapFilter = pcapFilter;
      this.analysisThreads = analysisThreads;
      this.integratedKick = integratedKick;

      try {
         File test = new File(nicOrLog);
         if (test.exists()) {
            this.packetSource = new LogPacketSource(nicOrLog);
         } else {
            this.packetSource = new JpcapPacketSource(nicOrLog, pcapFilter);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public void monitor() {
      try {
         this.traceParams();
         this.printNetworkIFs();
         this.monitorInner();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   private void traceParams() {
      log.info("");
      log.info("BotHunter starting with:");
      log.info("PacketSource=" + this.nicOrLog);
      log.info("--> using " + this.packetSource);
      log.info("PcapFilter=" + this.pcapFilter);
      log.info("AnalysisThreadCount=" + this.analysisThreads);
      log.info("");
      log.info("MinAnalysisPackets=" + Params.MIN_ANALYSIS_PACKETS_PER_SOCKET);
      log.info("AnalyseSequence=" + Params.SEQUENCE_ANALYSIS);
      log.info("Mitc=" + Params.MIN_INTERLEAVE_TRANSITIONS);
      log.info("AnalyseRatios=" + Params.RATIO_ANALYSIS);
      if (Params.RATIO_ANALYSIS) {
         log.info("AnalyseRatiosSensitivity=" + Params.ANALYSE_RATIOS_SENSITIVITY);
      }

      log.info("");
      log.info("AutoKick=" + Params.AUTOKICK);
      if (Params.AUTOKICK) {
         log.info("RegistryProxy=" + Params.REGISTRY_PROXY);
      }

      log.info("StatsInterval=" + Params.STATS_INTERVAL_SECONDS);
      log.info("");
      log.info("MaxPacketsPerSocket=" + Params.MAX_PACKETS_PER_SOCKET);
      log.info("ClientIpTimeout=" + Params.CLIENT_IP_TIMEOUT_SECS);
      log.info("ClientPortTimeout=" + Params.CLIENT_PORT_TIMEOUT_SECS);
      log.info("DuplicateReportInterval=" + Params.DUPLICATE_REPORT_INTERVAL_SECS);
      log.info("");
   }

   private void printNetworkIFs() {
      NetworkInterface[] devices = JpcapCaptor.getDeviceList();
      log.info("Network interfaces:");
      NetworkInterface[] arr$ = devices;
      int len$ = devices.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         NetworkInterface d = arr$[i$];
         log.info("  " + d.name + "(" + d.description + ")");
      }

   }

   private void monitorInner() throws Exception {
      for(int i = 0; i < this.analysisThreads; ++i) {
         Thread at = new Thread(new AnalysisThread(i, this.ipBuckets, this.integratedKick));
         at.setPriority(1);
         at.start();
      }

      this.lStartTimeMillis = System.currentTimeMillis();
      this.lLastStatsMs = System.currentTimeMillis();

      do {
         long startNanos = System.nanoTime();

         PacketDetails pkt;
         try {
            pkt = this.packetSource.nextPacket();
         } catch (Exception var5) {
            log.error(var5, var5);
            pkt = null;
         }

         if (pkt != null) {
            PacketsPerIP ppip = (PacketsPerIP)this.ipBuckets.get(pkt.getClientIP());
            if (ppip == null) {
               ppip = new PacketsPerIP(pkt.getClientIP());
               this.ipBuckets.put(pkt.getClientIP(), ppip);
            }

            ppip.addPacket(pkt);
            this.lTotalNanos += System.nanoTime() - startNanos;
            ++this.captureCount;
            if ((System.currentTimeMillis() - this.lLastStatsMs) / 1000L >= (long)Params.STATS_INTERVAL_SECONDS) {
               this.updatePacketCaptureStats();
               this.lLastStatsMs = System.currentTimeMillis();
            }
         }
      } while(!this.packetSource.finished());

   }

   private void updatePacketCaptureStats() {
      long lElapsedTime = System.currentTimeMillis() - this.lStartTimeMillis;
      double average = (double)this.lTotalNanos / (double)this.captureCount / 1000.0D;
      double pps = (double)this.captureCount / ((double)lElapsedTime / 1000.0D);
      long portCount = 0L;
      long packetCount = 0L;

      int[] counts;
      for(Iterator i$ = this.ipBuckets.keySet().iterator(); i$.hasNext(); packetCount += (long)counts[1]) {
         String ip = (String)i$.next();
         PacketsPerIP bkt = (PacketsPerIP)this.ipBuckets.get(ip);
         counts = bkt.getPortAndPacketCount();
         portCount += (long)counts[0];
      }

      StatsCollector.getInstance().updatePacketCaptureStats(this.lTotalNanos, (long)this.captureCount, average, pps, (long)this.ipBuckets.keySet().size(), portCount, packetCount);
      if (this.debugEnabled) {
         log.debug("AVERAGE PROCESSING TIME PER PACKET (microsecs)=" + average);
         log.debug("packets captured=" + this.captureCount);
         log.debug("packets/sec = " + pps);
         log.debug("ipBuckets ip count=" + this.ipBuckets.keySet().size());
         log.debug("ipBuckets total packet count=" + packetCount);
      }

   }
}
