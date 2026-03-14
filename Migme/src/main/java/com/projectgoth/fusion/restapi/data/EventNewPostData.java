package com.projectgoth.fusion.restapi.data;

import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "events"
)
public class EventNewPostData {
   public String timestamp;
   public String fullPostid;
   public String parentFullPostid;
   public String originality;
   public String application;
   public String type;
   public List<String> hashtags;
   public List<Map<String, String>> links;
   public List<Integer> mentions;
}
