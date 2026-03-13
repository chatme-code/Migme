package com.projectgoth.fusion.messageswitchboard;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.BaseServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageSwitchboardStats;
import com.projectgoth.fusion.slice._MessageSwitchboardAdminDisp;

public class MessageSwitchboardAdminI extends _MessageSwitchboardAdminDisp {
   public MessageSwitchboardStats getStats(Current __current) throws FusionException {
      try {
         return this.getStatsInner();
      } catch (Exception var4) {
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }

   private MessageSwitchboardStats getStatsInner() {
      BaseServiceStats baseStats = ServiceStatsFactory.getBaseServiceStats(MessageSwitchboard.startTime);
      MessageSwitchboardStats stats = new MessageSwitchboardStats();
      return stats;
   }
}
