package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "forgotpassword"
)
public class DisconnectUserSessionData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String username;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String reason;
}
