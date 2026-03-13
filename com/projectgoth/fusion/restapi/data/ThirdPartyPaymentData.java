package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "thirdpartypayment"
)
public class ThirdPartyPaymentData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String reference;
   public String description;
   @XmlElement(
      required = true,
      nillable = false
   )
   public double amount;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String currency;
   public String ipAddress;
   public String sessionId;
   public String mobileDevice;
   public String userAgent;
}
