package com.projectgoth.fusion.chatnewsfeed.cricket.feed;

import com.projectgoth.fusion.chatnewsfeed.CricketFeed;
import com.projectgoth.fusion.chatnewsfeed.cricket.data.MatchScorecard;
import com.projectgoth.fusion.chatnewsfeed.util.FileUtil;
import com.projectgoth.fusion.chatnewsfeed.util.XmlUtil;
import com.projectgoth.fusion.common.ConfigUtils;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchSummaryFeedHelper {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(MatchSummaryFeedHelper.class));

   public void getMatchSummary(DocumentBuilderFactory xmlFactory, MatchScorecard scorecard, String localDir, String remoteDir, boolean isDetailed) {
      try {
         File file = new File(localDir + File.separatorChar + scorecard.getMatch().getMatchIdentifier() + ".xml");
         FileUtil.downloadFile(remoteDir + scorecard.getMatch().getMatchIdentifier() + ".xml", file);
         if (file.exists()) {
            Document xmlDocument = FileUtil.getDocument(xmlFactory, file);
            NodeList nodeList = xmlDocument.getElementsByTagName("Matchdetail");
            if (nodeList != null) {
               Node node = nodeList.item(0);
               if (node != null) {
                  this.parseMatchDetail(xmlDocument, scorecard, node, isDetailed);
               }
            }
         }
      } catch (Exception var10) {
         logger.error("Error reading innings summary feed." + var10.getMessage());
      }

   }

   private void parseMatchDetail(Document xmlDocument, MatchScorecard scorecard, Node node, boolean isDetailed) {
      if (node != null) {
         this.setRunsInOver(xmlDocument, scorecard);
         String currentInnings = scorecard.getInnings();
         String currentInningsTag = currentInnings + "Innings";
         Node currentInningsNode = XmlUtil.getLastNode(xmlDocument, currentInningsTag);
         if (currentInningsNode != null && currentInningsNode.getNodeType() == 1) {
            this.getCurrentInningsAttributes(scorecard, currentInningsNode);
            this.getCurrentEquation(xmlDocument, scorecard);
         }

         if (isDetailed) {
            this.backfillInningsSummaries(xmlDocument, scorecard);
            scorecard.setCurrentInningsSummary(this.createCurrentInningsSummary(scorecard));
         }
      }

   }

   private String createCurrentInningsSummary(MatchScorecard scorecard) {
      StringBuilder inningsSummary = new StringBuilder();
      inningsSummary.append(this.getMapping(CricketFeed.teamScoreNameMappings, scorecard.getBattingTeam())).append(" ").append(scorecard.getScore()).append(" (").append(scorecard.getCompletedOvers()).append(" ov)");
      if (scorecard.getMatch().isLive()) {
         inningsSummary.append(" ").append(this.getRunsNeeded(scorecard));
      }

      inningsSummary.append(" ").append(this.getCurrentRunRateString(scorecard));
      if (scorecard.getMatch().isLive()) {
         inningsSummary.append(" ").append(this.getRequiredRunRateString(scorecard));
      }

      return inningsSummary.toString();
   }

   public String getEndOfOverText(MatchScorecard scorecard) {
      StringBuilder endOfOverText = new StringBuilder();
      if (scorecard.isEndOfOver()) {
         int lastOver = StringUtils.isEmpty(scorecard.getOver()) ? 0 : Integer.parseInt(scorecard.getOver().trim());
         String runString = scorecard.getRunsInOver() == 1 ? " run" : " runs";
         endOfOverText.append("End of over ").append(lastOver).append(" (").append(scorecard.getRunsInOver()).append(runString).append(") - ");
      }

      return endOfOverText.toString();
   }

   public String getTeamScore(MatchScorecard scorecard, String currentScore) {
      StringBuilder teamScore = new StringBuilder();
      return teamScore.append(this.getMapping(CricketFeed.teamScoreNameMappings, scorecard.getBattingTeam())).append(" ").append(currentScore).append(" ").append(this.getRunsNeeded(scorecard)).toString();
   }

   public String getRunsNeeded(MatchScorecard scorecard) {
      String runString = scorecard.getRunsNeeded() == 1 ? " run" : " runs";
      return scorecard.getRunsNeeded() > 0 ? " (" + scorecard.getRunsNeeded() + " " + runString + " required)" : "";
   }

   public String getCurrentRunRateString(MatchScorecard scorecard) {
      StringBuilder runRate = new StringBuilder();
      runRate.append(" ");
      if (scorecard.getMatch().isLive()) {
         runRate.append("Current ");
      }

      runRate.append("RR: ").append(this.formatDecimalNumber(scorecard.getRunRate()));
      return runRate.toString();
   }

   public String getRequiredRunRateString(MatchScorecard scorecard) {
      StringBuilder requiredRunRate = new StringBuilder();
      if ("Second".equals(scorecard.getInnings()) || "Fourth".equals(scorecard.getInnings())) {
         requiredRunRate.append(" ").append("Required RR: ").append(this.formatDecimalNumber(scorecard.getRequiredRunRate()));
      }

      return requiredRunRate.toString();
   }

   private void getCurrentInningsAttributes(MatchScorecard scorecard, Node node) {
      String team = this.getBattingTeam(node);
      scorecard.setBattingTeam(team);
      int overs = this.getAllottedOvers(node);
      scorecard.setAllottedOvers(overs);
      scorecard.setBowlingTeam(XmlUtil.getStringAttribute(node, "Bowlingteam"));
      scorecard.setTargetScore(XmlUtil.getIntegerAttribute(node, "Target"));
   }

   private void backfillInningsSummaries(Document xmlDocument, MatchScorecard scorecard) {
      String innings = scorecard.getInnings();
      int inningsNumber = (Integer)CricketFeedConstants.inningsNumberMappings.get(innings);

      for(int previousInnings = inningsNumber - 1; previousInnings > 0; --previousInnings) {
         String inningsTag = (String)CricketFeedConstants.inningsReverseNameMappings.get(previousInnings) + "Innings";
         String equationTag = (String)CricketFeedConstants.inningsReversePrefixMappings.get(previousInnings) + "Equation";
         switch(previousInnings) {
         case 1:
            if (StringUtils.isEmpty(scorecard.getFIScoreString())) {
               scorecard.setFIScoreString(this.backFillInningsSummary(xmlDocument, inningsTag, equationTag, scorecard));
            }
            break;
         case 2:
            if (StringUtils.isEmpty(scorecard.getSIScoreString())) {
               scorecard.setSIScoreString(this.backFillInningsSummary(xmlDocument, inningsTag, equationTag, scorecard));
            }
            break;
         case 3:
            if (StringUtils.isEmpty(scorecard.getTIScoreString())) {
               scorecard.setTIScoreString(this.backFillInningsSummary(xmlDocument, inningsTag, equationTag, scorecard));
            }
         }
      }

   }

   private String backFillInningsSummary(Document xmlDocument, String inningsTag, String equationTag, MatchScorecard scorecard) {
      StringBuilder inningsSummary = new StringBuilder();
      Node inningsNode = XmlUtil.getLastNode(xmlDocument, inningsTag);
      if (inningsNode != null && inningsNode.getNodeType() == 1) {
         String battingTeam = this.getBattingTeam(inningsNode);
         if (!StringUtils.isEmpty(battingTeam)) {
            inningsSummary.append(this.getMapping(CricketFeed.teamScoreNameMappings, battingTeam)).append(" ");
         }
      }

      Node equationNode = XmlUtil.getLastNode(xmlDocument, equationTag);
      if (equationNode != null && equationNode.getNodeType() == 1) {
         float oversPlayed = this.getOversPlayed(equationNode);
         int total = this.getTotal(equationNode);
         int wickets = this.getWickets(equationNode);
         float runRate = this.getRunRate(equationNode);
         String score = total + "/" + wickets;
         inningsSummary.append(score).append(" (").append(oversPlayed).append(" ov) RR ").append(runRate);
      }

      return inningsSummary.toString();
   }

   private void getCurrentEquation(Document xmlDocument, MatchScorecard scorecard) {
      String tag = (String)CricketFeedConstants.inningsMappings.get(scorecard.getInnings()) + "Equation";
      Node equationNode = XmlUtil.getLastNode(xmlDocument, tag);
      if (equationNode != null && equationNode.getNodeType() == 1) {
         float overs = this.getOversPlayed(equationNode);
         int total = this.getTotal(equationNode);
         int wickets = this.getWickets(equationNode);
         float runRate = this.getRunRate(equationNode);
         float requiredRunRate = 0.0F;
         scorecard.setTotal(total);
         scorecard.setWickets(wickets);
         scorecard.setCompletedOvers(overs);
         scorecard.setRunRate((float)total / overs);
         scorecard.setScore(total + "/" + wickets);
         if ("Second".equals(scorecard.getInnings()) || "Fourth".equals(scorecard.getInnings())) {
            scorecard.setRunsNeeded(scorecard.getTargetScore() - total);
            requiredRunRate = (float)scorecard.getRunsNeeded() / ((float)scorecard.getAllottedOvers() - overs);
            scorecard.setRequiredRunRate(requiredRunRate);
         }

         if (logger.isDebugEnabled()) {
            String equation = scorecard.getBattingTeam().trim() + " " + total + "/" + wickets + " Current RR " + runRate + " Required RR " + requiredRunRate;
            logger.debug("Current Equation: " + equation);
         }
      }

   }

   private void setRunsInOver(Document xmlDocument, MatchScorecard scorecard) {
      int runsInOver = 0;
      if (!StringUtils.isEmpty(scorecard.getOver())) {
         int over = Integer.parseInt(scorecard.getOver());
         String tag = (String)CricketFeedConstants.inningsMappings.get(scorecard.getInnings()) + "OverDetail";
         Node overDetailNode = XmlUtil.getLastNode(xmlDocument, tag);
         if (overDetailNode != null && overDetailNode.getNodeType() == 1) {
            runsInOver = XmlUtil.getIntegerAttribute(overDetailNode, "Over" + over);
         }
      }

      scorecard.setRunsInOver(runsInOver);
   }

   public String getPreviousInningsSummaries(MatchScorecard scorecard, boolean includeRunRate, boolean htmlAllowed) {
      StringBuilder summary = new StringBuilder();
      String lineDelimiter = htmlAllowed ? "<br>" : "; ";
      String text;
      if (!StringUtils.isEmpty(scorecard.getFIScoreString())) {
         text = includeRunRate ? scorecard.getFIScoreString() : scorecard.getFIScoreString().substring(0, scorecard.getFIScoreString().indexOf(" RR"));
         summary.append(text).append(lineDelimiter);
      }

      if (!StringUtils.isEmpty(scorecard.getSIScoreString())) {
         text = includeRunRate ? scorecard.getSIScoreString() : scorecard.getFIScoreString().substring(0, scorecard.getSIScoreString().indexOf(" RR"));
         summary.append(text).append(lineDelimiter);
      }

      if (!StringUtils.isEmpty(scorecard.getTIScoreString())) {
         text = includeRunRate ? scorecard.getTIScoreString() : scorecard.getFIScoreString().substring(0, scorecard.getTIScoreString().indexOf(" RR"));
         summary.append(text).append(lineDelimiter);
      }

      return summary.toString();
   }

   private String getBattingTeam(Node node) {
      String team = XmlUtil.getStringAttribute(node, "Battingteam");
      return team;
   }

   private int getAllottedOvers(Node node) {
      int overs = XmlUtil.getIntegerAttribute(node, "AllottedOvers");
      return overs;
   }

   private float getRunRate(Node node) {
      float runRate = XmlUtil.getFloatAttribute(node, "Runrate");
      return runRate;
   }

   private int getWickets(Node node) {
      int wickets = XmlUtil.getIntegerAttribute(node, "Wickets");
      return wickets;
   }

   private int getTotal(Node node) {
      int total = XmlUtil.getIntegerAttribute(node, "Total");
      return total;
   }

   private float getOversPlayed(Node node) {
      float overs = XmlUtil.getFloatAttribute(node, "Overs");
      return overs;
   }

   private String getMapping(Map<String, String> map, String key) {
      String value = (String)map.get(key);
      return StringUtils.isEmpty(value) ? key : value;
   }

   public String getListAsString(List<String> list) {
      StringBuilder listAsString = new StringBuilder();
      if (list != null && !list.isEmpty()) {
         for(int i = 0; i < list.size(); ++i) {
            listAsString.append((String)list.get(i));
            if (i != list.size() - 1) {
               listAsString.append(", ");
            }
         }
      }

      return listAsString.toString();
   }

   private String formatDecimalNumber(float number) {
      NumberFormat formatter = new DecimalFormat("0.0");
      formatter.setMinimumFractionDigits(1);
      formatter.setMaximumFractionDigits(1);
      return formatter.format((double)number);
   }
}
