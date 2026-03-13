package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class PromotedPostData implements Serializable {
   private String url;
   private int slot;
   private Date startDate;
   private Date endDate;
   private int countryID;
   private boolean status;

   public PromotedPostData() {
   }

   public PromotedPostData(ResultSet rs) throws SQLException {
      this.setUrl(rs.getString("url"));
      this.setSlot(rs.getInt("slot"));
      this.setStatus(rs.getBoolean("status"));
      this.setStartDate(new Date(rs.getTimestamp("startdate").getTime()));
      this.setEndDate(new Date(rs.getTimestamp("enddate").getTime()));
      this.setCountryID(rs.getInt("countryid"));
   }

   public String getUrl() {
      return this.url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public Date getStartDate() {
      return this.startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public Date getEndDate() {
      return this.endDate;
   }

   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }

   public int getCountryID() {
      return this.countryID;
   }

   public void setCountryID(int countryID) {
      this.countryID = countryID;
   }

   public boolean isStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
      this.status = status;
   }
}
