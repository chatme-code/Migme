package com.projectgoth.fusion.slice;

import java.util.Map;

public final class NotificationDataEntryHolder {
   public Map<String, Map<String, String>> value;

   public NotificationDataEntryHolder() {
   }

   public NotificationDataEntryHolder(Map<String, Map<String, String>> value) {
      this.value = value;
   }
}
