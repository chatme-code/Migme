package com.projectgoth.fusion.common;

import java.util.UUID;
import org.apache.log4j.Logger;

public class MemCachedDistributedLock {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCachedDistributedLock.class));
   private static final long LOCK_WAIT_INTERVAL = 100L;

   public static void getDistributedLock(String lockID) {
      getDistributedLock(lockID, -1L);
   }

   public static boolean getDistributedLock(String lockID, long waitTimeMillis) {
      long cumWaitTime = 0L;

      while(!MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.DISTRIBUTED_LOCK, lockID, 1)) {
         if (waitTimeMillis >= 0L && waitTimeMillis <= cumWaitTime) {
            return false;
         }

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var6) {
         }

         if (waitTimeMillis >= 0L) {
            cumWaitTime += 100L;
         }
      }

      return true;
   }

   public static void releaseDistributedLock(String lockID) {
      MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.DISTRIBUTED_LOCK, lockID);
   }

   public static MemCachedDistributedLock.LockInstance getDistributedLock(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String lockID, long waitTimeMillis, long lockExpiry) {
      long cumWaitTime = 0L;
      UUID lockInstance = UUID.randomUUID();
      long ttl = lockExpiry < 0L ? keySpace.getCacheTime() : lockExpiry;

      while(!MemCachedClientWrapper.add(keySpace, lockID, lockInstance, ttl)) {
         if (waitTimeMillis >= 0L && waitTimeMillis <= cumWaitTime) {
            return null;
         }

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var12) {
         }

         if (waitTimeMillis >= 0L) {
            cumWaitTime += 100L;
         }
      }

      return new MemCachedDistributedLock.LockInstance(keySpace, lockID, lockInstance);
   }

   public static MemCachedDistributedLock.LockInstance getDistributedLock(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String lockID) {
      return getDistributedLock(keySpace, lockID, -1L, -1L);
   }

   public static MemCachedDistributedLock.LockInstance tryGetDistributedLock(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String lockID, long lockExpiry) {
      return getDistributedLock(keySpace, lockID, 0L, lockExpiry);
   }

   public static class LockInstance {
      private MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace;
      private String lockID;
      private UUID instanceID;

      public LockInstance(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String lockID, UUID instanceID) {
         this.keySpace = keySpace;
         this.lockID = lockID;
         this.instanceID = instanceID;
      }

      public void release(boolean throwIfDifferentOwner) {
         UUID storedInstanceID = (UUID)MemCachedClientWrapper.get(this.keySpace, this.lockID);
         if (storedInstanceID != null && this.instanceID.equals(storedInstanceID)) {
            MemCachedClientWrapper.delete(this.keySpace, this.lockID);
         } else {
            if (throwIfDifferentOwner) {
               throw new IllegalStateException("Cannot release lock " + this.keySpace + ":" + this.lockID + " which is no longer owned by this lock instance");
            }

            MemCachedDistributedLock.log.error("Cannot release lock " + this.keySpace + ":" + this.lockID + " which is no longer owned by this lock instance");
         }

      }
   }
}
