package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "user"
)
public class UserRegistrationData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public Integer partnerId;
   @XmlElement
   public String userName;
   @XmlElement
   public String password;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String mobilePhone;
   @XmlElement
   public String emailAddress;
   @XmlElement
   public String regnIPAddress;
   @XmlElement
   public Integer userId;
}
