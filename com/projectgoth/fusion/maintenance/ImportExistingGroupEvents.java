package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ExternalizedQueriesProperties;
import com.projectgoth.fusion.common.PortRegistry;
import com.projectgoth.fusion.dao.impl.GroupDAOJDBC;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupEventData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.slice.JobSchedulingServicePrx;
import com.projectgoth.fusion.slice.JobSchedulingServicePrxHelper;
import com.projectgoth.fusion.slice.SMSUserNotification;
import com.projectgoth.fusion.smsengine.SMSControl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ImportExistingGroupEvents extends AbstractDatabaseMaintenance {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ImportExistingGroupEvents.class));
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private static JobSchedulingServicePrx jobSchedulingServicePrx;

   public ImportExistingGroupEvents() throws IOException {
      this.loadProperties();
      this.configureDataSources();
   }

   public List<GroupEventData> getGroupEvents(Connection connection) throws SQLException {
      List<GroupEventData> events = new ArrayList();
      PreparedStatement preparedStatement = connection.prepareStatement("select * from groupevent ge where ge.status = 1 and ge.starttime > now() and id > 39");
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
         do {
            events.add(GroupEventData.fromResultSet(resultSet));
         } while(resultSet.next());
      }

      resultSet.close();
      preparedStatement.close();
      return events;
   }

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

   public void start(String jobSchedulingHostname) throws SQLException, Exception {
      int id = 0;
      Properties queryProperties = new Properties();
      queryProperties.load(new FileInputStream(new File(ConfigUtils.getConfigDirectory() + "queries.properties")));
      ExternalizedQueriesProperties queries = new ExternalizedQueriesProperties();
      queries.setQueries(queryProperties);
      GroupDAOJDBC groupDAO = new GroupDAOJDBC();
      groupDAO.setDataSource(this.masterDataSource);
      groupDAO.setQueries(queries);
      Connection masterConnection = null;

      try {
         masterConnection = this.masterDataSource.getConnection();
         masterConnection.setAutoCommit(false);
         List events = this.getGroupEvents(masterConnection);

         try {
            log.info("got " + events.size() + " events");
            Iterator i$ = events.iterator();

            while(i$.hasNext()) {
               GroupEventData event = (GroupEventData)i$.next();
               int id = event.id;
               log.info("looking at event id [" + event.id + "] with name [" + event.description + "] and start time [" + event.startTime + "]");
               long fiveMinutesBeforeStart = event.startTime.getTime() - 300000L;
               GroupData group = groupDAO.getGroup(event.groupID);
               if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.GROUP_EVENT_NOTIFICATION)) {
                  SMSUserNotification smsNote = new SMSUserNotification(group.name + " alert! \"" + event.description + "\" is about to start. Login to mig33", (String)null, SystemSMSData.SubTypeEnum.GROUP_EVENT_NOTIFICATION.value());
                  log.info("sms note = " + smsNote.message);
                  getJobSchedulingServiceProxy(jobSchedulingHostname).scheduleFusionGroupEventNotificationViaSMS(event.id, event.groupID, fiveMinutesBeforeStart, smsNote);
               }

               String alertNote = group.name + " alert! \"" + event.description + "\" is about to start. Join chatroom now";
               log.info("alert note = " + alertNote);
               getJobSchedulingServiceProxy(jobSchedulingHostname).scheduleFusionGroupEventNotificationViaAlert(event.id, event.groupID, fiveMinutesBeforeStart, alertNote);
               log.info("done with event [" + event.id + "]");
            }
         } catch (Exception var18) {
            log.error("failed to process event id [" + id + "], aborting", var18);
         }
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

   }

   public static void main(String[] args) throws Exception {
      DOMConfigurator.configureAndWatch("log4j.xml");
      ImportExistingGroupEvents main = new ImportExistingGroupEvents();
      main.start(args[0]);
      log.info("shutting ice down... ");
      iceCommunicator.shutdown();
      log.info("ice shut down... ");
   }
}
