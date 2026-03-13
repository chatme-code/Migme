package com.projectgoth.fusion.payment.mimopay;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
   name = "RESPONSE"
)
public class PendingStatusInquiryRequest {
   @XmlElement(
      name = "SERVICENAME"
   )
   public String serviceName;
   @XmlElement(
      name = "MERCHANTCODE"
   )
   public String merchantCode;
   @XmlElement(
      name = "GAMECODE"
   )
   public String gameCode;
   @XmlElement(
      name = "USERID"
   )
   public String userID;
   @XmlElement(
      name = "TRANSID"
   )
   public String ourTransactionId;
   @XmlElement(
      name = "RKEY"
   )
   public String reloadCardOrTokenKey;
   @XmlElement(
      name = "PTYPE"
   )
   public String pType;
   @XmlElement(
      name = "TIMESTAMP"
   )
   public long timestamp;
   @XmlElement(
      name = "HASHKEY"
   )
   public String hashKey;
}
