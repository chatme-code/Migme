package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "reversecredit"
)
public class ReverseCreditData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String misUserName;
   @XmlElement(
      required = true,
      nillable = false
   )
   public Long accountEntryID;

   public String toString() {
      return String.format("[misUsername:%s, accountEntryID:%s]", this.misUserName, this.accountEntryID);
   }
}
