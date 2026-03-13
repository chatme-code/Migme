package com.projectgoth.fusion.bothunter;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.BotHunterPrx;
import com.projectgoth.fusion.slice.BotHunterPrxHelper;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.slice.SuspectGroupIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;

public class KickThread implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(KickThread.class));
   private static final String UNKNOWN_USER = "<unknown>";
   private static final String NEWLINE = "\n";
   private static final String COLON = ":";
   private static KickThread instance;
   private AtomicBoolean bContinue = new AtomicBoolean(true);
   private ConcurrentLinkedQueue<SuspectGroup> suspectQueue = new ConcurrentLinkedQueue();
   private HashMap<String, Long> lastReportedTimes = new HashMap();
   private boolean integratedKick = true;
   private boolean debugEnabled;
   private Communicator communicator;
   private Random randomKickAction = new Random(System.currentTimeMillis());
   private BotHunterPrx botHunterPrx;
   private RegistryPrx registryPrx;
   private ObjectCachePrx ocPrx;

   private KickThread(boolean integratedKick) {
      this.integratedKick = integratedKick;
      this.debugEnabled = log.isDebugEnabled();
   }

   public static synchronized KickThread getInstance(boolean integratedKick) {
      if (instance == null) {
         instance = new KickThread(integratedKick);
      }

      return instance;
   }

   public void terminate() {
      this.bContinue.set(false);
   }

   public void addSuspects(SuspectGroup grp) {
      if (this.debugEnabled) {
         log.debug("KickThread: adding " + grp.getMembers().length + " suspects");
      }

      this.suspectQueue.add(grp);
   }

   public void run() {
      log.info("KickThread starting with AutoKick=" + Params.AUTOKICK);
      log.info("and integrated kick=" + this.integratedKick);
      if (Params.AUTOKICK) {
         try {
            log.info("Connecting to fusion registry");
            this.connectToRegistry();
         } catch (Exception var3) {
            log.info("Can't connect to fusion registry!", var3);
         }
      }

      if (this.integratedKick) {
         this.runIntegratedKick();
      } else {
         try {
            this.runAsIceClient();
         } catch (Exception var2) {
            log.error("Exception running as ice client", var2);
         }
      }

   }

   private void runAsIceClient() throws Exception {
      log.info("Connecting to bot hunter");
      this.connectToBotHunter();

      do {
         if (this.debugEnabled) {
            log.debug("Retrieving suspect groups from BotHunter");
         }

         SuspectGroupIce[] suspectGroupIces = this.botHunterPrx.getLatestSuspects();
         if (this.debugEnabled) {
            log.debug("Number of suspect groups retrieved from BotHunter=" + suspectGroupIces.length);
         }

         SuspectGroupIce[] arr$ = suspectGroupIces;
         int len$ = suspectGroupIces.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SuspectGroupIce groupIce = arr$[i$];
            SuspectGroup group = new SuspectGroup(groupIce);
            this.reportSuspectGroup(group);
         }

         try {
            Thread.sleep(100L);
         } catch (Exception var7) {
         }
      } while(this.bContinue.get());

   }

   private void runIntegratedKick() {
      do {
         if (!this.suspectQueue.isEmpty()) {
            SuspectGroup suspects = (SuspectGroup)this.suspectQueue.remove();
            this.reportSuspectGroup(suspects);
         }

         try {
            Thread.sleep(100L);
         } catch (Exception var2) {
         }
      } while(this.bContinue.get());

   }

   private void reportSuspectGroup(SuspectGroup suspects) {
      StringBuffer sb = new StringBuffer();
      boolean atLeastOneNewSuspect = false;
      Suspect[] arr$ = suspects.getMembers();
      int len$ = arr$.length;

      int len$;
      for(len$ = 0; len$ < len$; ++len$) {
         Suspect s = arr$[len$];
         String clientAddr = s.getClientIP() + ":" + s.getClientPort();
         Long lastReportedAt = (Long)this.lastReportedTimes.get(clientAddr);
         if (lastReportedAt != null && (System.currentTimeMillis() - lastReportedAt) / 1000L <= (long)Params.DUPLICATE_REPORT_INTERVAL_SECS) {
            if (this.debugEnabled) {
               log.debug("Client ip:port " + clientAddr + " recently reported");
            }
         } else {
            this.lastReportedTimes.put(clientAddr, System.currentTimeMillis());
            atLeastOneNewSuspect = true;
         }

         String displayUser = s.getUsername();
         if (displayUser == null || displayUser.length() == 0) {
            displayUser = "<unknown>";
         }

         sb.append(s.getClientIP() + " User " + displayUser + " clientPort " + s.getClientPort() + " meanTcpTimestamp " + s.getMeanTcpTimestamp() + " meanTcpTimestamp/arrivalTime " + s.getMeanTcpTimestampOverArrivalTime() + " lastHeardFrom/secondsAgo " + (System.currentTimeMillis() - s.getLastAddedTo()) / 1000L + " lastWinscaleSeen " + s.getLastWinscaleSeen() + " claimedClientType " + s.getClaimedClientType() + "\n");
      }

      if (atLeastOneNewSuspect) {
         String ip = suspects.getClientIP();
         log.info("suspectGroupIP " + ip + " suspectClientPorts " + suspects.getMembers().length + " nonSuspectClientPorts " + suspects.getInnocentPortCount() + ":" + "\n" + sb.toString());
         if (Params.AUTOKICK) {
            Suspect[] arr$ = suspects.getMembers();
            len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Suspect s = arr$[i$];
               if (s.getUsername() != null) {
                  this.doAutokick(s.getUsername());
               }
            }
         }
      }

   }

   private void connectToRegistry() throws Exception {
      log.info("Calling Ice.Util.initialize");
      this.communicator = Util.initialize(new String[0]);
      log.info("Called Ice.Util.initialize");
      String registryAddress = Params.REGISTRY_PROXY;
      ObjectPrx basePrx = this.communicator.stringToProxy(registryAddress);
      log.info("Connecting to " + registryAddress);
      log.info("basePrx=" + basePrx);
      log.info("Casting basePrx to registryPrx");
      this.registryPrx = RegistryPrxHelper.uncheckedCast(basePrx);
      log.info("Got registryPrx = " + this.registryPrx);
      this.ocPrx = this.registryPrx.getLowestLoadedObjectCache();
      log.info("Got object cache proxy = " + this.ocPrx);
   }

   private void connectToBotHunter() throws Exception {
      log.info("Calling Ice.Util.initialize");
      this.communicator = Util.initialize(new String[0]);
      log.info("Called Ice.Util.initialize");
      String botHunterAddress = Params.BOT_HUNTER_PROXY;
      ObjectPrx basePrx = this.communicator.stringToProxy(botHunterAddress);
      log.info("Connecting to " + botHunterAddress);
      log.info("basePrx=" + basePrx);
      this.botHunterPrx = BotHunterPrxHelper.uncheckedCast(basePrx);
   }

   private void doAutokick(String username) {
      try {
         log.info("Autokicking user " + username);
         UserPrx userPrx = this.ocPrx.createUserObject(username);
         if (this.debugEnabled) {
            log.debug("Got user into the ObjectCache ok: user proxy=" + userPrx);
         }

         String bs = null;
         switch(this.randomKickAction.nextInt(4)) {
         case 0:
            bs = "migme is currently experiencing heavy load. We value your loyalty. Please try again later.";
            break;
         case 1:
            bs = "Please check your network settings and try again.";
            break;
         case 2:
            bs = "User loyalty heuristic rewards program -- reward allocation code 7!";
            break;
         case 3:
            log.info("Letting off user " + username);
            return;
         }

         log.info("Disconnecting user=" + userPrx);
         userPrx.disconnect(bs);
         log.info("Disconnected user=" + userPrx);
      } catch (Exception var4) {
         log.error("Exception kicking user " + username, var4);
      }

   }
}
