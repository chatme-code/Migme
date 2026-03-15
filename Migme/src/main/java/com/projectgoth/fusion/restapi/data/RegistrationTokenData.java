package com.projectgoth.fusion.restapi.data;

public class RegistrationTokenData {
   public String status;
   public String username;
   public String email;

   public RegistrationTokenData(String status, String username, String email) {
      this.status = status;
      this.username = username;
      this.email = email;
   }
}
