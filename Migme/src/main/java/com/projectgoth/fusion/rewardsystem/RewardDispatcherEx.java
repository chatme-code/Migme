package com.projectgoth.fusion.rewardsystem;

import com.projectgoth.fusion.data.RewardProgramData;

class RewardDispatcherEx implements Runnable {
   public String dispatchedRewardDataStr;
   public RewardProgramData program;

   public RewardDispatcherEx(RewardProgramData programData, String dispatchedRewardDataStr) {
      this.program = programData;
      this.dispatchedRewardDataStr = dispatchedRewardDataStr;
   }

   public void run() {
      RewardDispatcher dispatcher = RewardDispatcher.getRewardDispatcherFactory().getNewInstance(this.program);
      String redisQueueKey = RewardDispatcher.getQueueName(this.program.id);
      dispatcher.doDispatch(redisQueueKey, this.dispatchedRewardDataStr);
   }
}
