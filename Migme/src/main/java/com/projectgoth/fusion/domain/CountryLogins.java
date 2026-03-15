package com.projectgoth.fusion.domain;

public class CountryLogins {
   private Integer country;
   private Integer logins;
   private Boolean mobileVerified;

   public CountryLogins(Integer country, Integer logins, Boolean mobileVerified) {
      this.country = country;
      this.logins = logins;
      this.mobileVerified = mobileVerified;
   }

   public Integer getCountry() {
      return this.country;
   }

   public Integer getLogins() {
      return this.logins;
   }

   public Boolean getMobileVerified() {
      return this.mobileVerified;
   }
}
