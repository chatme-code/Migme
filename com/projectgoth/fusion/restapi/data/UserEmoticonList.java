package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.data.EmoticonData;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
