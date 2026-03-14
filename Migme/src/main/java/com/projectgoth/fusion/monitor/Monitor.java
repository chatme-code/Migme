package com.projectgoth.fusion.monitor;

import Ice.Application;
import Ice.Properties;
import com.projectgoth.fusion.common.ConfigUtils;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class Monitor extends Application {
   private static final String APP_NAME = "Monitor";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "Monitor.cfg";
   public static Properties properties = null;
   public static String smtpHost;
   public static String smtpRecipients;
   public static String jdbcURL;
   public static Tree tree;
   public static LinkedList<RegistryMonitor> registryMonitors = new LinkedList();
   public static LinkedList<AuthenticationServiceMonitor> authenticationServiceMonitors = new LinkedList();
   public static LinkedList<ObjectCacheMonitor> objectCacheMonitors = new LinkedList();
   public static LinkedList<GatewayMonitor> gatewayMonitors = new LinkedList();
   public static LinkedList<SMSEngineMonitor> smsEngineMonitors = new LinkedList();
   public static LinkedList<MessageLoggerMonitor> messageLoggerMonitors = new LinkedList();
   public static LinkedList<SessionCacheMonitor> sessionCacheMonitors = new LinkedList();
   public static LinkedList<EventSystemMonitor> eventSystemMonitors = new LinkedList();
   public static LinkedList<EventStoreMonitor> eventStoreMonitors = new LinkedList();
   public static LinkedList<ImageServerMonitor> imageServerMonitors = new LinkedList();
   public static LinkedList<EmailAlertMonitor> emailAlertMonitors = new LinkedList();
   public static LinkedList<JobSchedulingServiceMonitor> jobSchedulingMonitors = new LinkedList();
   public static LinkedList<UserNotificationServiceMonitor> userNotificationServiceMonitors = new LinkedList();
   public static LinkedList<BlueLabelServiceMonitor> blueLabelServiceMonitors = new LinkedList();
   public static LinkedList<ReputationServiceMonitor> reputationServiceMonitors = new LinkedList();
   public static LinkedList<BotServiceMonitor> botServiceMonitors = new LinkedList();
   public static LinkedList<EventQueueMonitor> eventQueueMonitors = new LinkedList();
   public static LinkedList<BotHunterMonitor> botHunterMonitors = new LinkedList();
   public static LinkedList<RecommendationDataCollectionServiceMonitor> rdcsMonitors = new LinkedList();
   public static LinkedList<RecommendationGenerationServiceMonitor> rgsMonitors = new LinkedList();
   protected Shell shell;
   protected Display display;
   private TreeItem registriesNode;
   private TreeItem authenticationServiceNode;
   private TreeItem objectCachesNode;
   private TreeItem gatewaysNode;
   private TreeItem smsEngineNode;
   private TreeItem messageLoggerNode;
   private TreeItem sessionCacheNode;
   private TreeItem eventSystemNode;
   private TreeItem eventStoreNode;
   private TreeItem imageServerNode;
   private TreeItem emailAlertNode;
   private TreeItem jobSchedulingServiceNode;
   private TreeItem userNotificationServiceNode;
   private TreeItem blueLabelServiceNode;
   private TreeItem reputationServiceNode;
   private TreeItem botServiceNode;
   private TreeItem eventQueueNode;
   private TreeItem botHunterNode;
   private TreeItem rdcsNode;
   private TreeItem rgsNode;
   private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

   public static void main(String[] args) {
      try {
         Monitor window = new Monitor();
         if (args.length >= 1) {
            window.main("Monitor", args, args[0]);
         } else {
            window.main("Monitor", args, CONFIG_FILE);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public int run(String[] arg0) {
      this.open();
      return 0;
   }

   public void open() {
      this.display = Display.getDefault();
      this.createContents();
      this.shell.open();
      this.shell.layout();
      if (this.loadConfig()) {
         this.initTree();
         this.startMonitors();

         while(!this.shell.isDisposed()) {
            if (!this.display.readAndDispatch()) {
               this.display.sleep();
            }
         }

         this.scheduler.shutdownNow();
         this.display.dispose();
      }
   }

   private boolean loadConfig() {
      properties = communicator().getProperties();
      smtpHost = properties.getProperty("SMTP.Host");
      MessageBox messageBox;
      if (smtpHost.length() == 0) {
         messageBox = new MessageBox(this.shell, 33);
         messageBox.setMessage("You must specify a value for SMTP.Host in " + CONFIG_FILE);
         messageBox.open();
         return false;
      } else {
         smtpRecipients = properties.getProperty("SMTP.Recipients");
         if (smtpRecipients.length() == 0) {
            messageBox = new MessageBox(this.shell, 33);
            messageBox.setMessage("You must specify a value for SMTP.Recipients in " + CONFIG_FILE);
            messageBox.open();
            return false;
         } else {
            jdbcURL = properties.getProperty("JDBC.URL");
            if (jdbcURL.length() == 0) {
               messageBox = new MessageBox(this.shell, 33);
               messageBox.setMessage("You must specify a value for JDBC.URL in " + CONFIG_FILE);
               messageBox.open();
               return false;
            } else {
               return true;
            }
         }
      }
   }

   private void initTree() {
      this.registriesNode = new TreeItem(tree, 0);
      this.registriesNode.setText("Registries");
      this.authenticationServiceNode = new TreeItem(tree, 0);
      this.authenticationServiceNode.setText("Authentication Servers");
      this.objectCachesNode = new TreeItem(tree, 0);
      this.objectCachesNode.setText("Object Caches");
      this.gatewaysNode = new TreeItem(tree, 0);
      this.gatewaysNode.setText("Gateways");
      this.smsEngineNode = new TreeItem(tree, 0);
      this.smsEngineNode.setText("SMSEngines");
      this.messageLoggerNode = new TreeItem(tree, 0);
      this.messageLoggerNode.setText("Message Loggers");
      this.sessionCacheNode = new TreeItem(tree, 0);
      this.sessionCacheNode.setText("Session Caches");
      this.eventQueueNode = new TreeItem(tree, 0);
      this.eventQueueNode.setText("Event Queue Workers");
      this.eventSystemNode = new TreeItem(tree, 0);
      this.eventSystemNode.setText("Event Systems");
      this.eventStoreNode = new TreeItem(tree, 0);
      this.eventStoreNode.setText("Event Stores");
      this.imageServerNode = new TreeItem(tree, 0);
      this.imageServerNode.setText("Image Servers");
      this.emailAlertNode = new TreeItem(tree, 0);
      this.emailAlertNode.setText("Email Alert Apps");
      this.jobSchedulingServiceNode = new TreeItem(tree, 0);
      this.jobSchedulingServiceNode.setText("Job Scheduling Services");
      this.userNotificationServiceNode = new TreeItem(tree, 0);
      this.userNotificationServiceNode.setText("User Notification Services");
      this.blueLabelServiceNode = new TreeItem(tree, 0);
      this.blueLabelServiceNode.setText("Blue Label Services");
      this.reputationServiceNode = new TreeItem(tree, 0);
      this.reputationServiceNode.setText("Reputation Services");
      this.botServiceNode = new TreeItem(tree, 0);
      this.botServiceNode.setText("Bot Services");
      this.botHunterNode = new TreeItem(tree, 0);
      this.botHunterNode.setText("Bot Hunter");
      this.rdcsNode = new TreeItem(tree, 0);
      this.rdcsNode.setText("Recommendation Data Collection Services");
      this.rgsNode = new TreeItem(tree, 0);
      this.rgsNode.setText("Recommendation Generation Services");
   }

   private void startMonitors() {
      long updateInterval = (long)(properties.getPropertyAsIntWithDefault("UpdateInterval", 60) * 1000);
      int numRegistries = properties.getPropertyAsInt("NumRegistries");

      int numAuths;
      for(numAuths = 1; numAuths <= numRegistries; ++numAuths) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("Registry", numAuths);
         RegistryMonitor registryMonitor = new RegistryMonitor(hap.getHostName(), hap.getPort(), this.registriesNode);
         registryMonitors.add(registryMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(registryMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numAuths = properties.getPropertyAsInt("NumAuthenticationServers");

      int numObjectCaches;
      for(numObjectCaches = 1; numObjectCaches <= numAuths; ++numObjectCaches) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("AuthenticationService", numObjectCaches);
         AuthenticationServiceMonitor authenticationServiceMonitor = new AuthenticationServiceMonitor(hap.getHostName(), hap.getPort(), this.authenticationServiceNode);
         authenticationServiceMonitors.add(authenticationServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(authenticationServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numObjectCaches = properties.getPropertyAsInt("NumObjectCaches");

      int numGateways;
      for(numGateways = 1; numGateways <= numObjectCaches; ++numGateways) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("ObjectCache", numGateways);
         ObjectCacheMonitor objectCacheMonitor = new ObjectCacheMonitor(hap.getHostName(), hap.getPort(), this.objectCachesNode);
         objectCacheMonitors.add(objectCacheMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(objectCacheMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numGateways = properties.getPropertyAsInt("NumGateways");

      int numSMSEngines;
      for(numSMSEngines = 1; numSMSEngines <= numGateways; ++numSMSEngines) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("Gateway", numSMSEngines);
         GatewayMonitor gatewayMonitor = new GatewayMonitor(hap.getHostName(), hap.getPort(), this.gatewaysNode);
         gatewayMonitors.add(gatewayMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(gatewayMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numSMSEngines = properties.getPropertyAsInt("NumSMSEngines");

      int numMessageLoggers;
      for(numMessageLoggers = 1; numMessageLoggers <= numSMSEngines; ++numMessageLoggers) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("SMSEngine", numMessageLoggers);
         SMSEngineMonitor smsEngineMonitor = new SMSEngineMonitor(hap.getHostName(), hap.getPort(), this.smsEngineNode);
         smsEngineMonitors.add(smsEngineMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(smsEngineMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numMessageLoggers = properties.getPropertyAsInt("NumMessageLoggers");

      int numSessionCaches;
      for(numSessionCaches = 1; numSessionCaches <= numMessageLoggers; ++numSessionCaches) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("MessageLogger", numSessionCaches);
         MessageLoggerMonitor messageLoggerMonitor = new MessageLoggerMonitor(hap.getHostName(), hap.getPort(), this.messageLoggerNode);
         messageLoggerMonitors.add(messageLoggerMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(messageLoggerMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numSessionCaches = properties.getPropertyAsInt("NumSessionCaches");

      int numEventQueueWorkers;
      for(numEventQueueWorkers = 1; numEventQueueWorkers <= numSessionCaches; ++numEventQueueWorkers) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("SessionCache", numEventQueueWorkers);
         SessionCacheMonitor sessionCacheMonitor = new SessionCacheMonitor(hap.getHostName(), hap.getPort(), this.sessionCacheNode);
         sessionCacheMonitors.add(sessionCacheMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(sessionCacheMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numEventQueueWorkers = properties.getPropertyAsInt("NumEventQueueWorkers");

      int numEventSystems;
      for(numEventSystems = 1; numEventSystems <= numEventQueueWorkers; ++numEventSystems) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("EventQueueWorker", numEventSystems);
         EventQueueMonitor monitor = new EventQueueMonitor(hap.getHostName(), hap.getPort(), this.eventQueueNode);
         eventQueueMonitors.add(monitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(monitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numEventSystems = properties.getPropertyAsInt("NumEventSystems");

      int numEventStores;
      for(numEventStores = 1; numEventStores <= numEventSystems; ++numEventStores) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("EventSystem", numEventStores);
         EventSystemMonitor eventSystemMonitor = new EventSystemMonitor(hap.getHostName(), hap.getPort(), this.eventSystemNode);
         eventSystemMonitors.add(eventSystemMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(eventSystemMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numEventStores = properties.getPropertyAsInt("NumEventStores");

      int numImageServers;
      for(numImageServers = 1; numImageServers <= numEventStores; ++numImageServers) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("EventStore", numImageServers);
         EventStoreMonitor eventStoreMonitor = new EventStoreMonitor(hap.getHostName(), hap.getPort(), this.eventStoreNode);
         eventStoreMonitors.add(eventStoreMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(eventStoreMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numImageServers = properties.getPropertyAsInt("NumImageServers");

      int numEmailAlertApps;
      for(numEmailAlertApps = 1; numEmailAlertApps <= numImageServers; ++numEmailAlertApps) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("ImageServer", numEmailAlertApps);
         ImageServerMonitor imageServerMonitor = new ImageServerMonitor(hap.getHostName(), hap.getPort(), this.imageServerNode);
         imageServerMonitors.add(imageServerMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(imageServerMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numEmailAlertApps = properties.getPropertyAsInt("NumEmailAlertApps");

      int numJobSchedulingServices;
      for(numJobSchedulingServices = 1; numJobSchedulingServices <= numEmailAlertApps; ++numJobSchedulingServices) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("EmailAlert", numJobSchedulingServices);
         EmailAlertMonitor emailAlertMonitor = new EmailAlertMonitor(hap.getHostName(), hap.getPort(), this.emailAlertNode);
         emailAlertMonitors.add(emailAlertMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(emailAlertMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numJobSchedulingServices = properties.getPropertyAsInt("NumJobSchedulingServices");

      int numUserNotificationServices;
      for(numUserNotificationServices = 1; numUserNotificationServices <= numJobSchedulingServices; ++numUserNotificationServices) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("JobSchedulingService", numUserNotificationServices);
         JobSchedulingServiceMonitor jobSchedulingServiceMonitor = new JobSchedulingServiceMonitor(hap.getHostName(), hap.getPort(), this.jobSchedulingServiceNode);
         jobSchedulingMonitors.add(jobSchedulingServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(jobSchedulingServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numUserNotificationServices = properties.getPropertyAsInt("NumUserNotificationServices");

      int numBlueLabelServices;
      for(numBlueLabelServices = 1; numBlueLabelServices <= numUserNotificationServices; ++numBlueLabelServices) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("UserNotificationService", numBlueLabelServices);
         UserNotificationServiceMonitor userNotificationServiceMonitor = new UserNotificationServiceMonitor(hap.getHostName(), hap.getPort(), this.userNotificationServiceNode);
         userNotificationServiceMonitors.add(userNotificationServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(userNotificationServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numBlueLabelServices = properties.getPropertyAsInt("NumBlueLabelServices");

      int numReputationServices;
      for(numReputationServices = 1; numReputationServices <= numBlueLabelServices; ++numReputationServices) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("BlueLabelService", numReputationServices);
         BlueLabelServiceMonitor blueLabelServiceMonitor = new BlueLabelServiceMonitor(hap.getHostName(), hap.getPort(), this.blueLabelServiceNode);
         blueLabelServiceMonitors.add(blueLabelServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(blueLabelServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numReputationServices = properties.getPropertyAsInt("NumReputationServices");

      int numBotServices;
      for(numBotServices = 1; numBotServices <= numReputationServices; ++numBotServices) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("ReputationService", numBotServices);
         ReputationServiceMonitor reputationServiceMonitor = new ReputationServiceMonitor(hap.getHostName(), hap.getPort(), this.reputationServiceNode);
         reputationServiceMonitors.add(reputationServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(reputationServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numBotServices = properties.getPropertyAsInt("NumBotServices");

      int numBotHunters;
      for(numBotHunters = 1; numBotHunters <= numBotServices; ++numBotHunters) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("BotService", numBotHunters);
         BotServiceMonitor botServiceMonitor = new BotServiceMonitor(hap.getHostName(), hap.getPort(), this.botServiceNode);
         botServiceMonitors.add(botServiceMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(botServiceMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numBotHunters = properties.getPropertyAsInt("NumBotHunters");

      int numRDCSs;
      for(numRDCSs = 1; numRDCSs <= numBotHunters; ++numRDCSs) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("BotHunter", numRDCSs);
         BotHunterMonitor botHunterMonitor = new BotHunterMonitor(hap.getHostName(), hap.getPort(), this.botHunterNode);
         botHunterMonitors.add(botHunterMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(botHunterMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numRDCSs = properties.getPropertyAsInt("NumRecommendationDataCollectionServices");

      int numRGSs;
      for(numRGSs = 1; numRGSs <= numRDCSs; ++numRGSs) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("RecommendationDataCollectionService", numRGSs);
         RecommendationDataCollectionServiceMonitor rdcsMonitor = new RecommendationDataCollectionServiceMonitor(hap.getHostName(), hap.getPort(), this.rdcsNode);
         rdcsMonitors.add(rdcsMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(rdcsMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

      numRGSs = properties.getPropertyAsInt("NumRecommendationGenerationServices");

      for(int i = 1; i <= numRGSs; ++i) {
         Monitor.HostAndPort hap = new Monitor.HostAndPort("RecommendationGenerationService", i);
         RecommendationGenerationServiceMonitor rgsMonitor = new RecommendationGenerationServiceMonitor(hap.getHostName(), hap.getPort(), this.rgsNode);
         rgsMonitors.add(rgsMonitor);
         this.scheduler.scheduleWithFixedDelay(new Monitor.MonitorTask(rgsMonitor), 0L, updateInterval, TimeUnit.MILLISECONDS);
      }

   }

   protected void createContents() {
      this.shell = new Shell();
      this.shell.setLayout(new FillLayout());
      this.shell.setSize(500, 375);
      this.shell.setText("Fusion Monitor");
      Menu menu = new Menu(this.shell, 2);
      this.shell.setMenuBar(menu);
      MenuItem fileMenuItem = new MenuItem(menu, 64);
      fileMenuItem.setText("File");
      Menu fileMenu = new Menu(fileMenuItem);
      fileMenuItem.setMenu(fileMenu);
      MenuItem fileExitMenuItem = new MenuItem(fileMenu, 0);
      fileExitMenuItem.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            Monitor.this.shell.dispose();
         }
      });
      fileExitMenuItem.setText("Exit");
      tree = new Tree(this.shell, 2048);
      tree.addListener(17, new Listener() {
         public void handleEvent(Event e) {
            Iterator i$ = Monitor.objectCacheMonitors.iterator();

            while(i$.hasNext()) {
               ObjectCacheMonitor objectCacheMonitor = (ObjectCacheMonitor)i$.next();
               if (e.item.equals(objectCacheMonitor.userObjectsTreeItem)) {
                  objectCacheMonitor.loadUserObjects();
               }
            }

            i$ = Monitor.gatewayMonitors.iterator();

            while(i$.hasNext()) {
               GatewayMonitor gatewayMonitor = (GatewayMonitor)i$.next();
               if (e.item.equals(gatewayMonitor.threadPoolTreeItem) && gatewayMonitor.threadPoolTreeItem.getExpanded()) {
                  gatewayMonitor.threadPoolTreeItem.setText("Thread Pools");
               }
            }

         }
      });
   }

   private class HostAndPort {
      private final String hostName;
      private final Integer port;

      public HostAndPort(String propName, int propIndex) {
         String[] hostAndPort = Monitor.properties.getProperty(propName + propIndex).toUpperCase().split(":");
         this.hostName = hostAndPort[0];
         if (hostAndPort.length > 1) {
            this.port = Integer.valueOf(hostAndPort[1]);
         } else {
            this.port = null;
         }

      }

      public String getHostName() {
         return this.hostName;
      }

      public Integer getPort() {
         return this.port;
      }
   }

   private class MonitorTask implements Runnable {
      private BaseMonitor baseMonitor;

      public MonitorTask(BaseMonitor baseMonitor) {
         this.baseMonitor = baseMonitor;
      }

      public void run() {
         this.baseMonitor.getStats();
         Display.getDefault().asyncExec(this.baseMonitor);
      }
   }
}
