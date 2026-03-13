package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.GiftShowerUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent(
   version = 1
)
public class GiftShowerUserEvent extends UserEvent {
   public static final String EVENT_NAME = "GIFT_SHOWER";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftShowerUserEvent.class));
   private String recipient;
   private String giftName;
   private int virtualGiftReceivedId;
   private int totalRecipients;

   public GiftShowerUserEvent() {
   }

   public GiftShowerUserEvent(UserEvent event, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients) {
      super(event);
      this.recipient = recipient;
      this.giftName = giftName;
      this.virtualGiftReceivedId = virtualGiftReceivedId;
      this.totalRecipients = totalRecipients;
   }

   public GiftShowerUserEvent(GiftShowerUserEventIce event) {
      super((UserEventIce)event);
      this.recipient = event.recipient;
      this.giftName = event.giftName;
      this.virtualGiftReceivedId = event.virtualGiftReceivedId;
      this.totalRecipients = event.totalRecipients;
   }

   public String getRecipient() {
      return this.recipient;
   }

   public int getTotalRecipients() {
      return this.totalRecipients;
   }

   public int getVirtualGiftReceivedId() {
      return this.virtualGiftReceivedId;
   }

   public String getGiftName() {
      return this.giftName;
   }

   public GiftShowerUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      GiftShowerUserEventIce iceEvent = new GiftShowerUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.recipient, this.giftName, this.virtualGiftReceivedId, this.totalRecipients);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" recipient [").append(this.recipient).append("]");
      buffer.append(" giftName [").append(this.giftName).append("]");
      buffer.append(" virtualGiftReceivedId [").append(this.virtualGiftReceivedId).append("]");
      buffer.append(" totalRecipients [").append(this.totalRecipients).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(GiftShowerUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("recipient", event.recipient);
      map.put("giftName", event.giftName);
      map.put("virtualGiftReceivedId", Integer.toString(event.virtualGiftReceivedId));
      map.put("totalRecipients", Integer.toString(event.totalRecipients - 1));
      return map;
   }
}
