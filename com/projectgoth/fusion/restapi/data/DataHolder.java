package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
