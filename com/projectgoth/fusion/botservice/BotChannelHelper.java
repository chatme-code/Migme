package com.projectgoth.fusion.botservice;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.slice.BotInstance;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class BotChannelHelper {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotChannelHelper.class));

   public static void updateBots(String username, BotData.BotCommandEnum command, Map<String, BotInstance> channelBots, String channelID) {
      synchronized(channelBots) {
         Iterator i$ = channelBots.values().iterator();

         while(i$.hasNext()) {
            BotInstance botInstance = (BotInstance)i$.next();

            try {
               botInstance.botServiceProxy.sendNotificationToBotsInChannel(channelID, username, command.value());
            } catch (Exception var9) {
               log.error("Could not update bots for user '" + username + "' " + command.name(), var9);
            }
         }

      }
   }

   public static List<BotData> getGames() {
      try {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         return messageEJB.getBots();
      } catch (Exception var1) {
         log.error("Exception in getGames(). ", var1);
         return Collections.EMPTY_LIST;
      }
   }
}
