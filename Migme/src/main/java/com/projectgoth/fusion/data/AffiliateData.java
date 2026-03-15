package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class AffiliateData implements Serializable {
   public Integer id;
   public String name;
   public String password;
   public String code;
   public Double commission;
   public Date lastlogindate;
   public String referredBy;
   public String username;
   public String emailAddress;
   public String firstName;
   public String lastName;
   public String additionalInfo;
   public Integer countryIdDetected;
   public String registrationIpAddress;
   public Date dateRegistered;
   public String fromUserRegistration;
   public String registerWithoutMobile;
   public String mobilePhone;
   public String sessionId;
   public String mobileDevice;
   public String userAgent;
}
