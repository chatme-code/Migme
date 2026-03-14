package com.projectgoth.fusion.data;

import java.io.Serializable;

public class UserDataAndRegistrationContextData implements Serializable {
   public UserData userData;
   public RegistrationContextData regContextData;

   public UserDataAndRegistrationContextData(UserData userData, RegistrationContextData regContextData) {
      this.userData = userData;
      this.regContextData = regContextData;
   }
}
