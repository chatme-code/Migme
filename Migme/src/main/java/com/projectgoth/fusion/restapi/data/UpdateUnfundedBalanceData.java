package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

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
