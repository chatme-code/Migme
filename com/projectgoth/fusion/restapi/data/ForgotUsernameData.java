package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "forgotusername"
)
public class ForgotUsernameData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String emailAddress;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String ipAddress;
}
