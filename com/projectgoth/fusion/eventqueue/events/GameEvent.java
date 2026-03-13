package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.sql.Connection;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class GameEvent extends Event {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GameEvent.class));

   public GameEvent() {
      super(Enums.EventTypeEnum.GAME_EVENT);
   }

   public GameEvent(String username, String applicationId, String gameEventJson) {
      super(username, Enums.EventTypeEnum.GAME_EVENT);
      this.putParameter("applicationId", applicationId);
      this.putParameter("gameEventJSONString", gameEventJson);
   }

   public boolean executeInternal() throws Exception {
      log.info(String.format("Looking up userid for username [%s]", this.eventSubject));
      User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      int userid = userBean.getUserID(this.eventSubject, (Connection)null, false);
      if (userid == -1) {
         log.error(String.format("Unable to execute event: UserID not found for [%s]", this.eventSubject));
         return false;
      } else {
         log.info(String.format("Found userid [%d] for username[%s]", userid, this.eventSubject));
         String jsonStr = this.getParameter("gameEventJSONString");
         String pathPrefix = String.format("/user/%d/gameevent", userid);
         log.info("Making API call to POST " + pathPrefix);
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         JSONObject response = apiUtil.post(pathPrefix, jsonStr);
         log.info(String.format("Game Event [%s] [%d-%d-%d] for [%s] submitted successfully: response : %s", this.type.description, userid, this.type.value, this.timestamp, this.eventSubject, response.toString()));
         return true;
      }
   }
}
