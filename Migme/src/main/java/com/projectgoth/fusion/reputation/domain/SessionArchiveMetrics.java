package com.projectgoth.fusion.reputation.domain;

public class SessionArchiveMetrics implements Metrics {
   public static final int USERNAME_INDEX = 0;
   public static final int CHATROOM_MESSAGES_SENT_INDEX = 1;
   public static final int PRIVATE_MESSAGES_SENT_INDEX = 2;
   public static final int TOTAL_TIME_INDEX = 3;
   public static final int PHOTOS_UPLOADED_INDEX = 4;
   private String username;
   private int chatRoomMessagesSent = 0;
   private int privateMessagesSent = 0;
   private long totalTime = 0L;
   private int photosUploaded = 0;

   public void reset(String username) {
      this.username = username;
      this.chatRoomMessagesSent = 0;
      this.privateMessagesSent = 0;
      this.totalTime = 0L;
      this.photosUploaded = 0;
   }

   public void addChatRoomMessagesSent(int messages) {
      this.chatRoomMessagesSent += messages;
   }

   public void addPrivateMessagesSent(int messages) {
      this.privateMessagesSent += messages;
   }

   public void addTotalTime(long seconds) {
      this.totalTime += seconds;
   }

   public void addPhotosUploaded(int photos) {
      this.photosUploaded += photos;
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

   public long getTotalTime() {
      return this.totalTime;
   }

   public int getPhotosUploaded() {
      return this.photosUploaded;
   }

   public boolean hasMetrics() {
      return this.totalTime != 0L || this.chatRoomMessagesSent != 0 || this.photosUploaded != 0 || this.privateMessagesSent != 0;
   }

   public String toLine() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.username).append(',').append(this.chatRoomMessagesSent).append(',').append(this.privateMessagesSent).append(',').append(this.totalTime).append(',').append(this.photosUploaded);
      return builder.toString();
   }
}
