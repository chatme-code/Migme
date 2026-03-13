package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

class RewardProgramsImpl implements RewardPrograms {
   private static final int CACHE_TIME = 60000;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardPrograms.class));
   private Map<Integer, RewardProgramData> programs = new ConcurrentHashMap();
   private Map<RewardProgramData.TypeEnum, RewardPrograms.RewardProgramDataList> programsMap = new ConcurrentHashMap();
   private static Semaphore semaphore = new Semaphore(1);
   private volatile long lastUpdated;

   public void loadPrograms() {
      if (this.lastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      log.debug("Loading reward programs from data storage");

      try {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         List<RewardProgramData> newPrograms = contentEJB.getRewardPrograms();
         Iterator i$ = this.programs.keySet().iterator();

         while(i$.hasNext()) {
            Integer oldProgramID = (Integer)i$.next();
            boolean found = false;
            Iterator i$ = newPrograms.iterator();

            while(i$.hasNext()) {
               RewardProgramData program = (RewardProgramData)i$.next();
               if (program.id == oldProgramID) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               this.programs.remove(oldProgramID);
               this.programsMap.clear();
            }
         }

         i$ = newPrograms.iterator();

         while(i$.hasNext()) {
            RewardProgramData program = (RewardProgramData)i$.next();
            this.add(program);
         }

         this.lastUpdated = System.currentTimeMillis();
      } catch (Exception var12) {
         log.error("Unable to load reward programs.Exception:" + var12, var12);
      } finally {
         semaphore.release();
      }

   }

   public void add(RewardProgramData program) {
      this.programs.put(program.id, program);
      this.programsMap.clear();
   }

   public void resetCachedProperties() {
      this.lastUpdated = 0L;
   }

   private void testCacheUpdate() {
      if (System.currentTimeMillis() - this.lastUpdated > 60000L) {
         this.loadPrograms();
      }

   }

   public Collection<RewardProgramData> getAll() {
      this.testCacheUpdate();
      return this.programs.values();
   }

   public RewardProgramData get(int id) {
      this.testCacheUpdate();
      return (RewardProgramData)this.programs.get(id);
   }

   public List<RewardProgramData> get(RewardProgramData.TypeEnum type) {
      return this.getRewardPrograms(type).getRewardPrograms();
   }

   public RewardPrograms.RewardProgramDataList getRewardPrograms(RewardProgramData.TypeEnum type) {
      this.testCacheUpdate();
      RewardPrograms.RewardProgramDataList rewardPrograms = (RewardPrograms.RewardProgramDataList)this.programsMap.get(type);
      if (rewardPrograms == null) {
         List<RewardProgramData> rpdList = new LinkedList();
         boolean needToCheckUserReputation = false;
         Iterator i$ = this.programs.values().iterator();

         while(i$.hasNext()) {
            RewardProgramData program = (RewardProgramData)i$.next();
            if (program.type == type) {
               rpdList.add(program);
               if (program.needToCheckUserReputation()) {
                  needToCheckUserReputation = true;
               }
            }
         }

         rewardPrograms = new RewardPrograms.RewardProgramDataList(rpdList, needToCheckUserReputation);
         this.programsMap.put(type, rewardPrograms);
      }

      return rewardPrograms;
   }
}
