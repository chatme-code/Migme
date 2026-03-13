package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.common.PortRegistry;
import com.projectgoth.fusion.slice.GroupEvent;
import com.projectgoth.fusion.slice.JobSchedulingServicePrx;
import com.projectgoth.fusion.slice.JobSchedulingServicePrxHelper;
import java.util.Calendar;
import java.util.TimeZone;

public class JobMaintenance {
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private static JobSchedulingServicePrx jobSchedulingServicePrx;

   public static JobSchedulingServicePrx getJobSchedulingServiceProxy(String hostname) throws Exception {
      if (jobSchedulingServicePrx == null) {
         if (iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         ObjectPrx base = iceCommunicator.stringToProxy("JobSchedulingService: tcp -h " + hostname + " -p " + PortRegistry.JOB_SCHEDULING_SERVICE.getPort() + " -t 5000");
         jobSchedulingServicePrx = JobSchedulingServicePrxHelper.checkedCast(base);
         if (jobSchedulingServicePrx == null) {
            throw new Exception("Invalid JobSchedulingService proxy");
         }
      }

      return jobSchedulingServicePrx;
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.err.println("Usage: JobMaintenance <job scheduling hostname>");
         System.exit(1);
      }

      try {
         String hostname = args[0];
         TimeZone tz = TimeZone.getTimeZone("Africa/Johannesburg");
         Calendar calendar = Calendar.getInstance(tz);
         calendar.set(5, 2);
         calendar.set(2, 4);
         calendar.set(11, 18);
         calendar.set(12, 30);
         calendar.set(13, 0);
         GroupEvent groupEvent = new GroupEvent();
         groupEvent.groupId = 12;
         groupEvent.startTime = calendar.getTimeInMillis();
         groupEvent.chatRoomName = "KXIP Stadium";
         groupEvent.duration = 60;
         groupEvent.status = 1;
         groupEvent.description = "Chat with Yousuf Abdulla, Kings XI Punjab Star";
         int eventId = getJobSchedulingServiceProxy(hostname).scheduleFusionGroupEvent(groupEvent);
         System.out.println("event id " + eventId);
         groupEvent.id = eventId;
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         iceCommunicator.shutdown();
         System.exit(0);
      }

   }
}
