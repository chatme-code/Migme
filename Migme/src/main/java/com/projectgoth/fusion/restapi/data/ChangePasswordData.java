package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "changepassword"
)
public class ChangePasswordData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String username;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String newPassword;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String ipAddress;
}
