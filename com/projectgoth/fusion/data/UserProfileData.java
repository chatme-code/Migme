package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserProfileData implements Serializable {
   public Integer id;
   public String username;
   public String firstName;
   public String lastName;
   public String homeTown;
   public String city;
   public String state;
   public Date dateOfBirth;
   public UserProfileData.GenderEnum gender;
   public String jobs;
   public String schools;
   public String hobbies;
   public String likes;
   public String dislikes;
   public String aboutMe;
   public UserProfileData.RelationshipStatusEnum relationshipStatus;
   public UserProfileData.StatusEnum status;
   public Boolean anonymousViewing;
   public Integer numProfileViews;

   public UserProfileData() {
   }

   public UserProfileData(UserData userData) {
      this.id = userData.userID;
      this.username = userData.username;
      this.firstName = "";
      this.lastName = "";
      this.homeTown = "";
      this.city = "";
      this.state = "";
      this.dateOfBirth = userData.dateRegistered;
      this.gender = UserProfileData.GenderEnum.FEMALE;
      this.jobs = "";
      this.schools = "";
      this.hobbies = "";
      this.likes = "";
      this.dislikes = "";
      this.aboutMe = "";
      this.relationshipStatus = UserProfileData.RelationshipStatusEnum.COMPLICATED;
      this.status = UserProfileData.StatusEnum.PUBLIC;
      this.anonymousViewing = false;
   }

   public UserProfileData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.firstName = rs.getString("firstName");
      this.lastName = rs.getString("lastName");
      this.homeTown = rs.getString("homeTown");
      this.city = rs.getString("city");
      this.state = rs.getString("state");
      this.dateOfBirth = rs.getTimestamp("dateOfBirth");
      this.jobs = rs.getString("jobs");
      this.schools = rs.getString("schools");
      this.hobbies = rs.getString("hobbies");
      this.likes = rs.getString("likes");
      this.dislikes = rs.getString("dislikes");
      this.aboutMe = rs.getString("aboutMe");
      String strVal = rs.getString("gender");
      if (strVal != null) {
         this.gender = UserProfileData.GenderEnum.fromValue(strVal);
      }

      Integer intVal = (Integer)rs.getObject("relationshipStatus");
      if (intVal != null) {
         this.relationshipStatus = UserProfileData.RelationshipStatusEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = UserProfileData.StatusEnum.fromValue(intVal);
      }

   }

   public boolean isDifferent(UserProfileData oldProfileData) {
      if (this.username != null && !this.username.equals(oldProfileData.username)) {
         return true;
      } else if (this.firstName != null && !this.firstName.equals(oldProfileData.firstName)) {
         return true;
      } else if (this.lastName != null && !this.lastName.equals(oldProfileData.lastName)) {
         return true;
      } else if (this.homeTown != null && !this.homeTown.equals(oldProfileData.homeTown)) {
         return true;
      } else if (this.city != null && !this.city.equals(oldProfileData.city)) {
         return true;
      } else if (this.state != null && !this.state.equals(oldProfileData.state)) {
         return true;
      } else if (this.dateOfBirth != null && !this.dateOfBirth.equals(oldProfileData.dateOfBirth)) {
         return true;
      } else if (this.gender != null && this.gender != oldProfileData.gender) {
         return true;
      } else if (this.jobs != null && !this.jobs.equals(oldProfileData.jobs)) {
         return true;
      } else if (this.schools != null && !this.schools.equals(oldProfileData.schools)) {
         return true;
      } else if (this.hobbies != null && !this.hobbies.equals(oldProfileData.hobbies)) {
         return true;
      } else if (this.likes != null && !this.likes.equals(oldProfileData.likes)) {
         return true;
      } else if (this.dislikes != null && !this.dislikes.equals(oldProfileData.dislikes)) {
         return true;
      } else if (this.aboutMe != null && !this.aboutMe.equals(oldProfileData.aboutMe)) {
         return true;
      } else if (this.relationshipStatus != null && this.relationshipStatus != oldProfileData.relationshipStatus) {
         return true;
      } else if (this.status != null && this.status != oldProfileData.status) {
         return true;
      } else {
         return this.anonymousViewing != null && !this.anonymousViewing.equals(oldProfileData.anonymousViewing);
      }
   }

   public static enum StatusEnum {
      PUBLIC(1),
      CONTACTS_ONLY(2),
      PRIVATE(3);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserProfileData.StatusEnum fromValue(int value) {
         UserProfileData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserProfileData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum RelationshipStatusEnum {
      SINGLE(1),
      IN_A_RELATIONSHIP(2),
      DOMESTIC_PARTNER(3),
      MARRIED(4),
      COMPLICATED(5);

      private int value;

      private RelationshipStatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserProfileData.RelationshipStatusEnum fromValue(int value) {
         UserProfileData.RelationshipStatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserProfileData.RelationshipStatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum GenderEnum {
      MALE("M"),
      FEMALE("F");

      private String value;

      private GenderEnum(String value) {
         this.value = value;
      }

      public String value() {
         return this.value;
      }

      public static UserProfileData.GenderEnum fromValue(String value) {
         UserProfileData.GenderEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserProfileData.GenderEnum e = arr$[i$];
            if (value.equals(e.value())) {
               return e;
            }
         }

         return null;
      }
   }
}
