package com.projectgoth.fusion.domain;

public class UsernameAndMobileNumber {
   private String username;
   private String mobileNumber;

   public UsernameAndMobileNumber(String username, String mobileNumber) {
      this.username = username;
      this.mobileNumber = mobileNumber;
   }

   public String getUsername() {
      return this.username;
   }

   public String getMobileNumber() {
      return this.mobileNumber;
   }
}
