package com.projectgoth.fusion.eventqueue.queues;

import com.projectgoth.fusion.eventqueue.Event;

public interface EventQueueClient {
   void enqueue(Event var1) throws Exception;

   Event getForProcessing() throws Exception;

   void processingCompleted(boolean var1) throws Exception;

   void requeueEventInProcess() throws Exception;

   void setProcessingQueueID(int var1);

   int numberOfEventsPending() throws Exception;

   int numberOfEventsInProcess() throws Exception;

   int flushPendingQueue() throws Exception;

   int flushProcessingQueue() throws Exception;

   void disconnectClient() throws Exception;
}
