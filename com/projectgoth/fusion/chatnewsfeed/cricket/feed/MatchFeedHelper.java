package com.projectgoth.fusion.chatnewsfeed.cricket.feed;

import com.projectgoth.fusion.chatnewsfeed.CricketFeed;
import com.projectgoth.fusion.chatnewsfeed.cricket.data.Match;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchFeedHelper {
   private static final Logger logger = Logger.getLogger(ConfigUtils.getLoggerName(CricketFeed.class));

   public void processMatches(Node node, Document xmlDocument, Map<String, Match> matches, List<String> seriesNames) {
      NodeList nodeList = xmlDocument.getElementsByTagName("match");
      if (nodeList != null) {
         Map<String, Match> updatedMatches = new HashMap();

         for(Node matchNode = nodeList.item(0); matchNode != null; matchNode = matchNode.getNextSibling()) {
            this.processMatchNode(matchNode, updatedMatches, CricketFeed.filterBySeries, seriesNames);
         }

         Iterator i$ = updatedMatches.values().iterator();

         while(true) {
            Match updatedMatch;
            boolean isJustEnded;
            do {
               if (!i$.hasNext()) {
                  return;
               }

               updatedMatch = (Match)i$.next();
               Match existingMatch = (Match)matches.get(updatedMatch.getMatchIdentifier());
               isJustEnded = existingMatch != null && !updatedMatch.isLive() && !existingMatch.isResultReported();
            } while(!updatedMatch.isLive() && !isJustEnded && !updatedMatch.isUpcoming());

            synchronized(matches) {
               matches.put(updatedMatch.getMatchIdentifier(), updatedMatch);
            }
         }
      }
   }

   private void processMatchNode(Node node, Map<String, Match> updatedMatches, boolean checkSeries, List<String> seriesNames) {
      if (node.getNodeType() == 1) {
         Match match = new Match();
         NamedNodeMap attributesMap = node.getAttributes();
         this.getDeterminantMatchAttributes(match, attributesMap);
         boolean isMatchInSeries = !checkSeries || checkSeries && seriesNames.contains(match.getSeriesName());
         if (match.getMatchIdentifier() != null && isMatchInSeries) {
            String matchId = match.getMatchIdentifier();
            this.getOtherAttributes(match, attributesMap);
            synchronized(this) {
               updatedMatches.put(matchId, match);
            }
         }
      }

   }

   private void getDeterminantMatchAttributes(Match match, NamedNodeMap attributes) {
      Node id = attributes.getNamedItem("matchfile");
      if (id != null) {
         match.setMatchIdentifier(id.getTextContent());
      }

      Node live = attributes.getNamedItem("live");
      if (live != null) {
         match.setLive(Integer.parseInt(live.getTextContent()) != 0);
      }

      Node seriesName = attributes.getNamedItem("seriesname");
      if (seriesName != null) {
         match.setSeriesName(seriesName.getTextContent());
      }

   }

   private void getOtherAttributes(Match match, NamedNodeMap attributes) {
      Node upcoming = attributes.getNamedItem("upcoming");
      if (upcoming != null) {
         match.setUpcoming(Integer.parseInt(upcoming.getTextContent()) != 0);
      }

      Node state = attributes.getNamedItem("delayed");
      if (state != null) {
         match.setState(state.getTextContent());
      }

      Node teamA = attributes.getNamedItem("teama");
      if (teamA != null) {
         match.setTeamA(teamA.getTextContent());
      }

      Node teamB = attributes.getNamedItem("teamb");
      if (teamB != null) {
         match.setTeamB(teamB.getTextContent());
      }

      Node result = attributes.getNamedItem("matchresult");
      if (result != null) {
         match.setResult(result.getTextContent());
      }

      Node venue = attributes.getNamedItem("venue");
      if (venue != null) {
         match.setVenue(venue.getTextContent());
      }

      Node matchNumber = attributes.getNamedItem("matchnumber");
      if (matchNumber != null) {
         match.setMatchNumber(matchNumber.getTextContent());
      }

      Node matchDate = attributes.getNamedItem("matchdate");
      if (matchDate != null) {
         match.setMatchDate(matchDate.getTextContent());
      }

      Node matchTime = attributes.getNamedItem("matchtime");
      if (matchTime != null) {
         match.setMatchTime(matchTime.getTextContent().substring(0, 5));
         match.setCalendarTimeZone(matchTime.getTextContent().substring(7, 10));
         if (match.getMatchStartTime() == null) {
            match.setMatchStartTime(this.getStartTime(match));
         }
      }

      Node matchMonth = attributes.getNamedItem("matchmonth");
      if (matchMonth != null) {
         match.setMatchMonth(matchMonth.getTextContent());
      }

      Node matchYear = attributes.getNamedItem("matchyear");
      if (matchYear != null) {
         match.setMatchYear(matchYear.getTextContent());
      }

   }

   public Date getStartTime(Match match) {
      Date date = match.getMatchStartTime();
      if (date == null && !StringUtils.isEmpty(match.getMatchDate()) && !StringUtils.isEmpty(match.getMatchTime())) {
         String dateString = match.getMatchDate() + " " + match.getMatchTime();

         try {
            date = DateTimeUtils.stringToDate(dateString, "MM/dd/yyyy HH:mm", match.getCalendarTimeZone());
            match.setMatchStartTime(date);
         } catch (Exception var5) {
            logger.error("Invalid start time:" + dateString + " for match ID: " + match.getMatchIdentifier());
            var5.printStackTrace();
         }
      }

      return date;
   }
}
