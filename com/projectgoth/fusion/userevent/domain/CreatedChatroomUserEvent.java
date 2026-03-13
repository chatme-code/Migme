package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.CreatedChatroomUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class CreatedChatroomUserEvent extends UserEvent {
   public static final String EVENT_NAME = "CREATE_PUBLIC_CHATROOM";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CreatedChatroomUserEvent.class));
   private String chatroom;

   public CreatedChatroomUserEvent() {
   }

   public CreatedChatroomUserEvent(UserEvent event, String chatroom) {
      super(event);
      this.chatroom = chatroom;
   }

   public CreatedChatroomUserEvent(CreatedChatroomUserEventIce event) {
      super((UserEventIce)event);
      this.chatroom = event.chatroom;
   }

   public String getChatroom() {
      return this.chatroom;
   }

   public void setChatroom(String friend) {
      this.chatroom = friend;
   }

   public CreatedChatroomUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      CreatedChatroomUserEventIce iceEvent = new CreatedChatroomUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.chatroom);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" chatroom [").append(this.chatroom).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(CreatedChatroomUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("chatroom", event.chatroom);
      return map;
   }
}
