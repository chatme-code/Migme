package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "settings"
)
public class SettingsProfileDetailsData {
   private static final Pattern emailAddressRegex = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
   public DataWithPrivacyData<Integer, SettingsEnums.ShowHide> id;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> username;
   public DataWithPrivacyData<Boolean, SettingsEnums.ShowHide> isVerified;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> aboutMeVerified;
   public DataWithPrivacyData<Date, SettingsEnums.ShowHide> dateRegistered;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> country;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> gender;
   public DataWithPrivacyData<Integer, SettingsEnums.ShowHide> relationship;
   public DataWithPrivacyData<Date, SettingsEnums.Birthday> dateOfBirth;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> interests;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> aboutMe;
   public DataWithPrivacyData<SettingsProfileDetailsData.FirstLastName, SettingsEnums.ShowHide> firstLastName;
   public DataWithPrivacyData<String, SettingsEnums.EveryoneFriendHide> mobilePhone;
   public DataWithPrivacyData<String, SettingsEnums.EveryoneFollowerFriendHide> externalEmail;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> mig33Email;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> city;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> state;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> schools;
   public DataWithPrivacyData<String, SettingsEnums.ShowHide> companies;
   public boolean externalEmailVerified = false;
   public static final SettingsEnums.Birthday PRIVACY_DEFAULT_BIRTHDAY;
   public static final SettingsEnums.ShowHide PRIVACY_DEFAULT_FIRSTLASTNAME;
   public static final SettingsEnums.EveryoneFriendHide PRIVACY_DEFAULT_MOBILEPHONE;
   public static final SettingsEnums.EveryoneFollowerFriendHide PRIVACY_DEFAULT_EXTERNALEMAIL;

   public SettingsProfileDetailsData() {
   }

   public SettingsProfileDetailsData(UserData userData, UserProfileData upData, CountryData countryData, Map<String, Integer> privacySettings) {
      if (upData == null) {
         upData = new UserProfileData();
      }

      this.id = new DataWithPrivacyData(userData.userID, SettingsEnums.ShowHide.SHOW);
      this.username = new DataWithPrivacyData(userData.username, SettingsEnums.ShowHide.SHOW);
      this.isVerified = new DataWithPrivacyData(userData.accountVerified == UserData.AccountVerifiedEnum.VERIFIED_OK, SettingsEnums.ShowHide.SHOW);
      this.aboutMeVerified = new DataWithPrivacyData(userData.aboutMeVerified, SettingsEnums.ShowHide.SHOW);
      this.dateRegistered = new DataWithPrivacyData(userData.dateRegistered, SettingsEnums.ShowHide.SHOW);
      this.country = new DataWithPrivacyData(countryData.name, SettingsEnums.ShowHide.SHOW);
      this.gender = new DataWithPrivacyData(upData.gender == null ? null : upData.gender.value(), SettingsEnums.ShowHide.SHOW);
      this.relationship = new DataWithPrivacyData(upData.relationshipStatus == null ? null : upData.relationshipStatus.value(), SettingsEnums.ShowHide.SHOW);
      this.dateOfBirth = new DataWithPrivacyData(upData.dateOfBirth, privacySettings.containsKey("DobPrivacy") ? SettingsEnums.Birthday.fromValue((Integer)privacySettings.get("DobPrivacy")) : PRIVACY_DEFAULT_BIRTHDAY);
      this.interests = new DataWithPrivacyData(upData.hobbies, SettingsEnums.ShowHide.SHOW);
      this.aboutMe = new DataWithPrivacyData(upData.aboutMe, SettingsEnums.ShowHide.SHOW);
      this.firstLastName = new DataWithPrivacyData(new SettingsProfileDetailsData.FirstLastName(upData.firstName, upData.lastName), privacySettings.containsKey("FLNamePv") ? SettingsEnums.ShowHide.fromValue((Integer)privacySettings.get("FLNamePv")) : PRIVACY_DEFAULT_FIRSTLASTNAME);
      this.mobilePhone = new DataWithPrivacyData(userData.mobilePhone, privacySettings.containsKey("MobNumPrivacy") ? SettingsEnums.EveryoneFriendHide.fromValue((Integer)privacySettings.get("MobNumPrivacy")) : PRIVACY_DEFAULT_MOBILEPHONE);
      this.externalEmail = new DataWithPrivacyData(userData.emailAddress, privacySettings.containsKey("ExtEmPv") ? SettingsEnums.EveryoneFollowerFriendHide.fromValue((Integer)privacySettings.get("ExtEmPv")) : PRIVACY_DEFAULT_EXTERNALEMAIL);
      this.externalEmailVerified = userData.emailVerified;
      this.mig33Email = new DataWithPrivacyData(this.getMig33EmailFromUserData(userData), SettingsEnums.ShowHide.SHOW);
      this.city = new DataWithPrivacyData(upData.city, SettingsEnums.ShowHide.SHOW);
      this.state = new DataWithPrivacyData(upData.state, SettingsEnums.ShowHide.SHOW);
      this.schools = new DataWithPrivacyData(upData.schools, SettingsEnums.ShowHide.SHOW);
      this.companies = new DataWithPrivacyData(upData.jobs, SettingsEnums.ShowHide.SHOW);
   }

