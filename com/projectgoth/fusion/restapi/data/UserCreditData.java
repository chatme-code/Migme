package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
