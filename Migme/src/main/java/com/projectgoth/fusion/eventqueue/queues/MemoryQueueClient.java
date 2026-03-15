package com.projectgoth.fusion.eventqueue.queues;

import com.projectgoth.fusion.eventqueue.Event;
import java.util.ArrayList;

public class MemoryQueueClient implements EventQueueClient {
   public static final String TYPE = "memory";
   static ArrayList<Event> incomingQueue;
   static ArrayList<Event> processingQueue;

   public MemoryQueueClient() {
      if (incomingQueue == null) {
         incomingQueue = new ArrayList();
         processingQueue = new ArrayList();
      }

   }

   public void enqueue(Event e) {
      incomingQueue.add(e);
   }

   public int numberOfEventsPending() {
      return incomingQueue.size();
   }

   public int numberOfEventsInProcess() {
      return processingQueue.size();
   }

   public Event getForProcessing() {
      if (this.numberOfEventsPending() > 0) {
         Event e = (Event)incomingQueue.remove(0);
         processingQueue.add(e);
         return e;
      } else {
         return null;
      }
   }

   public void processingCompleted(boolean b) {
      processingQueue.remove(0);
   }

   public void requeueEventInProcess() {
   }

   public void setProcessingQueueID(int uniqueID) {
   }

   public int flushPendingQueue() {
      int size = incomingQueue.size();
      incomingQueue.clear();
      return size;
   }

   public int flushProcessingQueue() {
      int size = processingQueue.size();
      processingQueue.clear();
      return size;
   }

   public void disconnectClient() throws Exception {
      throw new UnsupportedOperationException();
   }
}
