package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(
   name = "holder"
)
public class DataHolder<DataType> {
   @XmlElement(
      nillable = true,
      required = false
   )
   public DataType data;

   public DataHolder() {
   }

   public DataHolder(DataType data) {
      this.data = data;
   }
}
