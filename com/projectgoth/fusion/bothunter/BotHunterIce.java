package com.projectgoth.fusion.bothunter;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class BotHunterIce extends Application {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotHunterIce.class));
   private static final String ONE = "1";
   private static final String CONFIG_FILE = "BotHunter.cfg";
   private ObjectAdapter bhAdaptor;
   private ObjectAdapter adminAdaptor;

   public static void main(String[] args) {
      BotHunterIce mbi = new BotHunterIce();
      String configFile = args.length > 0 ? args[0] : "BotHunter.cfg";
      int status = mbi.main(mbi.getClass().getName(), args, configFile);
      System.exit(status);
   }

   public int run(String[] args) {
      try {
         log.error("Starting Bot Hunter as an ice app");
         log.error("");
         this.bhAdaptor = communicator().createObjectAdapter("BotHunterAdapter");
         this.bhAdaptor.add(new BotHunterI(), Util.stringToIdentity("BotHunter"));
         this.bhAdaptor.activate();
         this.adminAdaptor = communicator().createObjectAdapter("AdminAdapter");
         this.adminAdaptor.add(new BotHunterAdminI(), Util.stringToIdentity("BotHunterAdmin"));
         this.adminAdaptor.activate();
         this.getSettings(args);
         String[] argsMinusFirst = new String[args.length - 1];
         System.arraycopy(args, 1, argsMinusFirst, 0, args.length - 1);
         BotHunter.mainInner(argsMinusFirst, false);
         log.error("Server terminated");
         return 0;
      } catch (Exception var3) {
         log.error("Exception occured. Server terminated. ", var3);
         return -1;
      }
   }

   private void getSettings(String[] args) {
      Properties p = communicator().getProperties();
      if (this.booleanIceProperty(p, "AnalyseRatios")) {
         Params.RATIO_ANALYSIS = true;
      }

      if (this.booleanIceProperty(p, "AnalyseSequence")) {
         Params.SEQUENCE_ANALYSIS = true;
      }

      Params.ANALYSIS_THREADS = p.getPropertyAsIntWithDefault("AnalysisThreadCount", Params.ANALYSIS_THREADS);
      if (this.booleanIceProperty(p, "AutoKick")) {
         Params.AUTOKICK = true;
         Params.REGISTRY_PROXY = p.getProperty("RegistryProxy");
      }

      Params.MIN_ANALYSIS_PACKETS_PER_SOCKET = p.getPropertyAsIntWithDefault("MinAnalysisPackets", Params.MIN_ANALYSIS_PACKETS_PER_SOCKET);
      Params.MIN_INTERLEAVE_TRANSITIONS = p.getPropertyAsIntWithDefault("Mitc", Params.MIN_INTERLEAVE_TRANSITIONS);
      Params.PACKET_SOURCE = p.getPropertyWithDefault("PacketSource", (String)null);
      Params.PCAP_FILTER = p.getPropertyWithDefault("PcapFilter", Params.PCAP_FILTER);
      Params.STATS_INTERVAL_SECONDS = p.getPropertyAsIntWithDefault("StatsInterval", Params.STATS_INTERVAL_SECONDS);
      Logger monitorLogger;
      if (this.booleanIceProperty(p, "VerboseAnalysis")) {
         monitorLogger = Logger.getLogger(ConfigUtils.getLoggerName(AnalysisThread.class));
         monitorLogger.setLevel(Level.DEBUG);
      }

      if (this.booleanIceProperty(p, "VerboseKick")) {
         monitorLogger = Logger.getLogger(ConfigUtils.getLoggerName(KickThread.class));
         monitorLogger.setLevel(Level.DEBUG);
      }

      if (this.booleanIceProperty(p, "VerbosePacketCapture")) {
         monitorLogger = Logger.getLogger(ConfigUtils.getLoggerName(MonitorThread.class));
         monitorLogger.setLevel(Level.DEBUG);
      }

   }

   private boolean booleanIceProperty(Properties p, String propName) {
      return p.getProperty(propName).equals("1");
   }
}
