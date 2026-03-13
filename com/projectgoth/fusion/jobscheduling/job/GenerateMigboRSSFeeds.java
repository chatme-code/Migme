package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GenerateMigboRSSFeeds implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GenerateMigboRSSFeeds.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext arg0) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (!semaphoreAquired) {
            log.warn("Another job is still generating RSS Feeds. Exiting...");
            return;
         }

         log.info("RSS Feeds Generation [START]");
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         log.info("Retrieving list of RSSFeedForUsers from Migbo Dataservice");
         JSONObject obj = apiUtil.get("/user/@all/rss");
         log.debug(String.format("Received JSON Response fro migbo-datsvc : %s ", obj.toString()));
         JSONObject data = obj.getJSONObject("data");
         JSONArray rssList = data.getJSONArray("rss");
         HashMap<String, Set<String>> map = new HashMap();

         for(int i = 0; i < rssList.length(); ++i) {
            JSONObject rssFeedForUser = rssList.getJSONObject(i);
            int userid = rssFeedForUser.getInt("userId");
            String feedURL = rssFeedForUser.getString("url");
            log.debug(String.format("Found userid[%d] url [%s]", userid, feedURL));
            Set<String> s = (Set)map.get("" + userid);
            if (s == null) {
               s = new HashSet();
               map.put("" + userid, s);
            }

            ((Set)s).add(feedURL);
         }

         log.info(String.format("Found %d users to Process", map.size()));
         Iterator i$ = map.keySet().iterator();

         while(i$.hasNext()) {
            String useridstr = (String)i$.next();
            Set<String> urls = (Set)map.get(useridstr);
            int userid = StringUtil.toIntOrDefault(useridstr, -1);
            if (userid == -1) {
               log.warn(String.format("Error parsing userid [%s] invalid userid", useridstr));
            } else {
               StringBuffer buffer = new StringBuffer();
               Iterator<String> iter = urls.iterator();
               buffer.append("{ 'urls':[");

               while(iter.hasNext()) {
                  buffer.append("'");
                  buffer.append((String)iter.next());
                  buffer.append("'");
                  if (iter.hasNext()) {
                     buffer.append(",");
                  }
               }

               buffer.append("] }");
               String postData = buffer.toString();
               String pathPrefix = String.format("/user/%d/rss", userid);
               boolean success = false;

               try {
                  success = apiUtil.postAndCheckOk(pathPrefix, postData);
               } catch (MigboApiUtil.MigboApiException var23) {
                  log.error("Unable to generate RSS Feeds -  MigboApiException: " + var23.getMessage(), var23);
               }

               if (success) {
                  log.info(String.format("Successfully generated RSS Feeds [%s] for [%d] via migbo-datasvc [%s]", postData, userid, pathPrefix));
               } else {
                  log.warn(String.format("Failed to generate RSS Feeds [%s] for [%d] via  migbo-datasvc [%s]", postData, userid, pathPrefix));
               }
            }
         }

         log.info("RSS Feeds Generation [COMPLETE]");
      } catch (Exception var24) {
         log.error("Unable to generate RSS Feeds - UnexpectedException: " + var24.getMessage() + ". Terminating Job.", var24);
         throw new JobExecutionException(var24);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }
}
