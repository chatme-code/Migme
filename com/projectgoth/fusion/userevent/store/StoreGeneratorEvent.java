package com.projectgoth.fusion.userevent.store;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.userevent.domain.EventPrivacySetting;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

@Entity(
   version = 1
)
public class StoreGeneratorEvent implements Serializable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(StoreGeneratorEvent.class));
   @PrimaryKey
   private String username;
   private List<UserEvent> events;
   private EventPrivacySetting publishingMask;

   public StoreGeneratorEvent() {
   }

   public StoreGeneratorEvent(String username) {
      this.username = username;
   }

   public StoreGeneratorEvent(String username, EventPrivacySetting mask) {
      this.username = username;
      this.publishingMask = mask;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public List<UserEvent> getEvents() {
      return this.events;
   }

   public void setEvents(List<UserEvent> events) {
      this.events = events;
   }

   public EventPrivacySetting getPublishingMask() {
      return this.publishingMask == null ? new EventPrivacySetting() : this.publishingMask;
   }

   public void setPublishingMask(EventPrivacySetting publishingMask) {
      this.publishingMask = publishingMask;
   }

   private void trimMaximumEvents(int eventsPerUser) {
      if (this.events.size() >= eventsPerUser) {
         if (log.isDebugEnabled()) {
            log.debug("discarding oldest [" + ((UserEvent)this.events.get(this.events.size() - 1)).getTimestamp() + " user event for user [" + this.username + "]");
         }

         this.events.remove(this.events.size() - 1);
      }

   }

   private void trimOlderEvents() {
      long cutoff = System.currentTimeMillis() - 1209600000L;

      while(!this.events.isEmpty() && ((UserEvent)this.events.get(this.events.size() - 1)).getTimestamp() < cutoff) {
         this.events.remove(this.events.get(this.events.size() - 1));
      }

   }

   public void addEvent(int eventsPerUser, UserEvent generatorEvent) {
      if (this.events == null) {
         this.events = new ArrayList(eventsPerUser);
      }

      if (!this.getPublishingMask().applyMask(generatorEvent)) {
         if (log.isDebugEnabled()) {
            log.debug("not adding event [" + generatorEvent + "] since receivingMask [" + this.publishingMask + "] returned false");
         }

      } else if (this.events.isEmpty()) {
         log.debug("adding first event for user [" + this.username + "]");
         this.events.add(generatorEvent);
      } else {
         this.trimMaximumEvents(eventsPerUser);
         this.trimOlderEvents();
         if (this.events.isEmpty()) {
            if (log.isDebugEnabled()) {
               log.debug("adding first event for user [" + this.username + "]");
            }

            this.events.add(generatorEvent);
         } else {
            int index = 0;

            for(Iterator i$ = this.events.iterator(); i$.hasNext(); ++index) {
               UserEvent currentEvent = (UserEvent)i$.next();
               if (generatorEvent.getTimestamp() >= currentEvent.getTimestamp()) {
                  if (log.isDebugEnabled()) {
                     log.debug("adding userevent with date " + generatorEvent.getTimestamp() + " at index " + index);
                  }

                  this.events.add(index, generatorEvent);
                  break;
               }
            }

         }
      }
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("username [").append(this.username).append("] with [").append(this.events != null ? this.events.size() : 0).append("] events");
      return buffer.toString();
   }
}
