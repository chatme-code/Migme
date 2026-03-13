package com.projectgoth.fusion.bothunter;

import Ice.Current;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SuspectGroupIce;
import com.projectgoth.fusion.slice._BotHunterDisp;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BotHunterI extends _BotHunterDisp {
   private static ConcurrentLinkedQueue<SuspectGroup> suspectQueue = new ConcurrentLinkedQueue();

   public SuspectGroupIce[] getLatestSuspects(Current __current) throws FusionException {
      int size = suspectQueue.size();
      SuspectGroupIce[] groups = new SuspectGroupIce[size];

      for(int i = 0; i < size; ++i) {
         SuspectGroup group = (SuspectGroup)suspectQueue.remove();
         groups[i] = group.toIceObject();
      }

      return groups;
   }

   public static void addSuspects(SuspectGroup suspects) {
      suspectQueue.add(suspects);
   }
}
