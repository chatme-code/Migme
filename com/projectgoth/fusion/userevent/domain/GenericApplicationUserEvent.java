package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.slice.GenericApplicationUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent(
   version = 1
)
public class GenericApplicationUserEvent extends UserEvent {
   public static final String WEB_URL_KEY = "web";
   public static final String ANDROID_URL_KEY = "android";
   public static final String J2ME_URL_KEY = "j2me";
   public static final int EVENT_TEXT_MAX_LENGTH = 200;
   public static final String EVENT_NAME = "GENERIC_APP_EVENT";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GenericApplicationUserEvent.class));
   private String text;
   private Map<String, String> urls;

   public GenericApplicationUserEvent() {
   }

   public GenericApplicationUserEvent(UserEvent event, String text) {
      super(event);
      this.text = text;
   }

   public GenericApplicationUserEvent(GenericApplicationUserEventIce event) {
      super((UserEventIce)event);
      this.text = event.text;
      this.urls = event.urls;
   }

   public Map<String, String> getURLs() {
      return this.urls;
   }

   public void setURLs(Map<String, String> urls) {
      this.urls = urls;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public GenericApplicationUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      long eventTimestamp = this.getTimestamp();
      String eventUsername = this.getGeneratingUsername();
      String eventDisplayPicture = null;
      String eventText = this.getText();
      Map eventURLs = this.getURLs();
      GenericApplicationUserEventIce iceEvent = new GenericApplicationUserEventIce(eventTimestamp, eventUsername, (String)eventDisplayPicture, eventText, eventURLs);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" text [").append(this.text).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(GenericApplicationUserEventIce event, ClientType deviceType) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("text", event.text);
      String deviceURL = null;
      switch(deviceType) {
      case AJAX1:
      case AJAX2:
         deviceURL = (String)event.urls.get("web");
         break;
      case ANDROID:
         deviceURL = (String)event.urls.get("android");
         break;
      case MIDP1:
      case MIDP2:
         deviceURL = (String)event.urls.get("j2me");
         break;
      default:
         deviceURL = null;
      }

      if (StringUtil.isBlank(deviceURL)) {
         map.put("url", "");
      } else {
         StringBuffer buffer = (new StringBuffer("(<a href='")).append(deviceURL).append("'>view</a>)");
         map.put("url", buffer.toString());
      }

      return map;
   }
}
