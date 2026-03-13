package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.common.SingleRateLimiter;

public class ChatUserRateLimit {
   private SingleRateLimiter sentBinaryDataLimiter;
   private SingleRateLimiter receivedBinaryDataLimiter;

   public ChatUserRateLimit(ChatObjectManagerUser objectManager) {
      int sendRateLimitTime = objectManager.getProperties().getPropertyAsIntWithDefault("sendRateLimitTime", 1) * 1000 * 60;
      int sendRateLimitAmount = objectManager.getProperties().getPropertyAsIntWithDefault("sendRateLimitAmount", 5);
      int receiveRateLimitTime = objectManager.getProperties().getPropertyAsIntWithDefault("receiveRateLimitTime", 1) * 1000 * 60;
      int receiveRateLimitAmount = objectManager.getProperties().getPropertyAsIntWithDefault("receiveRateLimitAmount", 5);
      this.sentBinaryDataLimiter = new SingleRateLimiter((long)sendRateLimitTime, (long)sendRateLimitAmount);
      this.receivedBinaryDataLimiter = new SingleRateLimiter((long)receiveRateLimitTime, (long)receiveRateLimitAmount);
   }

   public boolean onReceive() {
      return this.receivedBinaryDataLimiter.onEvent(System.currentTimeMillis());
   }

   public boolean onSend() {
      return this.sentBinaryDataLimiter.onEvent(System.currentTimeMillis());
   }
}
