package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "emailinquery"
)
public class EmailInqueryData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public String subject;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String content;
}
