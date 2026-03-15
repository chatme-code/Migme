package com.projectgoth.fusion.restapi.data;

public class DataWithPrivacyData<ValueType, PrivacyType> {
   public ValueType value;
   public PrivacyType privacy;

   public DataWithPrivacyData() {
   }

   DataWithPrivacyData(ValueType thevalue, PrivacyType theprivacy) {
      this.value = thevalue;
      this.privacy = theprivacy;
   }
}
