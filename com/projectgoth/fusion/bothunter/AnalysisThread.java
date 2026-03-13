package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class AnalysisThread implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AnalysisThread.class));
   private StatsCollector statsCollectorInstance;
   private final boolean debugEnabled;
   private final int threadIndex;
   private Random gen = new Random(System.currentTimeMillis() * (long)this.hashCode());
   private ConcurrentHashMap<String, PacketsPerIP> ipBuckets;
   private final boolean integratedKick;
   private final double RATIO_ANALYSIS_RATIO_MIN;
   private final double RATIO_ANALYSIS_RATIO_MAX;

   public AnalysisThread(int threadIndex, ConcurrentHashMap<String, PacketsPerIP> ipBuckets, boolean integratedKick) {
      this.debugEnabled = log.isDebugEnabled();
      this.threadIndex = threadIndex;
      this.ipBuckets = ipBuckets;
      this.integratedKick = integratedKick;
      this.RATIO_ANALYSIS_RATIO_MIN = 1.0D - Params.ANALYSE_RATIOS_SENSITIVITY / 100.0D;
      this.RATIO_ANALYSIS_RATIO_MAX = 1.0D + Params.ANALYSE_RATIOS_SENSITIVITY / 100.0D;
   }

   public void run() {
      log.info("Starting AnalysisThread " + this.threadIndex);
      log.debug("with RATIO_ANALYSIS_RATIO_MIN=" + this.RATIO_ANALYSIS_RATIO_MIN + " and RATIO_ANALYSIS_RATIO_MAX=" + this.RATIO_ANALYSIS_RATIO_MAX);
      this.statsCollectorInstance = StatsCollector.getInstance();

      while(true) {
         this.onePass();

         try {
            Thread.sleep(10L);
         } catch (Exception var2) {
         }
      }
   }

   private void onePass() {
      Set<String> ips = this.ipBuckets.keySet();
      if (ips.size() != 0) {
         PacketsPerIP ppip;
         do {
            int index = this.gen.nextInt(ips.size());
            String key = (String)((String)ips.toArray()[index]);
            ppip = (PacketsPerIP)this.ipBuckets.get(key);
            if (ppip == null) {
               ips = this.ipBuckets.keySet();
            } else {
               try {
                  if (this.debugEnabled) {
                     log.debug("Trying to analyze client IP " + ppip.getIP());
                  }

                  ppip.setUnderAnalysis();
                  if (this.debugEnabled) {
                     log.debug("Analyzing client IP " + ppip.getIP());
                  }

                  ppip.doCleanupDuringAnalysis();
               } catch (PacketsPerIP.BeingAnalyzedException var6) {
                  ppip = null;
               } catch (PacketsPerIP.TimedOutException var7) {
                  this.ipBuckets.remove(key);
                  ppip = null;
               }
            }
         } while(ppip == null);

         PacketsPerIP ppipCopy = (PacketsPerIP)ppip.clone();
         this.findBots(ppipCopy);
         ppip.clearUnderAnalysis();
      }
   }

   public void findBots(PacketsPerIP ppip) {
      String ip = ppip.getIP();
      Set<Integer> socketSet = ppip.getSockets();
      Integer[] sockets = new Integer[socketSet.size()];
      socketSet.toArray(sockets);
      HashSet<Suspect> uniqueSuspects = new HashSet();

      int j;
      for(int i = 0; i < socketSet.size(); ++i) {
         for(j = i + 1; j < socketSet.size(); ++j) {
            PacketsPerClientSocket iPackets = ppip.getPacketsPerClientSocket(sockets[i]);
            PacketsPerClientSocket jPackets = ppip.getPacketsPerClientSocket(sockets[j]);
            boolean suspect = this.isSocketPairSuspect(sockets[i], sockets[j], iPackets, jPackets);
            if (suspect) {
               if (this.debugEnabled) {
                  log.debug("AnalysisThread: IP " + ip + " sockets " + sockets[i] + " and " + sockets[j] + " are suspect");
               }

               Suspect suspectI = new Suspect(ip, sockets[i], iPackets);
               Suspect suspectJ = new Suspect(ip, sockets[j], jPackets);
               uniqueSuspects.add(suspectI);
               uniqueSuspects.add(suspectJ);
            }
         }
      }

      if (uniqueSuspects.size() != 0) {
         Suspect[] uniquesArray = new Suspect[uniqueSuspects.size()];
         uniqueSuspects.toArray(uniquesArray);
         Suspect[] arr$ = uniquesArray;
         int len$ = uniquesArray.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Suspect spt = arr$[i$];
            String username = ClientAddressToUsername.getInstance().getUsername((ClientAddress)spt);
            spt.setUsername(username);
         }

         j = ppip.getPortAndPacketCount()[0];
         SuspectGroup group = new SuspectGroup(uniquesArray, j - uniquesArray.length);
         if (this.integratedKick) {
            KickThread.getInstance(true).addSuspects(group);
         } else {
            BotHunterI.addSuspects(group);
         }

         this.statsCollectorInstance.addSuspects(uniqueSuspects);
      }

   }

   private boolean isSocketPairSuspect(int socketI, int socketJ, PacketsPerClientSocket iPackets, PacketsPerClientSocket jPackets) {
      if (iPackets.size() < Params.MIN_ANALYSIS_PACKETS_PER_SOCKET) {
         return false;
      } else if (jPackets.size() < Params.MIN_ANALYSIS_PACKETS_PER_SOCKET) {
         return false;
      } else {
         if (this.debugEnabled) {
            log.debug("AnalysisThread: comparing sockets " + socketI + "," + socketJ);
            log.debug("    with packet counts " + iPackets.size() + "," + jPackets.size());
         }

         if (Params.SEQUENCE_ANALYSIS) {
            int ii = 0;
            int jj = 0;
            int transitionCount = 0;

            long arrivalDiff;
            for(Long lastArrivalDiff = null; ii < iPackets.size() && jj < jPackets.size(); lastArrivalDiff = arrivalDiff) {
               PacketDetails iPkt = (PacketDetails)iPackets.get(ii);
               PacketDetails jPkt = (PacketDetails)jPackets.get(jj);
               arrivalDiff = jPkt.getArrivalTime() - iPkt.getArrivalTime();
               long tsDiff = jPkt.getTcpTimestamp() - iPkt.getTcpTimestamp();
               if (arrivalDiff * tsDiff < 0L) {
                  if (this.debugEnabled) {
                     log.debug("AnalysisThread: Sockets " + socketI + " and " + socketJ + " don't have consistently ordered timestamps so not suspect");
                  }

                  return false;
               }

               if (lastArrivalDiff != null && arrivalDiff * lastArrivalDiff < 0L) {
                  ++transitionCount;
               }

               if (arrivalDiff > 0L) {
                  ++ii;
               } else {
                  ++jj;
               }
            }

            this.statsCollectorInstance.incrementTotalSequencePairsAnalyzed();
            this.statsCollectorInstance.incrementTotalSequenceTransitions((long)transitionCount);
            if (transitionCount < Params.MIN_INTERLEAVE_TRANSITIONS) {
               if (this.debugEnabled) {
                  log.debug("AnalysisThread: Sockets " + socketI + " and " + socketJ + " have consistently ordered timestamps but they're not interleaved, " + " so not suspect");
               }

               return false;
            }

            if (this.debugEnabled) {
               log.debug("AnalysisThread: Sockets " + socketI + " and " + socketJ + " have consistently ordered interleaved timestamps, transitionCount=" + transitionCount);
            }

            this.statsCollectorInstance.incrementSequenceSuspectPairs();
         }

         if (Params.RATIO_ANALYSIS) {
            double iMean = iPackets.getMeanTcpTimestampOverArrivalTime();
            double jMean = jPackets.getMeanTcpTimestampOverArrivalTime();
            if (iMean / jMean > this.RATIO_ANALYSIS_RATIO_MAX || iMean / jMean < this.RATIO_ANALYSIS_RATIO_MIN) {
               if (this.debugEnabled) {
                  log.debug("AnalysisThread: Sockets " + socketI + " and " + socketJ + " not suspect because their time ratio means are not sufficiently similar");
                  log.debug("i ratio/j ratio=" + iMean / jMean);
               }

               return false;
            }

            if (this.debugEnabled) {
               log.debug("AnalysisThread: Sockets " + socketI + " and " + socketJ + " time ratio means:");
               log.debug("  i ratio=" + iMean);
               log.debug("  j ratio=" + jMean);
               log.debug("  ratio of ratios=" + iMean / jMean);
            }

            this.statsCollectorInstance.incrementRatioSuspectPairs();
         }

         return true;
      }
   }
}
