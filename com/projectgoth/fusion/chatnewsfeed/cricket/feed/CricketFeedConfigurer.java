package com.projectgoth.fusion.chatnewsfeed.cricket.feed;

import Ice.Properties;
import com.projectgoth.fusion.chatnewsfeed.CricketFeed;
import com.projectgoth.fusion.chatnewsfeed.util.FileUtil;
import com.projectgoth.fusion.common.ConfigUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

public class CricketFeedConfigurer {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(CricketFeed.class));
   CricketFeed cricketFeed;
   Properties properties;

   public CricketFeedConfigurer(Properties properties, CricketFeed cricketFeed) {
      this.cricketFeed = cricketFeed;
      this.properties = properties;
   }

   public void configure() {
      this.setupFeedParameters();
      this.setChatRoomProperties();
      this.setSeriesProperties();
      this.setTeamNameMappings();
      this.cleanupRemoteServer();
      this.cleanupLocalDirectory();
   }

   private void setChatRoomProperties() {
      CricketFeed.numberOfRooms = this.properties.getPropertyAsIntWithDefault("NumberOfRooms", 25);
      CricketFeed.generalChatRoomPrefixes = this.properties.getProperty("GeneralChatRoomPrefixes").split("[;]");
      CricketFeed.gameChatRoomPrefixes = this.properties.getProperty("GameChatRoomPrefixes").split("[;]");
   }

   private void setupFeedParameters() {
      String configFile = CricketFeed.getConfigFile();
      String username = this.properties.getProperty("UserName");
      String password = this.properties.getProperty("Pass");
      if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
         CricketFeed.setUsername(this.properties.getProperty("UserName"));
         CricketFeed.setPassword(this.properties.getProperty("Pass"));
         String localDir = this.properties.getProperty("LocalDir");
         String remoteDir = this.properties.getProperty("RemoteDir");
         if (!StringUtils.isEmpty(localDir) && !StringUtils.isEmpty(remoteDir)) {
            CricketFeed.setLocalDir(localDir);
            CricketFeed.setRemoteDir(remoteDir);
            String calendarFileName = this.properties.getProperty("CalendarFileName");
            if (StringUtils.isEmpty(calendarFileName)) {
               throw new RuntimeException("Feed file name missing! Check " + configFile);
            } else {
               CricketFeed.setCalendarFileName(calendarFileName);
               String scoreFileSuffix = this.properties.getProperty("ScoreFileSuffix");
               if (StringUtils.isEmpty(scoreFileSuffix)) {
                  throw new RuntimeException("Score file suffix missing! Check " + configFile);
               } else {
                  CricketFeed.setScoreFileSuffix(scoreFileSuffix);
               }
            }
         } else {
            throw new RuntimeException("Local/remote directory path missing! Check " + configFile);
         }
      } else {
         throw new RuntimeException("Login credentials missing! Check " + configFile);
      }
   }

   private void setSeriesProperties() {
      String filterBySeriesValue = this.properties.getProperty("FilterBySeries");
      boolean filterBySeries = !StringUtils.isEmpty(filterBySeriesValue) && filterBySeriesValue.equalsIgnoreCase("true");
      if (filterBySeries) {
         try {
            List<String> seriesNames = Arrays.asList(this.properties.getProperty("SeriesNames").split("[;]"));

            String seriesName;
            for(Iterator i$ = seriesNames.iterator(); i$.hasNext(); seriesName = seriesName.trim()) {
               seriesName = (String)i$.next();
            }

            CricketFeed.setSeriesNames(seriesNames);
         } catch (Exception var7) {
            logger.error("No series filters provided. Reading all matches...");
            filterBySeries = false;
         }
      }

      String seriesMappingsProperty = this.properties.getProperty("SeriesNameMappings");
      if (!StringUtils.isEmpty(seriesMappingsProperty)) {
         String[] seriesNameMappingsArray = seriesMappingsProperty.split("[;]");

         for(int i = 0; i < seriesNameMappingsArray.length; ++i) {
            String[] seriesNameMapping = seriesNameMappingsArray[i].split(":");
            CricketFeed.seriesNameMappings.put(seriesNameMapping[0].trim(), seriesNameMapping[1].trim());
         }
      }

      String useMatchNumber = this.properties.getProperty("UseMatchNumberForPrefix");
      CricketFeed.useMatchNumberForPrefix = !StringUtils.isEmpty(useMatchNumber) && useMatchNumber.equalsIgnoreCase("true");
   }

   private void setTeamNameMappings() {
      String teamNameConfigProperty = this.properties.getProperty("TeamPrefixMappings");
      String[] teamScoreNamesConfig;
      if (!StringUtils.isEmpty(teamNameConfigProperty)) {
         String[] teamNameMappings = teamNameConfigProperty.split("[;]");

         for(int i = 0; i < teamNameMappings.length; ++i) {
            teamScoreNamesConfig = teamNameMappings[i].split(":");
            CricketFeed.teamMappings.put(teamScoreNamesConfig[0].trim(), teamScoreNamesConfig[1].trim());
         }
      }

      String gamePrefixConfigProperty = this.properties.getProperty("GamePrefixMappings");
      if (!StringUtils.isEmpty(gamePrefixConfigProperty)) {
         String[] gamePrefixConfig = gamePrefixConfigProperty.split("[;]");

         for(int i = 0; i < gamePrefixConfig.length; ++i) {
            String[] gamePrefixMapping = gamePrefixConfig[i].split(":");
            CricketFeed.gamePrefixMappings.put(gamePrefixMapping[0].trim(), gamePrefixMapping[1].trim());
         }
      }

      String teamScoreNameProperty = this.properties.getProperty("TeamScoreNameMappings");
      if (!StringUtils.isEmpty(teamScoreNameProperty)) {
         teamScoreNamesConfig = teamScoreNameProperty.split("[;]");

         for(int i = 0; i < teamScoreNamesConfig.length; ++i) {
            String[] teamNameMapping = teamScoreNamesConfig[i].split(":");
            CricketFeed.teamScoreNameMappings.put(teamNameMapping[0].trim(), teamNameMapping[1].trim());
         }
      }

      this.mapTeamGroupModules();
   }

   private void mapTeamGroupModules() {
      String teamGroupModuleProperty = this.properties.getProperty("GroupModules");
      if (!StringUtils.isEmpty(teamGroupModuleProperty)) {
         String[] teamGroupModulesConfig = teamGroupModuleProperty.split("[;]");

         for(int i = 0; i < teamGroupModulesConfig.length; ++i) {
            String[] teamModuleMapping = teamGroupModulesConfig[i].split(":");
            if (!StringUtils.isEmpty(teamModuleMapping[1])) {
               try {
                  int moduleId = Integer.parseInt(teamModuleMapping[1].trim());
                  CricketFeed.groupModuleMappings.put(teamModuleMapping[0].trim(), moduleId);
               } catch (Exception var6) {
                  logger.warn("Invalid group module mapping for " + teamModuleMapping[0], var6);
               }
            }
         }
      }

      String groupUsernameProperty = this.properties.getProperty("GroupUsernames");
      if (!StringUtils.isEmpty(groupUsernameProperty)) {
         String[] groupUsernamesConfig = groupUsernameProperty.split("[;]");

         for(int i = 0; i < groupUsernamesConfig.length; ++i) {
            String[] groupUserNameMapping = groupUsernamesConfig[i].split(":");
            CricketFeed.groupUsernameMappings.put(groupUserNameMapping[0].trim(), groupUserNameMapping[1].trim());
         }
      }

   }

   private void cleanupRemoteServer() {
      String turnOnCleanup = this.properties.getProperty("TurnOnRemoteCleanup");
      boolean cleanup = !StringUtils.isEmpty(turnOnCleanup) && turnOnCleanup.equalsIgnoreCase("true");
      if (cleanup) {
         logger.info("Remote server cleanup is turned on");
         String ftpUser = this.properties.getProperty("FTPUser");
         String ftpPass = this.properties.getProperty("FTPPass");
         String ftpDir = this.properties.getProperty("FTPDir");
         String remoteServer = this.properties.getProperty("RemoteServer");
         String remoteServerTimeZone = this.properties.getProperty("RemoteServerTimeZone");
         this.checkMissingFTPProperty(ftpUser, "FTPUser");
         this.checkMissingFTPProperty(ftpPass, "FTPPass");
         this.checkMissingFTPProperty(ftpDir, "FTPDir");
         this.checkMissingFTPProperty(remoteServer, "RemoteServer");
         this.checkMissingFTPProperty(remoteServerTimeZone, "RemoteServerTimeZone");
         List<String> fileTypes = new ArrayList();
         fileTypes.add(".xml");
         List<String> filesToExclude = new ArrayList();
         filesToExclude.add(CricketFeed.getCalendarFileName());
         FileUtil.deleteOldRemoteFiles(remoteServer, remoteServerTimeZone, ftpDir, ftpUser, ftpPass, fileTypes, filesToExclude);
      } else {
         logger.info("Remote server cleanup is turned off");
      }

   }

   private void checkMissingFTPProperty(String name, String propertyName) {
      if (StringUtils.isEmpty(name)) {
         logger.error("Missing FTP property: '" + propertyName + "'. cannot perform server cleanup. Check " + CricketFeed.getConfigFile());
      }

   }

   private void cleanupLocalDirectory() {
      List<String> fileTypes = new ArrayList();
      fileTypes.add(".xml");
      List<String> filesToExclude = new ArrayList();
      filesToExclude.add(CricketFeed.getCalendarFileName());
      FileUtil.deleteOldLocalFiles(CricketFeed.getLocalDir(), fileTypes, filesToExclude);
   }
}
