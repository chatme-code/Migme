package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "user"
)
public class SSOFullLoginResponseData {
   public String alert;
   public Integer alertContentType;
   public String msnDetail;
   public String yahooDetail;
   public String mailUrl;
   public Integer mailCount;
   public String gtalkDetail;
   public String facebookDetail;
   public String reputationLevel;
   public String reputationImagePath;
   public Integer presence;

   public String toString() {
      return this.alert + ", " + this.alertContentType + ", " + this.msnDetail + ", " + this.yahooDetail + ", " + this.mailUrl + ", " + this.mailCount + ", " + this.gtalkDetail + ", " + this.facebookDetail + ", " + this.reputationLevel + ", " + this.reputationImagePath + ", " + this.presence;
   }
}
