package com.projectgoth.fusion.data;

import com.projectgoth.leto.common.event.CampaignParticipation;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CampaignParticipantData implements CampaignParticipation, Serializable {
   private long id;
   private Date dateCreated;
   private int userId;
   private String emailAddress;
   private String mobilePhone;
   private String reference;
   private int campaignId;
   private String userAgent;
   private Map<String, String> parameters = new HashMap();

   public CampaignParticipantData() {
   }

   public CampaignParticipantData(CampaignParticipantData that) {
      this.id = that.id;
      this.dateCreated = that.dateCreated;
      this.userId = that.userId;
      this.emailAddress = that.emailAddress;
      this.mobilePhone = that.mobilePhone;
      this.reference = that.reference;
      this.campaignId = that.campaignId;
      this.userAgent = that.userAgent;
      if (that.parameters != null) {
         this.parameters.putAll(that.parameters);
      }

   }

   public CampaignParticipantData(ResultSet rs) throws SQLException {
      this.id = rs.getLong("id");
      this.dateCreated = rs.getTimestamp("datecreated");
      this.userId = rs.getInt("userid");
      this.emailAddress = rs.getString("emailaddress");
      this.mobilePhone = rs.getString("mobilephone");
      this.reference = rs.getString("reference");
      this.campaignId = rs.getInt("campaignid");
      this.userAgent = rs.getString("useragent");
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getReference() {
      return this.reference;
   }

   public void setReference(String reference) {
      this.reference = reference;
   }

   public int getUserId() {
      return this.userId;
   }

   public void setUserId(int userId) {
      this.userId = userId;
   }

   public String getEmailAddress() {
      return this.emailAddress;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
   }

   public String getMobilePhone() {
      return this.mobilePhone;
   }

   public void setMobilePhone(String mobilePhone) {
      this.mobilePhone = mobilePhone;
   }

   public int getCampaignId() {
      return this.campaignId;
   }

   public void setCampaignId(Integer campaignId) {
      this.campaignId = campaignId;
   }

   public Map<String, String> getParameters() {
      return this.parameters;
   }

   public void setParameters(Map<String, String> parameters) {
      this.parameters = parameters;
   }

   public long getParticipationId() {
      return this.getId();
   }

   public Date getParticipationDate() {
      return this.getDateCreated();
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   public void setUserAgent(String userAgent) {
      this.userAgent = userAgent;
   }

   public String toString() {
      return "CampaignParticipantData{id=" + this.id + ", dateCreated=" + this.dateCreated + ", userId=" + this.userId + ", emailAddress='" + this.emailAddress + '\'' + ", mobilePhone='" + this.mobilePhone + '\'' + ", reference='" + this.reference + '\'' + ", campaignId=" + this.campaignId + ", userAgent='" + this.userAgent + '\'' + ", parameters=" + this.parameters + '}';
   }
}
