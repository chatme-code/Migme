package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
