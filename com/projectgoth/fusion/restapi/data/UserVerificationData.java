package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "user"
)
public class UserVerificationData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String username;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String emailAddress;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String registrationType;
   @XmlElement(
      required = true,
      nillable = false
   )
   public Long updatedTime;
   @XmlElement(
      required = true,
      nillable = false
   )
   public Boolean isVerified;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String campaign;

   public UserVerificationData() {
   }

   public UserVerificationData(String username, String emailAddress, String registrationType, Boolean isVerified, Long updatedTime, String campaign) {
      this.emailAddress = emailAddress;
      this.username = username;
      this.registrationType = registrationType;
      this.isVerified = isVerified;
      this.updatedTime = updatedTime;
      this.campaign = campaign;
   }
}
