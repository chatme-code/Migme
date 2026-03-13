package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "create"
)
public class UpdateUnfundedBalanceData {
   public int type;
   public String reference;
   public String description;
   public double amount;
   public String currency;
   public String ipAddress;
   public String userAgent;
}
