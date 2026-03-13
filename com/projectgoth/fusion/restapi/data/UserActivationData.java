package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.restapi.enums.RegistrationType;
import java.io.Serializable;

public class UserActivationData implements Serializable {
   public String username;
   public String emailAddress;
   public String token;
   public RegistrationType registrationType;
}
