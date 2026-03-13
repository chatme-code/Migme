package com.projectgoth.fusion.fashionshow;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.dao.FashionShowDAO;
import com.projectgoth.fusion.jobscheduling.JobSchedulingServiceContext;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class AvatarCandidates {
   private static final String KEY = "AvatarCandidates";
   private static final String KEY_TMP = "AvatarCandidates_tmp";
   private static final String FSHOW_KEY = "FashionShow";
   private static final String FIELD_LEVEL = "ReqdLevel";
   private static final String FIELD_ITEMS = "ReqdAvtrItems";
   private static final String FIELD_DAYS = "ReqdActiveDays";
   private static final String DEFAULT_MIG_LEVEL = "1";
   private static final String DEFAULT_DAYS = "14";
   private static final String DEFAULT_ITEMS = "2";
   private static final int CHUNK_SIZE = 250;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AvatarCandidates.class));

   public static void populateAvatarCandidates() throws Exception {
      Jedis masterInst = null;
      String reqd_level = "1";
      String active_days = "14";
      String item_count = "2";

      try {
         masterInst = Redis.getGamesSlaveInstance();
         Map<String, String> param = masterInst.hgetAll("FashionShow");
         reqd_level = (String)param.get("ReqdLevel");
         active_days = (String)param.get("ReqdActiveDays");
         item_count = (String)param.get("ReqdAvtrItems");
         if (reqd_level == null || reqd_level.length() == 0) {
            reqd_level = "1";
         }

         if (active_days == null || active_days.length() == 0) {
            active_days = "14";
         }

         if (item_count == null || item_count.length() == 0) {
            item_count = "2";
         }

         log.info("Required migLevel: " + reqd_level);
         log.info("Logged in the past " + active_days + " day(s)");
         log.info("Required number of avatar items: " + item_count);
      } catch (Exception var21) {
         log.error("Error while reading the mig level/active days/item count for fashion show: " + var21.getMessage());
      } finally {
         Redis.disconnect(masterInst, log);
      }

      ApplicationContext context = JobSchedulingServiceContext.getContext();
      FashionShowDAO fashionShowDAO = (FashionShowDAO)context.getBean("fashionShowDAO");

      try {
         final List<String> users = fashionShowDAO.getAvatarCandidatesForRedis(Integer.parseInt(active_days), Integer.parseInt(reqd_level), Integer.parseInt(item_count));
         masterInst = Redis.getGamesMasterInstance();
         log.info("Number of candidates in the pool before population: " + masterInst.zcard("AvatarCandidates"));
         masterInst.del("AvatarCandidates_tmp");

         for(final int i = 0; i < users.size(); i += 250) {
            masterInst.pipelined(new PipelineBlock() {
               public void execute() {
                  int j = i;

                  for(int k = 0; k < 250 && j + k < users.size(); ++k) {
                     this.zadd("AvatarCandidates_tmp", Math.random(), (String)users.get(j + k));
                  }

               }
            });
         }

         masterInst.rename("AvatarCandidates_tmp", "AvatarCandidates");
         log.info("Number of candidates in the pool after population: " + masterInst.zcard("AvatarCandidates"));
      } catch (Exception var19) {
         log.error("Pipeline execution error: " + var19.getMessage());
      } finally {
         Redis.disconnect(masterInst, log);
      }

   }
}
