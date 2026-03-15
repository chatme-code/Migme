package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.data.EmoticonData;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "emoticons"
)
public class UserEmoticonList {
   public List<EmoticonData> data;

   public UserEmoticonList(List<EmoticonData> data) {
      this.data = data;
   }
}
