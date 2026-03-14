package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "forgotpassword"
)
public class ForgotPasswordData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String username;
   @XmlElement(
      required = false,
      nillable = true
   )
   public Integer type;
   @XmlElement(
      required = false,
      nillable = true
   )
   public String emailAddress;
   @XmlElement(
      required = false,
      nillable = true
   )
   public int securityQuestion;
   @XmlElement(
      required = false,
      nillable = true
   )
   public String securityAnswer;
   @XmlElement(
      required = false,
      nillable = true
   )
   public String ipAddress;
   @XmlElement(
      required = false,
      nillable = true
   )
   public String mobileDevice;
   @XmlElement(
      required = false,
      nillable = true
   )
   public String userAgent;

   public String toString() {
      return String.format("[username:%s, type:%s, emailAddress:%s, securityQuestion:%s, securityAnswer:%s, ipAddress:%s, mobileDevice:%s, userAgent:%s]", this.username, this.type, this.emailAddress, this.securityQuestion, this.securityAnswer, this.ipAddress, this.mobileDevice, this.userAgent);
   }
}
