package com.projectgoth.fusion.data;

import com.projectgoth.fusion.restapi.enums.RegistrationType;
import java.io.Serializable;

public class UserRegistrationContextData implements Serializable {
   public String campaign;
   public boolean verified;
   public RegistrationType registrationType;
   public Integer invitationID;

   public UserRegistrationContextData(boolean verified) {
      this.verified = verified;
   }

   public UserRegistrationContextData(String campaign, boolean verified, RegistrationType registrationType) {
      this(campaign, verified, registrationType, (Integer)null);
   }

   public UserRegistrationContextData(String campaign, boolean verified, RegistrationType registrationType, Integer invitationID) {
      this.campaign = campaign;
      this.verified = verified;
      this.registrationType = registrationType;
      this.invitationID = invitationID;
   }

   public boolean isEmailBased() {
      return this.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH1 || this.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH2 || this.registrationType == RegistrationType.EMAIL_LEGACY || this.registrationType == RegistrationType.FACEBOOK_CONNECT;
   }
}
