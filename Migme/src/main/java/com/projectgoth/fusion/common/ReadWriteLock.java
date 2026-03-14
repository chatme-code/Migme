package com.projectgoth.fusion.common;

public class ReadWriteLock {
   private int givenLocks = 0;
   private int numWaitingReaders = 0;

   public synchronized void getReadLock() {
      ++this.numWaitingReaders;

      while(this.givenLocks == -1) {
         try {
            this.wait();
         } catch (InterruptedException var2) {
         }
      }

      --this.numWaitingReaders;
      ++this.givenLocks;
   }

   public synchronized void getWriteLock() {
      while(this.givenLocks != 0 || this.numWaitingReaders != 0) {
         try {
            this.wait();
         } catch (InterruptedException var2) {
         }
      }

      this.givenLocks = -1;
   }

   public synchronized void releaseLock() {
      if (this.givenLocks != 0) {
         if (this.givenLocks == -1) {
            this.givenLocks = 0;
         } else {
            --this.givenLocks;
         }

         this.notifyAll();
      }
   }
}
