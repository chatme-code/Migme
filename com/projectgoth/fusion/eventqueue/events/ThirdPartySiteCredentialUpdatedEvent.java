package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import org.apache.log4j.Logger;

public class ThirdPartySiteCredentialUpdatedEvent extends Event {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GameEvent.class));

   public ThirdPartySiteCredentialUpdatedEvent() {
      super(Enums.EventTypeEnum.THIRD_PARTY_SITE_CREDENTIAL_UPDATED);
   }

   public ThirdPartySiteCredentialUpdatedEvent(int userId) {
      super(Enums.EventTypeEnum.THIRD_PARTY_SITE_CREDENTIAL_UPDATED);
      this.putParameter("userId", Integer.toString(userId));
   }

   public boolean executeInternal() throws Exception {
      String userIdStr = this.getParameter("userId");
      String pathPrefix = String.format("/user/%s/thirdpartysites/cache", userIdStr);
      MigboApiUtil apiUtil = MigboApiUtil.getInstance();
      apiUtil.delete(pathPrefix);
      log.info("ThirdPartySiteCredentialUpdatedEvent submitted successfully. User ID: " + userIdStr);
      return true;
   }
}
