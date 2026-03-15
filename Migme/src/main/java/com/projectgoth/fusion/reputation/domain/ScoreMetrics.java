package com.projectgoth.fusion.reputation.domain;

public class ScoreMetrics implements Metrics {
   private String username;
   private int chatRoomMessagesSent = 0;
   private int privateMessagesSent = 0;
   private int totalTime = 0;
   private int photosUploaded = 0;
   private int kicks = 0;
   private int authenticatedReferrals = 0;
   private int rechargedAmount = 0;
   private int virtualGiftsReceived = 0;
   private int virtualGiftsSent = 0;
   private int callDuration = 0;
   private int totalScore = 0;

   public void reset(String username) {
      this.username = username;
      this.chatRoomMessagesSent = 0;
      this.privateMessagesSent = 0;
      this.totalTime = 0;
      this.photosUploaded = 0;
      this.kicks = 0;
      this.authenticatedReferrals = 0;
      this.rechargedAmount = 0;
      this.virtualGiftsReceived = 0;
      this.virtualGiftsSent = 0;
      this.callDuration = 0;
      this.totalScore = 0;
   }

   public boolean hasMetrics() {
      return this.totalTime != 0 || this.chatRoomMessagesSent != 0 || this.photosUploaded != 0 || this.privateMessagesSent != 0 || this.kicks != 0 || this.authenticatedReferrals != 0 || this.virtualGiftsReceived != 0 || this.virtualGiftsSent != 0 || this.callDuration != 0 || this.totalScore != 0;
   }

   public String toLine() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.username).append(',').append(this.chatRoomMessagesSent).append(',').append(this.privateMessagesSent).append(',').append(this.totalTime).append(',').append(this.photosUploaded).append(',').append(this.kicks).append(',').append(this.authenticatedReferrals).append(',').append(this.rechargedAmount).append(',').append(this.virtualGiftsReceived).append(',').append(this.virtualGiftsSent).append(',').append(this.callDuration).append(',').append(this.totalScore);
      return builder.toString();
   }

   public void addChatRoomMessagesSent(int messages) {
      this.chatRoomMessagesSent += messages;
   }

   public void addPrivateMessagesSent(int messages) {
      this.privateMessagesSent += messages;
   }

   public void addTotalTime(int seconds) {
      this.totalTime += seconds;
   }

   public void addPhotosUploaded(int photos) {
      this.photosUploaded += photos;
   }

   public void addKicks(int kicks) {
      this.kicks += kicks;
   }

   public void addAuthenticatedReferrals(int authenticatedReferrals) {
      this.authenticatedReferrals += authenticatedReferrals;
   }

   public void addRechargedAmount(int rechargedAmount) {
      this.rechargedAmount += rechargedAmount;
   }

   public void addVirtualGiftsReceived(int virtualGiftsReceived) {
      this.virtualGiftsReceived += virtualGiftsReceived;
   }

   public void addVirtualGiftsSent(int virtualGiftsSent) {
      this.virtualGiftsSent += virtualGiftsSent;
   }

   public void addCallDuration(int callDuration) {
      this.callDuration += callDuration;
   }

   public void addTotalScore(int totalScore) {
      this.totalScore += totalScore;
   }

   public String getUsername() {
      return this.username;
   }

   public int getChatRoomMessagesSent() {
      return this.chatRoomMessagesSent;
   }

   public int getPrivateMessagesSent() {
      return this.privateMessagesSent;
   }

   public int getTotalTime() {
      return this.totalTime;
   }

   public int getPhotosUploaded() {
      return this.photosUploaded;
   }

   public int getKicks() {
      return this.kicks;
   }

   public int getAuthenticatedReferrals() {
      return this.authenticatedReferrals;
   }

   public int getRechargedAmount() {
      return this.rechargedAmount;
   }

   public int getVirtualGiftsReceived() {
      return this.virtualGiftsReceived;
   }

   public int getVirtualGiftsSent() {
      return this.virtualGiftsSent;
   }

   public int getCallDuration() {
      return this.callDuration;
   }

   public int getTotalScore() {
      return this.totalScore;
   }
}
