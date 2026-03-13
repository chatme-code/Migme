package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class EmoteCommandFactory {
   private static final int LOCAL_CACHE_RANDOMNESS_TIME = 13;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EmoteCommandFactory.class));
   private static Map<String, EmoteCommandData> commands = new ConcurrentHashMap();
   private static Semaphore semaphore = new Semaphore(1);
   private static long lastUpdated;
   private static Random random = new Random();
   private static IcePrxFinder icePrxFinder;

   public static EmoteCommand getEmoteCommand(String command, ChatSource.ChatType chatType, IcePrxFinder icePrxFinder) {
      if (chatType == null) {
         return null;
      } else {
         long curTime = System.currentTimeMillis();
         long reloadInterval = (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.EMOTE_COMMAND_RELOAD_INTERVAL_IN_SECONDS) * 1000);
         if (curTime - lastUpdated > reloadInterval && curTime - lastUpdated - reloadInterval > (long)(random.nextInt(13) * 1000)) {
            loadCommands();
         }

         EmoteCommandData ecd = (EmoteCommandData)commands.get(command);
         return ecd != null && ecd.supportChatType(chatType) ? ecd.getEmoteCommandHandler().setIcePrxFinder(icePrxFinder) : null;
      }
   }

   public static void loadCommands() {
      if (lastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         java.util.List<EmoteCommandData> newCommands = misEJB.getEmoteCommands();
         Set<String> newKeySet = new HashSet();
         Iterator i$ = newCommands.iterator();

         while(i$.hasNext()) {
            EmoteCommandData emoteCommandData = (EmoteCommandData)i$.next();

            try {
               String key = emoteCommandData.getCommandName().toLowerCase();
               if (commands.containsKey(key)) {
                  log.debug(String.format("updating emote command %s", key));
                  EmoteCommandData oldecd = (EmoteCommandData)commands.get(key);
                  if (oldecd.getHandlerClassName().equals(emoteCommandData.getHandlerClassName())) {
                     emoteCommandData.setEmoteCommandHandler(oldecd.getEmoteCommandHandler());
                  } else {
                     emoteCommandData.instantiateEmoteCommandHandler();
                  }

                  log.debug(String.format("updated emote command %s", key));
               } else {
                  log.info(String.format("adding emote command %s", key));
                  emoteCommandData.instantiateEmoteCommandHandler();
                  log.info(String.format("added emote command %s", key));
               }

               newKeySet.add(key);
               commands.put(key, emoteCommandData);
            } catch (Exception var12) {
               log.error("Unable to load emote[" + emoteCommandData.getCommandName() + "]", var12);
            }
         }

         i$ = commands.keySet().iterator();

         while(i$.hasNext()) {
            String key = (String)i$.next();
            if (!newKeySet.contains(key)) {
               log.info(String.format("removing emote command %s", key));
               commands.remove(key);
               log.info(String.format("removed emote command %s", key));
            }
         }

         lastUpdated = System.currentTimeMillis();
      } catch (Exception var13) {
         log.error("Unable to load emote command", var13);
      } finally {
         semaphore.release();
      }

   }

   static {
      loadCommands();
   }
}
