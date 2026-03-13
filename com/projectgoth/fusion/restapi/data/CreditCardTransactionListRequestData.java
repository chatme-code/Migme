package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
