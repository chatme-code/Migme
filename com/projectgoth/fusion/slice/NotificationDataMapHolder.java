package com.projectgoth.fusion.slice;

import java.util.Map;

public final class NotificationDataMapHolder {
   public Map<Integer, Map<String, Map<String, String>>> value;

   public NotificationDataMapHolder() {
   }

   public NotificationDataMapHolder(Map<Integer, Map<String, Map<String, String>>> value) {
      this.value = value;
   }
}
