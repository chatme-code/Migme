package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.rmi.RemoteException;
import java.util.HashMap;
import javax.ejb.EJBObject;

public interface Rewards extends EJBObject {
   int getRewardScoreCap(int var1, RewardProgramData.CategoryEnum var2) throws RemoteException;

   HashMap getRewardScoreCap() throws RemoteException;

   Boolean sendTrigger(RewardProgramTrigger var1, long var2) throws RemoteException;
}
