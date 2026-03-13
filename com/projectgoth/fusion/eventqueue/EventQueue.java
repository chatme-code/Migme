package com.projectgoth.fusion.eventqueue;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.eventqueue.queues.EventQueueClient;
import com.projectgoth.fusion.eventqueue.queues.MemoryQueueClient;
import com.projectgoth.fusion.eventqueue.queues.RedisQueueClient;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class EventQueue {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventQueue.class));

   protected static EventQueueClient getNewClient() {
      String queueType = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.QUEUE_TYPE);

      try {
         if (StringUtil.isBlank(queueType)) {
            log.warn("System property 'fusion.eventqueue.queuetype' not set. Using RedisQueueClient as default");
            return new RedisQueueClient();
         }

         if (queueType.equals("redis")) {
            return new RedisQueueClient();
         }

         if (queueType.equals("memory")) {
            return new MemoryQueueClient();
         }
      } catch (Exception var2) {
         log.error("Unable to create EventQueue client :" + var2.getMessage(), var2);
      }

      return null;
   }

   public static EventQueueClient getClient() {
      return getNewClient();
   }

   public static void enqueueSingleEvent(Event e) {
      if (SystemProperty.getBool("EventQueueEnabled", false)) {
         EventQueueClient client = null;

         try {
            client = getClient();
            client.enqueue(e);
         } catch (Exception var12) {
            log.error("Unable to enqueue event: " + var12.getMessage(), var12);
         } finally {
            if (client != null) {
               try {
                  client.disconnectClient();
               } catch (Exception var11) {
                  client = null;
               }
            }

         }

      }
   }

   public static void enqueueMultipleEvents(List<Event> eventList) {
      if (SystemProperty.getBool("EventQueueEnabled", false)) {
         EventQueueClient client = null;

         try {
            client = getClient();
            Iterator i$ = eventList.iterator();

            while(i$.hasNext()) {
               Event e = (Event)i$.next();
               client.enqueue(e);
            }
         } catch (Exception var13) {
            log.error("Unable to enqueue multiple events : " + var13.getMessage(), var13);
         } finally {
            if (client != null) {
               try {
                  client.disconnectClient();
               } catch (Exception var12) {
                  client = null;
               }
            }

         }

      }
   }
}
