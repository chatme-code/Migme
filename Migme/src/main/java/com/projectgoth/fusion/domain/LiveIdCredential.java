package com.projectgoth.fusion.domain;

public class LiveIdCredential {
   private String username;
   private String liveId;
   private String password;

   public LiveIdCredential(String username, String liveId, String password) {
      this.username = username;
      this.liveId = liveId;
      this.password = password;
   }

   public String getUsername() {
      return this.username;
   }

   public String getLiveId() {
      return this.liveId;
   }

   public void setLiveId(String liveId) {
      this.liveId = liveId;
   }

   public String getPassword() {
      return this.password;
   }
}
