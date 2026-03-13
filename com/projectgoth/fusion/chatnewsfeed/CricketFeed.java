package com.projectgoth.fusion.chatnewsfeed;

import com.projectgoth.fusion.chatnewsfeed.cricket.data.Match;
import com.projectgoth.fusion.chatnewsfeed.cricket.data.MatchScorecard;
import com.projectgoth.fusion.chatnewsfeed.cricket.feed.CricketFeedConfigurer;
import com.projectgoth.fusion.chatnewsfeed.cricket.feed.MatchFeedHelper;
import com.projectgoth.fusion.chatnewsfeed.cricket.feed.MatchScoreFeedHelper;
import com.projectgoth.fusion.chatnewsfeed.util.FileUtil;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.GroupPostData;
import com.projectgoth.fusion.data.MessageData;
import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CricketFeed extends ChatRoomNewsFeedApp {
   protected static final String APP_NAME = "CricketFeed";
   protected static final String CONFIG_FILE = "CricketFeed.cfg";
   private static DocumentBuilderFactory xmlFactory;
   private MatchFeedHelper matchFeedHelper;
   private MatchScoreFeedHelper matchScoreFeedHelper;
   private Map<String, Match> matches = new HashMap();
   private Map<String, MatchScorecard> matchScorecards = new HashMap();
   public static boolean filterBySeries;
   public static List<String> seriesNames;
   private static String username;
   private static String password;
   private static String localDir;
   private static String remoteDir;
   private static String calendarFileName;
   private static String scoreFileSuffix;
   public static String[] generalChatRoomPrefixes;
   public static String[] gameChatRoomPrefixes;
   public static int numberOfRooms;
   public static Map<String, String> teamMappings = new HashMap();
   public static Map<String, String> gamePrefixMappings = new HashMap();
   public static Map<String, String> teamScoreNameMappings = new HashMap();
   public static Map<String, String> seriesNameMappings = new HashMap();
   public static Map<String, Integer> groupModuleMappings = new HashMap();
   public static Map<String, String> groupUsernameMappings = new HashMap();
   public static boolean useMatchNumberForPrefix;

   public CricketFeed() {
      xmlFactory = DocumentBuilderFactory.newInstance();
      this.matchFeedHelper = new MatchFeedHelper();
      this.matchScoreFeedHelper = new MatchScoreFeedHelper();
      Authenticator.setDefault(new CricketFeed.CustomAuthenticator());
   }

   public static DocumentBuilderFactory getXmlFactory() {
      return xmlFactory;
   }

   public static String getConfigFile() {
      return "CricketFeed.cfg";
   }

   public static String getLocalDir() {
      return localDir;
   }

   public static String getRemoteDir() {
      return remoteDir;
   }

   public static boolean isFilterBySeries() {
      return filterBySeries;
   }

   public static void setFilterBySeries(boolean filterBySeries) {
      CricketFeed.filterBySeries = filterBySeries;
   }

   public static List<String> getSeriesNames() {
      return seriesNames;
   }

   public static void setSeriesNames(List<String> seriesNames) {
      CricketFeed.seriesNames = seriesNames;
   }

   public static String getUsername() {
      return username;
   }

   public static void setUsername(String username) {
      CricketFeed.username = username;
   }

   public static String getPassword() {
      return password;
   }

   public static void setPassword(String password) {
      CricketFeed.password = password;
   }

   public static String getCalendarFileName() {
      return calendarFileName;
   }

   public static void setCalendarFileName(String calendarFileName) {
      CricketFeed.calendarFileName = calendarFileName;
   }

   public static String getScoreFileSuffix() {
      return scoreFileSuffix;
   }

   public static void setScoreFileSuffix(String scoreFileSuffix) {
      CricketFeed.scoreFileSuffix = scoreFileSuffix;
   }

   public static void setXmlFactory(DocumentBuilderFactory xmlFactory) {
      CricketFeed.xmlFactory = xmlFactory;
   }

   public static String[] getGeneralChatRoomPrefixes() {
      return generalChatRoomPrefixes;
   }

   public static void setGeneralChatRoomPrefixes(String[] generalChatRoomPrefixes) {
      CricketFeed.generalChatRoomPrefixes = generalChatRoomPrefixes;
   }

   public static String[] getGameChatRoomPrefixes() {
      return gameChatRoomPrefixes;
   }

   public static void setGameChatRoomPrefixes(String[] gameChatRoomPrefixes) {
      CricketFeed.gameChatRoomPrefixes = gameChatRoomPrefixes;
   }

   public static int getNumberOfRooms() {
      return numberOfRooms;
   }

   public static void setNumberOfRooms(int numberOfRooms) {
      CricketFeed.numberOfRooms = numberOfRooms;
   }

   public static Map<String, String> getTeamMappings() {
      return teamMappings;
   }

   public static void setTeamMappings(Map<String, String> teamMappings) {
      CricketFeed.teamMappings = teamMappings;
   }

   public static Map<String, String> getGamePrefixMappings() {
      return gamePrefixMappings;
   }

   public static void setGamePrefixMappings(Map<String, String> gamePrefixMappings) {
      CricketFeed.gamePrefixMappings = gamePrefixMappings;
   }

   public static Map<String, String> getTeamScoreNameMappings() {
      return teamScoreNameMappings;
   }

   public static void setTeamScoreNameMappings(Map<String, String> teamScoreNameMappings) {
      CricketFeed.teamScoreNameMappings = teamScoreNameMappings;
   }

   public static Map<String, String> getSeriesNameMappings() {
      return seriesNameMappings;
   }

   public static void setSeriesNameMappings(Map<String, String> seriesNameMappings) {
      CricketFeed.seriesNameMappings = seriesNameMappings;
   }

   public static Map<String, Integer> getGroupModuleMappings() {
      return groupModuleMappings;
   }

   public static void setGroupModuleMappings(Map<String, Integer> groupModuleMappings) {
      CricketFeed.groupModuleMappings = groupModuleMappings;
   }

   public static Map<String, String> getGroupUsernameMappings() {
      return groupUsernameMappings;
   }

   public static void setGroupUsernameMappings(Map<String, String> groupUsernameMappings) {
      CricketFeed.groupUsernameMappings = groupUsernameMappings;
   }

   public static boolean isUseMatchNumberForPrefix() {
      return useMatchNumberForPrefix;
   }

   public static void setUseMatchNumberForPrefix(boolean useMatchNumberForPrefix) {
      CricketFeed.useMatchNumberForPrefix = useMatchNumberForPrefix;
   }

   public static void setLocalDir(String localDir) {
      CricketFeed.localDir = localDir;
   }

   public static void setRemoteDir(String remoteDir) {
      CricketFeed.remoteDir = remoteDir;
   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      logger.info("CricketFeed version @version@");
      logger.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      CricketFeed feedApp = new CricketFeed();
      if (args.length >= 1) {
         logger.info("Using custom configuration file: " + args[0]);
         feedApp.main("CricketFeed", args, args[0]);
      } else {
         feedApp.main("CricketFeed", args, "CricketFeed.cfg");
      }

   }

   public int run(String[] arg0) {
      super.run(arg0);
      CricketFeedConfigurer configurer = new CricketFeedConfigurer(communicator().getProperties(), this);
      configurer.configure();
      logger.info("Service started");
      CricketFeed.GetMatchFeed matchFeed = new CricketFeed.GetMatchFeed();
      CricketFeed.GetMatchScoreFeed matchScoreFeed = new CricketFeed.GetMatchScoreFeed();
      matchFeed.start();
      matchScoreFeed.start();
      communicator().waitForShutdown();
      if (interrupted()) {
         logger.fatal("CricketFeed " + hostName + ": terminating");
      }

      return 0;
   }

   static {
      logger = Logger.getLogger(ConfigUtils.getLoggerName(CricketFeed.class));
   }

   public class CustomAuthenticator extends Authenticator {
      protected PasswordAuthentication getPasswordAuthentication() {
         return new PasswordAuthentication(CricketFeed.username, CricketFeed.password.toCharArray());
      }
   }

   class GetMatchScoreFeed extends Thread {
      public void run() {
         HashMap copyMatches = new HashMap();

         try {
            sleep(5000L);
         } catch (Exception var11) {
            ChatRoomNewsFeedApp.logger.error("Exception running match score feed during initial wait period", var11);
         }

         while(true) {
            while(true) {
               try {
                  CricketFeed.this.matchScoreFeedHelper.initializeMatchesForFeed(copyMatches, CricketFeed.this.matches, CricketFeed.this.matchScorecards);
                  Iterator i$ = CricketFeed.this.matchScorecards.values().iterator();

                  while(i$.hasNext()) {
                     MatchScorecard scorecard = (MatchScorecard)i$.next();
                     Match match = scorecard.getMatch();
                     if (ChatRoomNewsFeedApp.logger.isDebugEnabled()) {
                        ChatRoomNewsFeedApp.logger.debug("Match picked up: " + match);
                     }

                     if (match.isLive() || !StringUtils.isEmpty(match.getResult()) && !match.isResultReported()) {
                        Document xmlDocument = this.loadScoreFeed(match);
                        if (xmlDocument != null) {
                           this.processScore(scorecard, match, xmlDocument);
                        }
                     } else if (match.isUpcoming() && scorecard.isChangeChatRoomDescription()) {
                        this.setUpcomingMatchState(scorecard);
                     }
                  }

                  sleep(15000L);
               } catch (Exception var12) {
                  ChatRoomNewsFeedApp.logger.error("Exception running match score feed", var12);
               } finally {
                  if (ChatRoomNewsFeedApp.logger.isDebugEnabled()) {
                     ChatRoomNewsFeedApp.logger.debug("Scorecards found: " + CricketFeed.this.matchScorecards.values());
                  }

                  copyMatches.clear();
               }
            }
         }
      }

      public Document loadScoreFeed(Match match) {
         Document xmlDocument = null;
         String fileName = match.getMatchIdentifier() + CricketFeed.scoreFileSuffix;
         File file = new File(CricketFeed.localDir + File.separatorChar + fileName);
         FileUtil.downloadFile(CricketFeed.remoteDir + fileName, file);
         if (file.exists()) {
            try {
               xmlDocument = FileUtil.getDocument(CricketFeed.xmlFactory, file);
            } catch (Exception var6) {
               ChatRoomNewsFeedApp.logger.error("Match score file " + file.getAbsolutePath() + " could not be located. Skipping");
            }
         }

         return xmlDocument;
      }

      private void processScore(MatchScorecard scorecard, Match match, Document xmlDocument) {
         Node inningsNode = CricketFeed.this.matchScoreFeedHelper.getActiveInnings(xmlDocument, "Innings", scorecard);
         if (inningsNode != null && inningsNode.getNodeType() == 1) {
            NamedNodeMap attributesMap = inningsNode.getAttributes();
            CricketFeed.this.matchScoreFeedHelper.getInningsAttributes(scorecard, attributesMap);
            CricketFeed.this.matchScoreFeedHelper.setMatchPreview(scorecard, match, xmlDocument);
            NodeList scoreNodeList = inningsNode.getChildNodes();
            Node scoreNode = CricketFeed.this.matchScoreFeedHelper.getLastNode(scoreNodeList);
            if (scoreNode != null) {
               new ArrayList();
               List<MessageData> messagesToSend = CricketFeed.this.matchScoreFeedHelper.backFillScores(CricketFeed.this.matches, scorecard, scoreNodeList, scoreNode);
               MessageData messageData = CricketFeed.this.matchScoreFeedHelper.getScore(scoreNode, scorecard, CricketFeed.this.matches);
               if (scorecard.getCurrentNode() != -1 && scorecard.getCurrentNode() != scorecard.getLastReadNode()) {
                  this.sendLatestCommentary(scorecard, messagesToSend, messageData);
                  scorecard.setLastReadNode(scorecard.getCurrentNode());
                  scorecard.setLastCommentary(messageData.messageText);
               } else if (!match.isLive() && !match.isResultReported() && !StringUtils.isEmpty(match.getResult())) {
                  this.sendMatchResult(scorecard, messagesToSend);
               } else if (match.isLive() && scorecard.isChangeChatRoomDescription()) {
                  this.setLiveMatchState(scorecard);
               }
            }
         }

      }

      private void sendMatchResult(MatchScorecard scorecard, List<MessageData> messagesToSend) {
         CricketFeed.this.chatRoomNames = CricketFeed.this.matchScoreFeedHelper.createRoomList(scorecard.getMatch(), CricketFeed.generalChatRoomPrefixes, CricketFeed.numberOfRooms);
         CricketFeed.this.matchScoreFeedHelper.generateMatchSummary(scorecard, true);
         CricketFeed.this.matchScoreFeedHelper.appendFinalResult(scorecard, CricketFeed.this.matches, messagesToSend);
         String roomDescription = null;
         if (scorecard.isChangeChatRoomDescription()) {
            ChatRoomNewsFeedApp.logger.info("Room description needs to be updated with result");
            roomDescription = CricketFeed.this.matchScoreFeedHelper.createChatRoomDescription(scorecard);
            if (ChatRoomNewsFeedApp.logger.isInfoEnabled()) {
               ChatRoomNewsFeedApp.logger.info("Room description set: " + roomDescription);
            }
         }

         CricketFeed.this.matchScoreFeedHelper.appendInningsSummary(scorecard, messagesToSend);
         this.sendMessages(messagesToSend, roomDescription);
         this.postMatchResultToGroups(scorecard);
      }

      private void postMatchResultToGroups(MatchScorecard scorecard) {
         Match match = scorecard.getMatch();
         String teaser = CricketFeed.this.matchScoreFeedHelper.createMatchResultPost(scorecard);
         this.createGroupPost(teaser, "Cricket FanZone");
         this.createGroupPost(teaser, match.getTeamA());
         this.createGroupPost(teaser, match.getTeamB());
      }

      private void setUpcomingMatchState(MatchScorecard scorecard) {
         CricketFeed.this.chatRoomNames = CricketFeed.this.matchScoreFeedHelper.createRoomListForUpcomingEvent(scorecard.getMatch(), CricketFeed.this.matches, CricketFeed.numberOfRooms);
         String roomDescription = CricketFeed.this.matchScoreFeedHelper.createChatRoomDescription(scorecard);
         this.updateMatchState(roomDescription);
      }

      private void setLiveMatchState(MatchScorecard scorecard) {
         CricketFeed.this.chatRoomNames = CricketFeed.this.matchScoreFeedHelper.createRoomList(scorecard.getMatch(), CricketFeed.generalChatRoomPrefixes, CricketFeed.numberOfRooms);
         String roomDescription = CricketFeed.this.matchScoreFeedHelper.createChatRoomDescription(scorecard);
         this.updateMatchState(roomDescription);
      }

      private void updateMatchState(String roomDescription) {
         try {
            if (CricketFeed.this.chatRoomNames != null && !StringUtils.isEmpty(roomDescription)) {
               CricketFeed.this.updateChatRoomDescription(roomDescription);
               if (ChatRoomNewsFeedApp.logger.isInfoEnabled()) {
                  ChatRoomNewsFeedApp.logger.info("Room description set: " + roomDescription);
               }
            }
         } catch (Exception var3) {
            ChatRoomNewsFeedApp.logger.error("Could not update description of chat rooms.", var3);
         }

      }

      public void createGroupPost(String teaser, String groupName) {
         try {
            Integer groupModuleId = (Integer)CricketFeed.groupModuleMappings.get(groupName);
            String groupOwner = (String)CricketFeed.groupUsernameMappings.get(groupName);
            String username = StringUtils.isEmpty(groupOwner) ? (String)CricketFeed.groupUsernameMappings.get("Cricket FanZone") : groupOwner;
            if (groupModuleId != null) {
               CricketFeed.this.createGroupPost(username, groupModuleId, teaser, (String)null, GroupPostData.StatusEnum.ACTIVE.value());
               if (ChatRoomNewsFeedApp.logger.isDebugEnabled()) {
                  ChatRoomNewsFeedApp.logger.debug("Message '" + teaser + "' posted to group module " + groupModuleId);
               }
            }
         } catch (Exception var6) {
            ChatRoomNewsFeedApp.logger.warn("Error mapping group module for teams in match " + groupName);
         }

      }

      private boolean sendLatestCommentary(MatchScorecard scorecard, List<MessageData> messagesToSend, MessageData messageData) {
         CricketFeed.this.chatRoomNames = CricketFeed.this.matchScoreFeedHelper.createRoomList(scorecard.getMatch(), CricketFeed.generalChatRoomPrefixes, CricketFeed.numberOfRooms);
         boolean sentScores = false;
         if (messagesToSend != null) {
            if (scorecard.getLastReadNode() == 0) {
               messagesToSend.add(0, CricketFeed.this.matchScoreFeedHelper.createMessageData(scorecard.getMatch().getPreview()));
               if (ChatRoomNewsFeedApp.logger.isDebugEnabled()) {
                  ChatRoomNewsFeedApp.logger.debug("With Preview: " + scorecard.getMatch().getPreview());
               }
            }

            messagesToSend.add(messageData);
         }

         String roomDescription = null;
         if (scorecard.isChangeChatRoomDescription() || !scorecard.isLiveStatusUpdated()) {
            roomDescription = CricketFeed.this.matchScoreFeedHelper.createChatRoomDescription(scorecard);
            scorecard.setLiveStatusUpdated(true);
         }

         sentScores = this.sendMessages(messagesToSend, roomDescription);
         return sentScores;
      }

      private boolean sendMessages(List<MessageData> messages, String roomDescription) {
         try {
            if (CricketFeed.this.chatRoomNames != null) {
               CricketFeed.this.sendMessagesToChatRooms(messages, roomDescription);
               if (ChatRoomNewsFeedApp.logger.isInfoEnabled()) {
                  Iterator i$ = messages.iterator();

                  while(i$.hasNext()) {
                     MessageData messageData = (MessageData)i$.next();
                     ChatRoomNewsFeedApp.logger.info("Message sent: " + messageData.messageText);
                  }
               }

               return true;
            }
         } catch (Exception var5) {
            ChatRoomNewsFeedApp.logger.error("Error occurred while sending messages to one or more chat rooms.", var5);
         }

         return false;
      }
   }

   class GetMatchFeed extends Thread {
      public void run() {
         while(true) {
            try {
               File file = new File(CricketFeed.localDir + File.separatorChar + CricketFeed.calendarFileName);
               FileUtil.downloadFile(CricketFeed.remoteDir + CricketFeed.calendarFileName, file);
               if (file.exists()) {
                  Document xmlDocument = FileUtil.getDocument(CricketFeed.xmlFactory, file);
                  NodeList nodeList = xmlDocument.getElementsByTagName("calendar");
                  if (nodeList != null) {
                     Node node = nodeList.item(0);
                     if (node != null) {
                        CricketFeed.this.matchFeedHelper.processMatches(node, xmlDocument, CricketFeed.this.matches, CricketFeed.seriesNames);
                     }
                  }
               }

               if (ChatRoomNewsFeedApp.logger.isDebugEnabled()) {
                  ChatRoomNewsFeedApp.logger.debug("End of calendar download");
               }

               sleep(60000L);
            } catch (Exception var5) {
               ChatRoomNewsFeedApp.logger.error("Exception running match feed", var5);
            }
         }
      }
   }
}
