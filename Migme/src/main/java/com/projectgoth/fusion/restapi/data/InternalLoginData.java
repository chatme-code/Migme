package com.projectgoth.fusion.restapi.data;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "internalLoginData"
)
public class InternalLoginData {
   public int reuseExistingSession = 0;
   public String sid = "";
   public int presence = 1;
   public short clientVersion = 0;
   public String mobileDevice = "1";
   public byte deviceType = 10;
   public String view;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String remoteIpAddress;
   @XmlElement(
      required = true,
      nillable = false
   )
   public String userAgent;

   public InternalLoginData() {
      this.view = SSOEnums.View.MIGBO_AJAXV2.name();
      this.remoteIpAddress = "";
      this.userAgent = "";
   }

   public boolean reuseExistingSession() {
      return this.reuseExistingSession == 1;
   }
}