   public String getMig33EmailFromUserData(UserData userData) {
      boolean isUsernameValid = true;
      return !isUsernameValid && userData.alias == null ? null : String.format("%s@mig33.com", userData.alias == null ? userData.username : userData.alias);
   }

   public UserProfileData updateUserProfileData(UserProfileData upData) {
      upData.gender = this.gender.value == null ? null : UserProfileData.GenderEnum.fromValue((String)this.gender.value);
      upData.relationshipStatus = this.relationship.value == null ? null : UserProfileData.RelationshipStatusEnum.fromValue((Integer)this.relationship.value);
      upData.dateOfBirth = (Date)this.dateOfBirth.value;
      upData.hobbies = (String)this.interests.value;
      upData.aboutMe = (String)this.aboutMe.value;
      upData.firstName = ((SettingsProfileDetailsData.FirstLastName)this.firstLastName.value).firstName;
      upData.lastName = ((SettingsProfileDetailsData.FirstLastName)this.firstLastName.value).lastName;
      upData.city = (String)this.city.value;
      upData.state = (String)this.state.value;
      upData.schools = (String)this.schools.value;
      upData.jobs = (String)this.companies.value;
      return upData;
   }

   public boolean updateUserData(UserData userData) {
      boolean changed = false;
      if ((userData.emailAddress != null || this.externalEmail.value != null) && (userData.emailAddress == null || !userData.emailAddress.equals(this.externalEmail.value)) || this.externalEmail.value != null && null == userData.emailAddress) {
         userData.emailAddress = (String)this.externalEmail.value;
         changed = true;
      }

      return changed;
   }

   public Map<String, Integer> retrievePrivacy() {
      Map<String, Integer> values = new HashMap();
      values.put("DobPrivacy", (this.dateOfBirth.privacy == null ? PRIVACY_DEFAULT_BIRTHDAY : (SettingsEnums.Birthday)this.dateOfBirth.privacy).value());
      values.put("FLNamePv", (this.firstLastName.privacy == null ? PRIVACY_DEFAULT_FIRSTLASTNAME : (SettingsEnums.ShowHide)this.firstLastName.privacy).value());
      values.put("MobNumPrivacy", (this.mobilePhone.privacy == null ? PRIVACY_DEFAULT_MOBILEPHONE : (SettingsEnums.EveryoneFriendHide)this.mobilePhone.privacy).value());
      values.put("ExtEmPv", (this.externalEmail.privacy == null ? PRIVACY_DEFAULT_EXTERNALEMAIL : (SettingsEnums.EveryoneFollowerFriendHide)this.externalEmail.privacy).value());
      return values;
   }

   public Map<String, String> retrieveAdditionalUserInfos() {
      Map<String, String> values = new HashMap();
      return values;
   }

   static {
      PRIVACY_DEFAULT_BIRTHDAY = SettingsEnums.Birthday.HIDE;
      PRIVACY_DEFAULT_FIRSTLASTNAME = SettingsEnums.ShowHide.HIDE;
      PRIVACY_DEFAULT_MOBILEPHONE = SettingsEnums.EveryoneFriendHide.HIDE;
      PRIVACY_DEFAULT_EXTERNALEMAIL = SettingsEnums.EveryoneFollowerFriendHide.HIDE;
   }

   public static class FirstLastName {
      public String firstName;
      public String lastName;

      public FirstLastName() {
      }

      public FirstLastName(String firstName, String lastName) {
         this.firstName = firstName;
         this.lastName = lastName;
      }
   }
}
