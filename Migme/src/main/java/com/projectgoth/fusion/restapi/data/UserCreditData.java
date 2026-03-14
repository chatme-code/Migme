package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "credit"
)
public class UserCreditData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public Integer partnerId;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String mobilePhone;
   @XmlElement(
      required = true,
      nillable = false
   )
   public Double amount;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String transactionId;
   @XmlElement
   public Double balance;
   @XmlElement
   public Long accountEntryId;
}
