package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;

@Persistent
public class GeneratorEvent implements Serializable {
   private long timestamp;
   private UserEventType type;
   private String text;

   public GeneratorEvent() {
   }

   public GeneratorEvent(UserEventIce event) {
      this.timestamp = event.timestamp;
      this.text = event.text;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
   }

   public UserEventType getType() {
      return this.type;
   }

   public void setType(UserEventType type) {
      this.type = type;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public UserEventIce toIceEvent() {
      UserEventIce iceEvent = new UserEventIce();
      iceEvent.timestamp = this.timestamp;
      iceEvent.text = this.text;
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("timestamp [").append(this.timestamp).append("] ");
      buffer.append("type [").append(this.type).append("] ");
      buffer.append("text [").append(this.text).append("]");
      return buffer.toString();
   }
}
