package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.codehaus.jackson.JsonNode;

public class MerchantLocationData implements Serializable {
   private int id;
   private int locationId;
   private String username;
   private String name;
   private String address;
   private String phoneNumber;
   private String emailAddress;
   private String notes;
   private int status;
   private JsonNode userData;

   public JsonNode getUserData() {
      return this.userData;
   }

   public void setUserData(JsonNode userData) {
      this.userData = userData;
   }

   public MerchantLocationData() {
   }

   public MerchantLocationData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.locationId = rs.getInt("locationId");
      this.username = rs.getString("username");
      this.name = rs.getString("name");
      this.address = rs.getString("address");
      this.phoneNumber = rs.getString("phoneNumber");
      this.emailAddress = rs.getString("emailAddress");
      this.notes = rs.getString("notes");
      this.status = rs.getInt("status");
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getLocationId() {
      return this.locationId;
   }

   public void setLocationId(int locationId) {
      this.locationId = locationId;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getAddress() {
      return this.address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public String getPhoneNumber() {
      return this.phoneNumber;
   }

   public void setPhoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public String getEmailAddress() {
      return this.emailAddress;
   }

   public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
   }

   public String getNotes() {
      return this.notes;
   }

   public void setNotes(String notes) {
      this.notes = notes;
   }

   public int getStatus() {
      return this.status;
   }

   public void setStatus(int status) {
      this.status = status;
   }
}
