package com.projectgoth.fusion.restapi.data;

import java.util.Map;

public class Alert implements Comparable<Alert> {
   String timestamp;
   Integer alertType;
   String alertKey;
   Map<String, String> alertData;

   public Alert(String timestamp, Integer alertType, String alertKey, Map<String, String> alertData) {
      this.timestamp = timestamp;
      this.alertType = alertType;
      this.alertKey = alertKey;
      this.alertData = alertData;
   }

   public int compareTo(Alert a) {
      return a == null ? 1 : a.timestamp.compareTo(this.timestamp);
   }

   public boolean equals(Object a) {
      return a.getClass().equals(this.getClass()) ? this.equals((Alert)a) : false;
   }

   public boolean equals(Alert a) {
      if (this.timestamp != null && this.alertType != null && this.alertKey != null) {
         return this.timestamp.equals(a.timestamp) && this.alertType.equals(a.alertType) && this.alertKey.equals(a.alertKey);
      } else {
         return false;
      }
   }
}
