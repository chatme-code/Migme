package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "settings"
)
public class UserSettingRestData {
   @XmlElement(
      required = true,
      nillable = false
   )
   public int type;
   @XmlElement(
      required = true,
      nillable = false
   )
   public int value;

   public String toString() {
      return String.format("[type:%s, value:%s]", this.type, this.value);
   }
}
