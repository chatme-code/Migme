package com.projectgoth.fusion.chatnewsfeed.cricket.data;

import java.util.Date;

public class Match implements Cloneable {
   String matchIdentifier;
   String type;
   String state;
   boolean isLive;
   boolean isUpcoming;
   String preview;
   String result;
   String teamA;
   String teamB;
   String seriesName;
   String venue;
   String matchNumber;
   String matchDate;
   String matchMonth;
   String matchYear;
   String matchTime;
   String calendarTimeZone;
   Date matchStartTime;
   Date lastUpdated;
   boolean resultReported = false;
   String[] generalChatRooms;
   String[] teamAChatRooms;
   String[] teamBChatRooms;
   String[] matchChatRooms;

   public String getMatchIdentifier() {
      return this.matchIdentifier;
   }

   public void setMatchIdentifier(String matchIdentifier) {
      this.matchIdentifier = matchIdentifier;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getState() {
      return this.state;
   }

   public void setState(String state) {
      this.state = state;
   }

   public boolean isLive() {
      return this.isLive;
   }

   public void setLive(boolean isLive) {
      this.isLive = isLive;
   }

   public boolean isUpcoming() {
      return this.isUpcoming;
   }

   public void setUpcoming(boolean isUpcoming) {
      this.isUpcoming = isUpcoming;
   }

   public String getPreview() {
      return this.preview != null ? this.preview : "";
   }

   public void setPreview(String preview) {
      this.preview = preview;
   }

   public String getResult() {
      return this.result;
   }

   public void setResult(String result) {
      this.result = result;
   }

   public String getTeamA() {
      return this.teamA;
   }

   public void setTeamA(String teamA) {
      this.teamA = teamA;
   }

   public String getTeamB() {
      return this.teamB;
   }

   public void setTeamB(String teamB) {
      this.teamB = teamB;
   }

   public String getSeriesName() {
      return this.seriesName;
   }

   public void setSeriesName(String seriesName) {
      this.seriesName = seriesName;
   }

   public String getVenue() {
      return this.venue;
   }

   public void setVenue(String venue) {
      this.venue = venue;
   }

   public String getMatchNumber() {
      return this.matchNumber;
   }

   public void setMatchNumber(String matchNumber) {
      this.matchNumber = matchNumber;
   }

   public String getMatchDate() {
      return this.matchDate;
   }

   public void setMatchDate(String matchDate) {
      this.matchDate = matchDate;
   }

   public String getMatchMonth() {
      return this.matchMonth;
   }

   public void setMatchMonth(String matchMonth) {
      this.matchMonth = matchMonth;
   }

   public String getMatchYear() {
      return this.matchYear;
   }

   public void setMatchYear(String matchYear) {
      this.matchYear = matchYear;
   }

   public String getMatchTime() {
      return this.matchTime;
   }

   public void setMatchTime(String matchTime) {
      this.matchTime = matchTime;
   }

   public String getCalendarTimeZone() {
      return this.calendarTimeZone;
   }

   public void setCalendarTimeZone(String calendarTimeZone) {
      this.calendarTimeZone = calendarTimeZone;
   }

   public Date getMatchStartTime() {
      return this.matchStartTime;
   }

   public void setMatchStartTime(Date matchStartTime) {
      this.matchStartTime = matchStartTime;
   }

   public boolean isResultReported() {
      return this.resultReported;
   }

   public void setResultReported(boolean resultReported) {
      this.resultReported = resultReported;
   }

   public Date getLastUpdated() {
      return this.lastUpdated;
   }

   public void setLastUpdated(Date lastUpdated) {
      this.lastUpdated = lastUpdated;
   }

   public String[] getGeneralChatRooms() {
      return this.generalChatRooms;
   }

   public void setGeneralChatRooms(String[] generalChatRooms) {
      this.generalChatRooms = generalChatRooms;
   }

   public String[] getTeamAChatRooms() {
      return this.teamAChatRooms;
   }

   public void setTeamAChatRooms(String[] teamChatRooms) {
      this.teamAChatRooms = teamChatRooms;
   }

   public String[] getTeamBChatRooms() {
      return this.teamBChatRooms;
   }

   public void setTeamBChatRooms(String[] teamChatRooms) {
      this.teamBChatRooms = teamChatRooms;
   }

   public String[] getMatchChatRooms() {
      return this.matchChatRooms;
   }

   public void setMatchChatRooms(String[] matchChatRooms) {
      this.matchChatRooms = matchChatRooms;
   }

   protected Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   public String toString() {
      StringBuilder string = new StringBuilder(super.toString());
      string.append(" matchIdentifier [").append(this.matchIdentifier).append("]");
      string.append(", teamA [").append(this.teamA).append("]");
      string.append(", teamB [").append(this.teamB).append("]");
      string.append(", isLive [").append(this.isLive).append("]");
      string.append(", isUpcoming [").append(this.isUpcoming).append("]");
      string.append(", state [").append(this.state).append("]");
      string.append(", seriesName [").append(this.seriesName).append("]");
      string.append(", venue [").append(this.venue).append("]");
      string.append(", result [").append(this.result).append("]");
      string.append(", isResultReported [").append(this.resultReported).append("]");
      string.append(", type [").append(this.type).append("]");
      string.append(", matchNumber [").append(this.matchNumber).append("]");
      string.append(", matchDate [").append(this.matchDate).append("]");
      string.append(", matchMonth [").append(this.matchMonth).append("]");
      string.append(", matchYear [").append(this.matchYear).append("]");
      string.append(", lastUpdated [").append(this.lastUpdated).append("]");
      return string.toString();
   }
}
