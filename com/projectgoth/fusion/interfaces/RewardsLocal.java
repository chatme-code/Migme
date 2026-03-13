package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface RewardsLocal extends EJBLocalObject {
   int getRewardScoreCap(int var1, RewardProgramData.CategoryEnum var2) throws EJBException;

   HashMap getRewardScoreCap() throws EJBException;

   Boolean sendTrigger(RewardProgramTrigger var1, long var2);
}
