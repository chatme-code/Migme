package com.projectgoth.fusion.invitation.restapi.data;

import com.projectgoth.fusion.common.DateTimeUtils;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "create"
)
public class InvitationDetailsData implements Serializable {
   public int invitationID;
   public int status;
   private Date createdTS;
   private Date expiredTS;
   public int activityType;
   public int channelType;
   public int inviterUserID;
   public String destination;
   public String invitationToken;
   public HashMap<Integer, String> extraParameters = new HashMap();

   public void assignCreatedTime(Date ts) {
      this.createdTS = ts;
   }

   public Date fetchCreatedTime() {
      return this.createdTS;
   }

   public String getCreatedTime() {
      return DateTimeUtils.getStringForInvitationTime(this.createdTS);
   }

   public void setCreatedTime(String str) throws ParseException {
      if (str == null) {
         this.createdTS = null;
      } else {
         this.createdTS = DateTimeUtils.getInvitationTimeFromString(str);
      }

   }

   public void assignExpiredTime(Date ts) {
      this.expiredTS = ts;
   }

   public Date fetchExpiredTime() {
      return this.expiredTS;
   }

   public String getExpiredTime() {
      return DateTimeUtils.getStringForInvitationTime(this.expiredTS);
   }

   public void setExpiredTime(String str) throws ParseException {
      if (str == null) {
         this.expiredTS = null;
      } else {
         this.expiredTS = DateTimeUtils.getInvitationTimeFromString(str);
      }

   }
}
