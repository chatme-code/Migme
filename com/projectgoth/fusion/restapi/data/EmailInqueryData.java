package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
