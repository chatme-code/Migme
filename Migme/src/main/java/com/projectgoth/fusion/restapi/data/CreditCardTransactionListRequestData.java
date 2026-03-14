package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "accountTransaction"
)
public class CreditCardTransactionListRequestData {
   @XmlElement
   public String startDate = "";
   @XmlElement
   public String endDate = "";
   @XmlElement
   public String sortBy = "";
   @XmlElement
   public String sortOrder = "";
   @XmlElement
   public String showAuth = "";
   @XmlElement
   public String showPend = "";
   @XmlElement
   public String showRej = "";
   @XmlElement
   public String username = "";
   @XmlElement
   public int displayLimit = 0;
}
