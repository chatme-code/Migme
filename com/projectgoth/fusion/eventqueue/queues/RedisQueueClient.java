package com.projectgoth.fusion.eventqueue.queues;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RedisQueue;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.eventqueue.Event;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class RedisQueueClient implements EventQueueClient {
   public static final String TYPE = "redis";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RedisQueueClient.class));
   private String INCOMING_QUEUE = "EventQueue:inbox";
   private String PROCESSING_QUEUE = "EventQueue:active:0";
   RedisQueue queue = null;

   public RedisQueueClient() throws Exception {
      this.queue = RedisQueue.getInstance();
      if (this.queue == null) {
         throw new Exception("Unable to initialize RedisQueue instance");
      }
   }

   public RedisQueueClient(Jedis redisConnection) throws Exception {
      this.queue = RedisQueue.getInstance(redisConnection);
      if (this.queue == null) {
         throw new Exception("Unable to initialize RedisQueue instance");
      }
   }

   public void setProcessingQueueID(int uniqueID) {
      this.PROCESSING_QUEUE = String.format("EventQueue:active:%d", uniqueID);
   }

   public void enqueue(Event e) throws Exception {
      String jsonStr = e.toJSONString();

      try {
         this.queue.push(this.INCOMING_QUEUE, jsonStr, false);
      } catch (Exception var4) {
         log.error("Exception caught while trying to enqueue : " + jsonStr, var4);
      }

   }

   public Event getForProcessing() throws Exception {
      Event e = null;

      try {
         String jsonStr = this.queue.rpoplpushBlocking(this.INCOMING_QUEUE, this.PROCESSING_QUEUE, 50, 0);
         e = Event.fromJSONString(jsonStr);
         if (e == null && !StringUtil.isBlank(jsonStr)) {
            log.info(String.format("Unable to parse [%s]. Requeuing the message", jsonStr));
            this.queue.rpoplpush(this.PROCESSING_QUEUE, this.INCOMING_QUEUE);
         }

         return e;
      } catch (Exception var4) {
         String msg = String.format("Error retrieving event for processing [%s]", var4.getMessage());
         log.error(msg, var4);
         throw new Exception(msg);
      }
   }

   public void processingCompleted(boolean b) throws Exception {
      this.queue.pop(this.PROCESSING_QUEUE, false);
   }

   public void requeueEventInProcess() throws Exception {
      this.queue.rpoplpush(this.PROCESSING_QUEUE, this.INCOMING_QUEUE);
   }

   public int numberOfEventsPending() throws Exception {
      return this.queue.size(this.INCOMING_QUEUE);
   }

   public int numberOfEventsInProcess() throws Exception {
      return this.queue.size(this.PROCESSING_QUEUE);
   }

   public int flushPendingQueue() throws Exception {
      return this.queue.purgeQueue(this.INCOMING_QUEUE);
   }

   public int flushProcessingQueue() throws Exception {
      return this.queue.purgeQueue(this.PROCESSING_QUEUE);
   }

   public void disconnectClient() throws Exception {
      this.queue.disconnect();
   }
}
