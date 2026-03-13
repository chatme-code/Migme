package com.projectgoth.fusion.chatnewsfeed.cricket.feed;

import com.projectgoth.fusion.chatnewsfeed.CricketFeed;
import com.projectgoth.fusion.chatnewsfeed.cricket.data.Match;
import com.projectgoth.fusion.chatnewsfeed.cricket.data.MatchScorecard;
import com.projectgoth.fusion.chatnewsfeed.util.XmlUtil;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.data.MessageData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchScoreFeedHelper {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(CricketFeed.class));
   MatchSummaryFeedHelper matchSummaryFeedHelper = new MatchSummaryFeedHelper();

   public Node getLastNode(NodeList nodeList) {
      Node node = null;
      if (nodeList != null && nodeList.getLength() > 0) {
         node = nodeList.item(nodeList.getLength() - 1);
      }

      return node;
   }

   public Node getActiveInnings(Document xmlDocument, String tag, MatchScorecard scorecard) {
      Node lastInningsNode = null;
      NodeList nodeList = xmlDocument.getElementsByTagName(tag);
      if (nodeList != null && nodeList.getLength() > 0) {
         int pos = nodeList.getLength() - 1;

         for(lastInningsNode = nodeList.item(pos); pos >= 0 && !this.isActiveInnings(lastInningsNode, scorecard); lastInningsNode = nodeList.item(pos--)) {
         }
      }

      return lastInningsNode;
   }

   public void setMatchPreview(MatchScorecard scorecard, Match match, Document xmlDocument) {
      if (scorecard.getLastReadNode() == 0 && !StringUtils.isEmpty(scorecard.getInnings()) && "First".equals(scorecard.getInnings())) {
         Node preview = XmlUtil.getLastNode(xmlDocument, "Preview");
         if (preview != null) {
            match.setPreview(" >> " + preview.getTextContent());
         }
      }

   }

   public boolean isActiveInnings(Node inningsNode, MatchScorecard scorecard) {
      boolean isActiveInnings = true;
      if (inningsNode != null && inningsNode.getNodeType() == 1) {
         Node lastChild = inningsNode.getLastChild();
         isActiveInnings = lastChild != null;
      } else {
         isActiveInnings = false;
      }

      return isActiveInnings;
   }

   public void getInningsAttributes(MatchScorecard scorecard, NamedNodeMap attributesMap) {
      Node number = attributesMap.getNamedItem("Number");
      if (number != null) {
         String latestInnings = number.getTextContent();
         if (!StringUtils.isEmpty(scorecard.getInnings()) && !StringUtils.isEmpty(latestInnings) && !scorecard.getInnings().equals(latestInnings)) {
            scorecard.setInningsChange(true);
            scorecard.setTotal(0);
            scorecard.setWickets(0);
            scorecard.setScore("0/0");
         } else {
            scorecard.setInningsChange(false);
         }

         scorecard.setInnings(latestInnings);
      }

      Node batting = attributesMap.getNamedItem("Batting");
      if (batting != null) {
         scorecard.setBattingTeam(batting.getTextContent());
      }

      Node bowling = attributesMap.getNamedItem("Bowling");
      if (bowling != null) {
         scorecard.setBowlingTeam(bowling.getTextContent());
      }

   }

   public void initializeMatchesForFeed(Map<String, Match> copyMatches, Map<String, Match> matches, Map<String, MatchScorecard> matchScorecards) {
      synchronized(this) {
         if (!matches.isEmpty()) {
            copyMatches.putAll(matches);
         }
      }

      if (logger.isDebugEnabled()) {
         logger.debug("Starting a new round of commentary...");
      }

      Match updatedMatch;
      MatchScorecard scorecard;
      if (copyMatches != null) {
         for(Iterator i$ = copyMatches.values().iterator(); i$.hasNext(); matchScorecards.put(updatedMatch.getMatchIdentifier(), scorecard)) {
            updatedMatch = (Match)i$.next();
            scorecard = (MatchScorecard)matchScorecards.get(updatedMatch.getMatchIdentifier());
            if (scorecard == null) {
               scorecard = new MatchScorecard(updatedMatch);
            } else {
               Match thisMatch = scorecard.getMatch();
               this.updateMatchTransitionStatus(updatedMatch, thisMatch, scorecard);
               thisMatch.setLive(updatedMatch.isLive());
               thisMatch.setResult(updatedMatch.getResult());
               thisMatch.setState(updatedMatch.getState());
               thisMatch.setUpcoming(updatedMatch.isUpcoming());
            }
         }
      }

   }

   private void updateMatchTransitionStatus(Match updatedMatch, Match thisMatch, MatchScorecard scorecard) {
      boolean isMatchUpcoming = updatedMatch.isUpcoming();
      boolean isMatchJustLive = !thisMatch.isLive() && updatedMatch.isLive();
      boolean isMatchJustEnded = thisMatch.isLive() && !updatedMatch.isLive() && !StringUtils.isEmpty(updatedMatch.getResult());
      boolean isLiveMatchStateChanged = updatedMatch.isLive() && !StringUtils.isEmpty(updatedMatch.getState()) && (!StringUtils.isEmpty(thisMatch.getState()) && !thisMatch.getState().equals(updatedMatch.getState()) || StringUtils.isEmpty(thisMatch.getState()));
      if (!isMatchUpcoming && !isMatchJustLive && !isLiveMatchStateChanged && !isMatchJustEnded) {
         scorecard.setChangeChatRoomDescription(false);
      } else {
         scorecard.setChangeChatRoomDescription(true);
      }

   }

   public MessageData getScore(Node node, MatchScorecard scorecard, Map<String, Match> matches) {
      MessageData messageData = null;
      if (node.getNodeType() == 1) {
         int nodeId = this.getId(node);
         if (nodeId > 0) {
            boolean isNewScoreAvailable = this.checkNewScoreAvailable(scorecard, nodeId);
            if (isNewScoreAvailable) {
               this.getOverDetails(node, scorecard);
               messageData = this.getCommentary(node, scorecard, matches);
               if (messageData != null && !StringUtils.isEmpty(messageData.messageText)) {
                  scorecard.setCurrentNode(nodeId);
               }
            }
         }
      }

      return messageData;
   }

   public MessageData getScoreWithoutChecks(Node node, MatchScorecard scorecard, Map<String, Match> matches, StringBuilder message) {
      MessageData messageData = null;
      if (node.getNodeType() == 1) {
         int nodeId = this.getId(node);
         if (nodeId > 0) {
            this.getOverDetails(node, scorecard);
            messageData = this.getCommentaryWithoutChecks(node, scorecard, matches, message);
         }
      }

      return messageData;
   }

   public int getId(Node node) {
      int nodeId = -1;
      if (node.getNodeType() == 1) {
         Node id = node.getAttributes().getNamedItem("Id");
         if (id != null) {
            nodeId = this.toIntNodeId(id);
         }
      }

      return nodeId;
   }

   private int toIntNodeId(Node id) {
      int nodeId = Integer.parseInt(id.getTextContent());
      return nodeId;
   }

   private void getOverDetails(Node node, MatchScorecard scorecard) {
      Node over = node.getAttributes().getNamedItem("Over");
      scorecard.setOver(over == null ? "" : over.getTextContent());
      Node ball = node.getAttributes().getNamedItem("Ball");
      scorecard.setBall(ball == null ? "" : ball.getTextContent());
   }

   private boolean checkNewScoreAvailable(MatchScorecard scorecard, int nodeId) {
      return scorecard.getLastReadNode() < nodeId || scorecard.isInningsChange();
   }

   private MessageData getCommentaryWithoutChecks(Node node, MatchScorecard scorecard, Map<String, Match> matches, StringBuilder message) {
      this.appendTeamInfo(scorecard, message);
      MessageData messageData = this.parseScoreInfo(node, scorecard, matches, message);
      return messageData;
   }

   private MessageData getCommentary(Node node, MatchScorecard scorecard, Map<String, Match> matches) {
      StringBuilder message = new StringBuilder();
      this.appendTeamInfo(scorecard, message);
      MessageData messageData = this.parseScoreInfo(node, scorecard, matches, message);
      return messageData;
   }

   private MessageData parseScoreInfo(Node node, MatchScorecard scorecard, Map<String, Match> matches, StringBuilder message) {
      boolean isEndOfOver = false;
      String currentScore = "";
      MessageData messageData = null;
      if (node.getNodeType() == 1) {
         for(Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            String name = childNode.getNodeName();
            String content = childNode.getTextContent();
            if ("Score".equals(name)) {
               if (!StringUtils.isEmpty(content)) {
                  scorecard.setScore(content);
                  currentScore = content.trim();
               }
            } else if (!"ThisOver".equals(name)) {
               if ("Details".equals(name)) {
                  scorecard.setBallDetail(StringUtils.isEmpty(content) ? "" : content);
               } else if ("Commentary".equals(name)) {
                  if (!StringUtils.isEmpty(content)) {
                     messageData = this.createMessage(scorecard, matches, isEndOfOver, message, content, currentScore);
                  }
                  break;
               }
            } else {
               isEndOfOver = StringUtils.isEmpty(content) && this.checkEndOfOver(scorecard);
               scorecard.setEndOfOver(isEndOfOver);
            }
         }
      }

      return messageData;
   }

   private void appendTeamInfo(MatchScorecard scorecard, StringBuilder message) {
      Match match = scorecard.getMatch();
      String teamA = this.getMapping(CricketFeed.teamScoreNameMappings, match.getTeamA());
      String teamB = this.getMapping(CricketFeed.teamScoreNameMappings, match.getTeamB());
      message.append(teamA).append(" v ").append(teamB);
      if (logger.isDebugEnabled()) {
         logger.debug("With Team names: " + message);
      }

   }

   private boolean checkEndOfOver(MatchScorecard scorecard) {
      boolean isEndOfOver = false;
      if (!StringUtils.isEmpty(scorecard.getBall())) {
         int ball = Integer.parseInt(scorecard.getBall());
         isEndOfOver = ball >= 6;
      }

      return isEndOfOver;
   }

   private MessageData createMessage(MatchScorecard scorecard, Map<String, Match> matches, boolean isEndOfOver, StringBuilder message, String content, String currentScore) {
      message.append(" >> ");
      message.append(content);
      if (logger.isDebugEnabled()) {
         logger.debug("With Commentary: " + message);
      }

      if (isEndOfOver) {
         this.appendEndOfOverSummary(scorecard, message, currentScore);
         if (logger.isDebugEnabled()) {
            logger.debug("With End of over: " + message);
         }
      } else {
         message.append(" ").append(currentScore);
      }

      MessageData messageData = new MessageData();
      messageData.messageText = message.toString();
      messageData = this.appendBallDetails(scorecard, messageData);
      return messageData;
   }

   private MessageData appendBallDetails(MatchScorecard scorecard, MessageData messageData) {
      List<String> emoticonKeys = new ArrayList();
      String emoticonKey = "";
      if (scorecard.isSixer()) {
         emoticonKey = "(6s)";
      } else if (scorecard.isFour()) {
         emoticonKey = "(4s)";
      } else if (scorecard.isOut()) {
         emoticonKey = "(bowled-wicket)";
      }

      emoticonKeys.add(emoticonKey);
      if (!StringUtils.isEmpty(emoticonKey)) {
         messageData.emoticonKeys = emoticonKeys;
      }

      messageData.messageText = messageData.messageText + (!StringUtils.isEmpty(emoticonKey) ? " " + emoticonKey : "");
      return messageData;
   }

   public List<MessageData> backFillScores(Map<String, Match> matches, MatchScorecard scorecard, NodeList scoreNodeList, Node scoreNode) {
      List<MessageData> messagesToSend = new ArrayList();
      int newestNodeId = this.getId(scoreNode);
      int lastReadNodeId = scorecard.isInningsChange() ? 0 : scorecard.getLastReadNode();
      int diff = newestNodeId - lastReadNodeId - 1;
      int numNodesToRead = Math.min(scoreNodeList.getLength() - 1, Math.min(5, diff));
      this.getPreviousScores(matches, scorecard, scoreNodeList, lastReadNodeId, numNodesToRead, messagesToSend);
      if (lastReadNodeId == 0) {
         this.generateMatchSummary(scorecard, true);
         this.appendInningsSummary(scorecard, messagesToSend);
      }

      return messagesToSend;
   }

   private void getPreviousScores(Map<String, Match> matches, MatchScorecard scorecard, NodeList scoreNodeList, int lastReadNodeId, int numNodesToRead, List<MessageData> messagesToSend) {
      int minIndex = scoreNodeList.getLength() - 1 - numNodesToRead;
      int maxIndex = scoreNodeList.getLength() - 1 - 1;

      for(int index = minIndex; index <= maxIndex; ++index) {
         StringBuilder thisMessage = new StringBuilder();
         Node node = XmlUtil.getNode(scoreNodeList, index);
         if (node != null) {
            MessageData messageData = this.getScoreWithoutChecks(node, scorecard, matches, thisMessage);
            messagesToSend.add(messageData);
            logger.info(thisMessage.toString());
         }
      }

   }

   private void appendEndOfOverSummary(MatchScorecard scorecard, StringBuilder message, String currentScore) {
      this.generateMatchSummary(scorecard, false);
      message.append(" ").append(this.matchSummaryFeedHelper.getEndOfOverText(scorecard) + this.matchSummaryFeedHelper.getTeamScore(scorecard, currentScore));
      message.append(this.matchSummaryFeedHelper.getCurrentRunRateString(scorecard) + this.matchSummaryFeedHelper.getRequiredRunRateString(scorecard));
   }

   public void appendInningsSummary(MatchScorecard scorecard, List<MessageData> messagesToSend) {
      StringBuilder message = new StringBuilder();
      this.appendTeamInfo(scorecard, message);
      message.append(" >> ").append(this.matchSummaryFeedHelper.getPreviousInningsSummaries(scorecard, true, false));
      messagesToSend.add(this.createMessageData(message.toString()));
      messagesToSend.add(this.createMessageData(scorecard.getCurrentInningsSummary()));
   }

   public MessageData createMessageData(String message) {
      MessageData messageData = new MessageData();
      messageData.messageText = message;
      return messageData;
   }

   public void generateMatchSummary(MatchScorecard scorecard, boolean isDetailed) {
      this.matchSummaryFeedHelper.getMatchSummary(CricketFeed.getXmlFactory(), scorecard, CricketFeed.getLocalDir(), CricketFeed.getRemoteDir(), isDetailed);
   }

   public String createMatchResultPost(MatchScorecard scorecard) {
      StringBuilder message = new StringBuilder();
      message.append("<p><b>").append(DateTimeUtils.dateToString(scorecard.getMatch().getMatchStartTime(), "MMM dd, yyyy"));
      message.append(" ");
      this.appendTeamInfo(scorecard, message);
      message.append("<br>").append(scorecard.getMatch().getResult()).append("</b><br>");
      message.append(this.matchSummaryFeedHelper.getPreviousInningsSummaries(scorecard, false, true));
      message.append(scorecard.getCurrentInningsSummary().substring(0, scorecard.getCurrentInningsSummary().indexOf(" RR"))).append("</p>");
      return message.toString();
   }

   public void appendPreviewInfo(MatchScorecard scorecard, StringBuilder message) {
      if (scorecard.getLastReadNode() == 0) {
         message.append(scorecard.getMatch().getPreview());
         if (logger.isDebugEnabled()) {
            logger.debug("With Preview: " + message);
         }
      }

   }

   public void appendFinalResult(MatchScorecard scorecard, Map<String, Match> matches, List<MessageData> messagesToSend) {
      Match match = scorecard.getMatch();
      if (!match.isLive() && !StringUtils.isEmpty(match.getResult())) {
         messagesToSend.add(this.createMessageData(">> " + match.getResult()));
         synchronized(this) {
            Match thisMatch = (Match)matches.get(match.getMatchIdentifier());
            thisMatch.setResultReported(true);
         }

         match.setResultReported(true);
      }

   }

   public boolean updateUpcomingEvent(String team, Map<String, Match> matches) {
      boolean updateDescription = true;
      Iterator i$ = matches.values().iterator();

      while(i$.hasNext()) {
         Match match = (Match)i$.next();
         if (match.isLive() && (match.getTeamA().equals(team) || match.getTeamB().equals(team))) {
            updateDescription = false;
            break;
         }
      }

      return updateDescription;
   }

   public String[] createRoomListForUpcomingEvent(Match match, Map<String, Match> matches, int numberOfRooms) {
      boolean updateDescriptionTeamA = this.updateUpcomingEvent(match.getTeamA(), matches);
      boolean updateDescriptionTeamB = this.updateUpcomingEvent(match.getTeamB(), matches);
      if (this.isEmptyArray(match.getMatchChatRooms()) || this.isEmptyArray(match.getTeamAChatRooms()) || this.isEmptyArray(match.getTeamBChatRooms())) {
         if (logger.isInfoEnabled()) {
            logger.info("Creating new chat room list for match '" + match.getMatchIdentifier() + "'...");
         }

         this.createNewRoomList(match, (String[])null, numberOfRooms, updateDescriptionTeamA, updateDescriptionTeamB);
      }

      return this.generateRoomList(match, true, true);
   }

   private boolean isEmptyArray(String[] array) {
      return array == null || array != null && StringUtils.isEmpty(array[0]);
   }

   public String[] createRoomList(Match match, String[] generalChatRoomPrefixes, int numberOfRooms) {
      if (this.isEmptyArray(match.getMatchChatRooms()) || this.isEmptyArray(match.getTeamAChatRooms()) || this.isEmptyArray(match.getTeamBChatRooms())) {
         if (logger.isInfoEnabled()) {
            logger.info("Creating new chat room list for match '" + match.getMatchIdentifier() + "'...");
         }

         this.createNewRoomList(match, generalChatRoomPrefixes, numberOfRooms, true, true);
      }

      return this.generateRoomList(match, true, true);
   }

   public void createNewRoomList(Match match, String[] generalChatRoomPrefixes, int numberOfRooms, boolean includeTeamA, boolean includeTeamB) {
      if (generalChatRoomPrefixes != null && !StringUtils.isEmpty(generalChatRoomPrefixes[0])) {
         this.addGeneralRoomPrefixes(match, generalChatRoomPrefixes, numberOfRooms);
      }

      this.addGameRoomPrefixes(match, numberOfRooms);
      this.addTeamRoomPrefixes(match, numberOfRooms, includeTeamA, includeTeamB);
   }

   private String[] generateRoomList(Match match, boolean includeTeamA, boolean includeTeamB) {
      List<String> chatRoomList = new ArrayList();
      if (match.getGeneralChatRooms() != null) {
         Collections.addAll(chatRoomList, match.getGeneralChatRooms());
      }

      if (match.getMatchChatRooms() != null) {
         Collections.addAll(chatRoomList, match.getMatchChatRooms());
      }

      if (includeTeamA && match.getTeamAChatRooms() != null) {
         Collections.addAll(chatRoomList, match.getTeamAChatRooms());
      }

      if (includeTeamB && match.getTeamBChatRooms() != null) {
         Collections.addAll(chatRoomList, match.getTeamBChatRooms());
      }

      String[] chatRoomNames = (String[])chatRoomList.toArray(new String[0]);
      return chatRoomNames;
   }

   private void addGeneralRoomPrefixes(Match match, String[] generalChatRoomPrefixes, int numberOfRooms) {
      String[] generalChatRooms = new String[generalChatRoomPrefixes.length * numberOfRooms];
      int ctr = 0;

      for(int i = 0; i < generalChatRoomPrefixes.length; ++i) {
         for(int room = 0; room < numberOfRooms; ++room) {
            generalChatRooms[ctr++] = generalChatRoomPrefixes[i] + " " + (room + 1);
         }
      }

      match.setGeneralChatRooms(generalChatRooms);
   }

   private void addGameRoomPrefixes(Match match, int numberOfRooms) {
      if (this.isEmptyArray(match.getMatchChatRooms())) {
         String seriesName = (String)CricketFeed.seriesNameMappings.get(match.getSeriesName());
         boolean useMatchNumberForPrefix = CricketFeed.useMatchNumberForPrefix;
         String gameRoomPrefix = seriesName != null ? seriesName + " " : (useMatchNumberForPrefix ? match.getMatchNumber() + " " : "");
         String teamAPrefix = this.getMapping(CricketFeed.gamePrefixMappings, match.getTeamA());
         String teamBPrefix = this.getMapping(CricketFeed.gamePrefixMappings, match.getTeamB());
         StringBuilder teamAteamB = (new StringBuilder(teamAPrefix)).append(" v ").append(teamBPrefix);
         StringBuilder teamBteamA = (new StringBuilder(teamBPrefix)).append(" v ").append(teamAPrefix);
         String[] matchRoomPrefixes = new String[]{gameRoomPrefix + teamAteamB.toString(), gameRoomPrefix + teamBteamA.toString()};
         String[] matchChatRooms = new String[2 * numberOfRooms];
         int ctr = 0;

         for(int i = 0; i < matchRoomPrefixes.length; ++i) {
            for(int room = 0; room < numberOfRooms; ++room) {
               matchChatRooms[ctr++] = matchRoomPrefixes[i] + " " + (room + 1);
            }
         }

         match.setMatchChatRooms(matchChatRooms);
         if (logger.isInfoEnabled()) {
            logger.info("Match rooms added: " + matchChatRooms);
         }
      }

   }

   private void addTeamRoomPrefixes(Match match, int numberOfRooms, boolean includeTeamA, boolean includeTeamB) {
      String teamBPrefix;
      String[] teamChatRooms;
      if (includeTeamA && this.isEmptyArray(match.getTeamAChatRooms())) {
         teamBPrefix = this.getMapping(CricketFeed.teamMappings, match.getTeamA());
         teamChatRooms = this.addTeamChatRooms(match, numberOfRooms, teamBPrefix);
         match.setTeamAChatRooms(teamChatRooms);
      }

      if (includeTeamB && this.isEmptyArray(match.getTeamBChatRooms())) {
         teamBPrefix = this.getMapping(CricketFeed.teamMappings, match.getTeamB());
         teamChatRooms = this.addTeamChatRooms(match, numberOfRooms, teamBPrefix);
         match.setTeamBChatRooms(teamChatRooms);
      }

   }

   private String[] addTeamChatRooms(Match match, int numberOfRooms, String teamPrefix) {
      String[] teamChatRooms = new String[numberOfRooms];
      int ctr = 0;

      for(int room = 0; room < numberOfRooms; ++room) {
         teamChatRooms[ctr++] = teamPrefix + " " + (room + 1);
      }

      return teamChatRooms;
   }

   public String createCountdownMessage(Match match) {
      StringBuilder message = new StringBuilder();
      if (match.getMatchStartTime() != null) {
         String remainingTime = DateTimeUtils.getRemainingTime(match.getMatchStartTime());
         if (!StringUtils.isEmpty(remainingTime)) {
            message.append("Live match: ").append(this.getTeamString(match)).append(" starts in ").append(remainingTime);
         } else {
            message.append(this.getTeamString(match));
            if (!StringUtils.isEmpty(match.getState())) {
               message.append(": ").append(match.getState());
            } else {
               message.append("will start shortly");
            }
         }

         if (logger.isDebugEnabled()) {
            logger.debug(" Match start time is " + match.getMatchStartTime());
         }
      }

      return message.toString();
   }

   private String getTeamString(Match match) {
      StringBuilder message = new StringBuilder();
      message.append(this.getMapping(CricketFeed.teamScoreNameMappings, match.getTeamA())).append(" v ").append(this.getMapping(CricketFeed.teamScoreNameMappings, match.getTeamB())).append(" [").append(match.getMatchNumber()).append("] ");
      return message.toString();
   }

   public String createLiveMatchMessage(Match match) {
      StringBuilder message = new StringBuilder();
      String matchState = "Live match in progress: ";
      boolean hasMatchEnded = "Match Ended".equals(match.getState());
      if (!StringUtils.isEmpty(match.getState()) && !hasMatchEnded) {
         matchState = match.getState();
      } else if (hasMatchEnded && !StringUtils.isEmpty(match.getResult())) {
         matchState = match.getResult();
      }

      message.append(matchState).append(": ").append(this.getTeamString(match));
      if (logger.isInfoEnabled()) {
         logger.info(" Message :" + message);
      }

      return message.toString();
   }

   public String createMatchEndedMessage(MatchScorecard scorecard) {
      StringBuilder message = new StringBuilder();
      Match match = scorecard.getMatch();
      message.append("Result: ").append(this.getTeamString(match)).append(" - ").append(match.getResult());
      if (logger.isInfoEnabled()) {
         logger.info(" Message :" + message);
      }

      return message.toString();
   }

   public String createChatRoomDescription(MatchScorecard scorecard) {
      String chatRoomDescription = "";
      Match match = scorecard.getMatch();
      if (match.isUpcoming() && match.getMatchStartTime() != null) {
         chatRoomDescription = this.createCountdownMessage(scorecard.getMatch());
      } else if (match.isLive()) {
         chatRoomDescription = this.createLiveMatchMessage(scorecard.getMatch());
      } else if (!StringUtils.isEmpty(match.getResult())) {
         chatRoomDescription = this.createMatchEndedMessage(scorecard);
      }

      return chatRoomDescription;
   }

   private String getMapping(Map<String, String> map, String key) {
      String value = (String)map.get(key);
      return StringUtils.isEmpty(value) ? key : value;
   }
}
