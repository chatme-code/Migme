package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
