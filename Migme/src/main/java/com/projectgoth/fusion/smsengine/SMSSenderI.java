package com.projectgoth.fusion.smsengine;

import Ice.Current;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SystemSMSDataIce;
import com.projectgoth.fusion.slice._SMSSenderDisp;

public class SMSSenderI extends _SMSSenderDisp {
   private SMSEngine engine;

   public SMSSenderI(SMSEngine engine) {
      this.engine = engine;
   }

   public void sendSMS(MessageDataIce message, long delay, Current __current) throws FusionException {
      this.engine.queueDispatchThread(new DispatchThread(this.engine, new MessageData(message)), delay);
   }

   public void sendSystemSMS(SystemSMSDataIce message, long delay, Current __current) throws FusionException {
      this.engine.queueDispatchThread(new DispatchThread(this.engine, new SystemSMSData(message)), delay);
   }
}
