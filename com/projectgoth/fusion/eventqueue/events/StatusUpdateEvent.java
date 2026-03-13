package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import java.sql.Connection;
import org.apache.log4j.Logger;

public class StatusUpdateEvent extends Event {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(StatusUpdateEvent.class));

   public StatusUpdateEvent(String username, String statusMessage, int deviceType, int ssoView) {
      super(username, Enums.EventTypeEnum.STATUS_UPDATE_EVENT);
      this.putParameter("statusMessage", statusMessage);
      if (deviceType != -1) {
         this.putParameter("deviceType", "" + deviceType);
      }

      if (ssoView != -1) {
         this.putParameter("ssoView", "" + ssoView);
      }

   }

   public StatusUpdateEvent() {
      super(Enums.EventTypeEnum.STATUS_UPDATE_EVENT);
   }

   public boolean executeInternal() throws Exception {
      User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      int userid = userBean.getUserID(this.eventSubject, (Connection)null, false);
      if (userid == -1) {
         log.error(String.format("Unable to execute event: UserID not found for [%s]", this.eventSubject));
         return false;
      } else {
         String statusMessage = this.getParameter("statusMessage");
         int ssoViewInt = StringUtil.toIntOrDefault(this.getParameter("ssoView"), -1);
         SSOEnums.View ssoView = ssoViewInt == -1 ? null : SSOEnums.View.fromValue(ssoViewInt);
         ClientType type = ClientType.fromValue(StringUtil.toIntOrDefault(this.getParameter("deviceType"), -1));
         Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         String response = contentBean.createMigboTextPostForUser(userid, statusMessage, "", "", "1", type, ssoView);
         log.info("StatusUpdateEvent for [" + this.eventSubject + "][" + userid + "][" + statusMessage + "][" + type + "][" + ssoView + "] : " + response);
         return true;
      }
   }
}
