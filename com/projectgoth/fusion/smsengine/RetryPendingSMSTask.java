package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class RetryPendingSMSTask extends TimerTask {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RetryPendingSMSTask.class));
   private SMSEngine engine;
   private Date dateOfLastRetry;
   private int pullMessagesCreatedBefore;

   public RetryPendingSMSTask(SMSEngine engine, int pullMessagesCreatedBefore) {
      this.engine = engine;
      this.pullMessagesCreatedBefore = pullMessagesCreatedBefore;
   }

   public void run() {
      this.retryPendingUserSMS();
      this.retryPendingSystemSMS();
   }

   public void retryPendingUserSMS() {
      List userSMSList = null;

      try {
         if (this.dateOfLastRetry == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(11, -this.pullMessagesCreatedBefore);
            this.dateOfLastRetry = cal.getTime();
         }

         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         userSMSList = messageEJB.getMessages((String)null, Integer.valueOf(MessageType.SMS.value()), MessageData.SendReceiveEnum.SEND.value(), this.dateOfLastRetry, (Integer)null, MessageDestinationData.StatusEnum.PENDING.value());
         this.dateOfLastRetry = new Date();
      } catch (Exception var4) {
         log.warn("Failed to load pending User SMS from database - " + var4.toString());
      }

      if (userSMSList != null && userSMSList.size() != 0) {
         log.debug(userSMSList.size() + " pending User SMS loaded from database");
         Iterator i$ = userSMSList.iterator();

         while(i$.hasNext()) {
            MessageData message = (MessageData)i$.next();
            this.engine.queueDispatchThread(new DispatchThread(this.engine, message));
         }

      } else {
         log.debug("No pending User SMS in database");
      }
   }

   public void retryPendingSystemSMS() {
      List systemSMSList = null;

      try {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         systemSMSList = messageEJB.getSystemSMS((String)null, (Integer)null, (Date)null, SystemSMSData.StatusEnum.PENDING.value());
      } catch (Exception var4) {
         log.warn("Failed to load pending System SMS from database - " + var4.toString());
      }

      if (systemSMSList != null && systemSMSList.size() != 0) {
         log.debug(systemSMSList.size() + " pending System SMS loaded from database");
         Iterator i$ = systemSMSList.iterator();

         while(i$.hasNext()) {
            SystemSMSData systemSMS = (SystemSMSData)i$.next();
            this.engine.queueDispatchThread(new DispatchThread(this.engine, systemSMS));
         }

      } else {
         log.debug("No pending System SMS in database");
      }
   }
}
