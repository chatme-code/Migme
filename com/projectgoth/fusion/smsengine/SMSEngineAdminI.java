package com.projectgoth.fusion.smsengine;

import Ice.Current;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SMSEngineStats;
import com.projectgoth.fusion.slice._SMSEngineAdminDisp;

public class SMSEngineAdminI extends _SMSEngineAdminDisp {
   private SMSEngine smsEngine;

   public SMSEngineAdminI(SMSEngine smsEngine) {
      this.smsEngine = smsEngine;
   }

   public SMSEngineStats getStats(Current __current) throws FusionException {
      try {
         return this.smsEngine.getStats();
      } catch (Exception var4) {
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }
}
