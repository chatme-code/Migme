package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class ShortTextStatusUserEvent extends UserEvent {
   public static final String EVENT_NAME = "SHORT_TEXT_STATUS";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ShortTextStatusUserEvent.class));
   private String status;

   public ShortTextStatusUserEvent() {
   }

   public ShortTextStatusUserEvent(UserEvent event, String friend) {
      super(event);
      this.status = friend;
   }

   public ShortTextStatusUserEvent(ShortTextStatusUserEventIce event) {
      super((UserEventIce)event);
      this.status = event.status;
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String friend) {
      this.status = friend;
   }

   public ShortTextStatusUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      ShortTextStatusUserEventIce iceEvent = new ShortTextStatusUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.status);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" status [").append(this.status).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(ShortTextStatusUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("status", event.status);
      return map;
   }
}
