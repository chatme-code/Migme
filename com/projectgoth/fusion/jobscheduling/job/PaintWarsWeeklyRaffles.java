package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.paintwars.ItemData;
import com.projectgoth.fusion.paintwars.Painter;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import redis.clients.jedis.Jedis;

public class PaintWarsWeeklyRaffles implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaintWarsWeeklyRaffles.class));
   private static final int NUM_OF_WINNERS = 5;
   private static final String RAFFLE_CANDIDATES_KEY = "PaintWars:RaffleCandidates";
   private static final String CURRENT_WEEK_WINNERS_KEY = "PaintWars:CurrentWeekWinners";
   private static final String PREVIOUS_WEEK_WINNERS_KEY = "PaintWars:PreviousWeekWinners";

   public void execute(JobExecutionContext context) throws JobExecutionException {
      String[] winners = this.pickWinners();
      String message = "List of Paint Wars weekly raffle winners: \n";

      for(int i = 0; i < winners.length; ++i) {
         this.giveItem(winners[i]);
         message = message + winners[i] + "\n";
      }

      log.info(message);
      this.updateWinnerStats(winners);
   }

   private String[] pickWinners() {
      String[] raffleCandidates = this.getRaffleCandidates();
      HashSet<String> winners = new HashSet();
      if (raffleCandidates.length <= 5) {
         for(int i = 0; i < raffleCandidates.length; ++i) {
            winners.add(raffleCandidates[i]);
         }
      } else {
         Random randomizer = new Random();

         while(winners.size() < 5) {
            int winningIndex = randomizer.nextInt(raffleCandidates.length);
            String winner = raffleCandidates[winningIndex];
            winners.add(winner);
         }
      }

      return (String[])winners.toArray(new String[0]);
   }

   private String[] getRaffleCandidates() {
      Jedis slaveInstance = null;

      try {
         slaveInstance = Redis.getGamesSlaveInstance();
      } catch (Exception var3) {
         log.warn("Unable to load game configuration");
      }

      if (slaveInstance == null) {
         log.warn("Unable to get games slave instance");
         return new String[0];
      } else {
         Set<String> raffleCandidates = slaveInstance.zrange("PaintWars:RaffleCandidates", 0L, -1L);
         Redis.disconnect(slaveInstance, log);
         return (String[])raffleCandidates.toArray(new String[0]);
      }
   }

   private void giveItem(String username) {
      try {
         if (!Painter.isInventoryFull(username)) {
            int itemId = this.pickRandomItem();
            Painter.addItemToInventory(username, itemId);
            log.info("Giving item [" + itemId + "] to [" + username + "]");
            this.sendEmailNotification(username, itemId);
         } else {
            log.info("Unable to give away Paint Wars prize to: [" + username + "] because the inventory was full");
         }
      } catch (FusionException var3) {
         log.warn("Unable to give away Paint Wars prize to: " + username);
         log.warn(var3.getMessage());
      }

   }

   private int pickRandomItem() throws FusionException {
      Vector<ItemData> specialItems = Painter.getSpecialItems();
      Random generator = new Random();
      int randomIndex = generator.nextInt(specialItems.size() - 1);
      ItemData item = (ItemData)specialItems.get(randomIndex);
      int itemId = item.getId();
      return itemId;
   }

   private void sendEmailNotification(String username, int itemId) {
      try {
         String itemName = Painter.getSpecialItemName(itemId);
         if (itemName == null) {
            log.warn("Unable to find special item name");
            return;
         }

         Message messageBean = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         String destination = messageBean.getUserEmailAddress(username);
         String subject = "Paint Wars Weekly Raffles";
         String content = "Congratulations! You are a Winner of the Paint Wars Weekly Raffles! You won " + itemName + ". Go to your Game Page to check it out now!";
         messageBean.sendEmailFromNoReply(destination, subject, content);
      } catch (FusionException var8) {
         log.warn(var8.getMessage());
      } catch (CreateException var9) {
         log.warn(var9.getMessage());
      } catch (EJBException var10) {
         log.warn(var10.getMessage());
      } catch (RemoteException var11) {
         log.warn(var11.getMessage());
      }

   }

   private void updateWinnerStats(String[] winners) {
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getGamesMasterInstance();
      } catch (Exception var5) {
         log.warn("Unable to load game configuration");
      }

      if (masterInstance != null) {
         masterInstance.del("PaintWars:RaffleCandidates");
         String currentTime = Long.toString(Calendar.getInstance().getTimeInMillis() / 1000L);
         masterInstance.hset("PaintWars", "LastRaffleTime", currentTime);
         masterInstance.del("PaintWars:PreviousWeekWinners");
         masterInstance.sunionstore("PaintWars:PreviousWeekWinners", new String[]{"PaintWars:CurrentWeekWinners"});
         masterInstance.del("PaintWars:CurrentWeekWinners");

         for(int i = 0; i < winners.length; ++i) {
            masterInstance.sadd("PaintWars:CurrentWeekWinners", new String[]{winners[i]});
         }

         Redis.disconnect(masterInstance, log);
      } else {
         log.error("Unable to clear the raffle candidates");
      }

   }
}
