package com.projectgoth.fusion.recommendation.generation;

import Ice.Current;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.recommendation.RecommendationTransform;
import com.projectgoth.fusion.slice.BaseServiceStats;
import com.projectgoth.fusion.slice.RecommendationGenerationServiceStats;
import com.projectgoth.fusion.slice._RecommendationGenerationServiceDisp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import org.apache.log4j.Logger;

public class RecommendationGenerationServiceI extends _RecommendationGenerationServiceDisp {
   private static final Logger log = Logger.getLogger(RecommendationGenerationServiceI.class);
   private IcePrxFinder icePrxFinder;
   private List<RecommendationTransform> transforms;
   private List<Timer> scheduledTimers;
   private final RecommendationGenerationServiceStats stats = new RecommendationGenerationServiceStats();

   public void runTransformation(int transfmID, Current current) {
      log.info("INVOKED VIA JOB SCHEDULER: transformation" + transfmID);
      RecommendationGenerationTaskHS2 task = new RecommendationGenerationTaskHS2(transfmID);
      task.transform();
   }

   public void initialize() throws Exception {
      this.transforms = DAOFactory.getInstance().getRecommendationDAO().getActiveRecommendationTransforms();
      if (log.isDebugEnabled()) {
         log.debug("Loaded " + this.transforms.size() + " transformations");
      }

      this.scheduleTasks();
   }

   public void reinitialize() throws Exception {
      List<RecommendationTransform> newTransforms = DAOFactory.getInstance().getRecommendationDAO().getActiveRecommendationTransforms();
      boolean reschedule = this.transforms.size() != newTransforms.size();
      if (!reschedule) {
         for(int i = 0; i < newTransforms.size(); ++i) {
            RecommendationTransform newT = (RecommendationTransform)newTransforms.get(i);
            RecommendationTransform oldT = (RecommendationTransform)this.transforms.get(i);
            if (newT.getID() != oldT.getID() || newT.getRunInterval() != oldT.getRunInterval()) {
               reschedule = true;
               break;
            }
         }
      }

      if (reschedule) {
         Iterator i$ = this.scheduledTimers.iterator();

         while(i$.hasNext()) {
            Timer timer = (Timer)i$.next();
            timer.cancel();
         }

         this.transforms = newTransforms;
         this.scheduleTasks();
      }

   }

   private void scheduleTasks() {
      try {
         int gap = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RGS_INTERNAL_TIMER_SCHEDULE_GAP_SECONDS);
         this.scheduledTimers = new ArrayList();
         int delayIndex = 0;
         Iterator i$ = this.transforms.iterator();

         while(i$.hasNext()) {
            RecommendationTransform transform = (RecommendationTransform)i$.next();
            if (log.isDebugEnabled()) {
               log.debug("Processing recommendation transformation: id=" + transform.getID());
            }

            Timer timer = new Timer(true);
            RecommendationGenerationTaskHS2 task = new RecommendationGenerationTaskHS2(transform.getID());
            timer.schedule(task, (long)(delayIndex++ * gap * 1000), transform.getRunInterval() * 1000L);
            this.scheduledTimers.add(timer);
            log.info("Scheduled task for transform " + transform.getID() + "(" + transform.getName() + ") with interval=" + transform.getRunInterval());
         }
      } catch (Exception var7) {
         log.error("Exception scheduling Recommendation Generation tasks: e=" + var7, var7);
      }

   }

   void shutdown() {
      log.info("RecommendationGenerationService interrupted, shutting down...");
   }

   public void setIcePrxFinder(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = icePrxFinder;
   }

   public RecommendationGenerationServiceStats getStats() {
      BaseServiceStats baseStats = ServiceStatsFactory.getBaseServiceStats(RecommendationGenerationService.startTime);
      this.stats.hostname = baseStats.hostname;
      this.stats.version = baseStats.version;
      this.stats.jvmTotalMemory = baseStats.jvmTotalMemory;
      this.stats.jvmFreeMemory = baseStats.jvmFreeMemory;
      this.stats.uptime = baseStats.uptime;
      RecommendationGenerationTaskHS2.getStats(this.stats);
      return this.stats;
   }
}
