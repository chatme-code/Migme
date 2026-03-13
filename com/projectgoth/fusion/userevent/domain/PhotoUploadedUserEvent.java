package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.slice.PhotoUploadedUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class PhotoUploadedUserEvent extends UserEvent {
   public static final String EVENT_NAME = "PHOTO_UPLOADED";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PhotoUploadedUserEvent.class));
   private int scrapbookid;
   private String title;

   public PhotoUploadedUserEvent() {
   }

   public PhotoUploadedUserEvent(UserEvent event, int scrapbookid, String title) {
      super(event);
      this.scrapbookid = scrapbookid;
      this.title = title;
   }

   public PhotoUploadedUserEvent(PhotoUploadedUserEventIce event) {
      super((UserEventIce)event);
      this.scrapbookid = event.scrapbookid;
      this.title = event.title;
   }

   public int getScrapbookid() {
      return this.scrapbookid;
   }

   public void setScrapbookid(int scrapbookid) {
      this.scrapbookid = scrapbookid;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public PhotoUploadedUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      PhotoUploadedUserEventIce iceEvent = new PhotoUploadedUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.scrapbookid, this.title);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" scrapbookid [").append(this.scrapbookid).append("]");
      buffer.append(" title [").append(this.title).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(PhotoUploadedUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("scrapbookid", Integer.toString(event.scrapbookid));
      map.put("title", StringUtil.stripHTML(event.title));
      return map;
   }
}
