package com.projectgoth.fusion.recommendation.collector.addressbook;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.recommendation.collector.sinks.log4j.Log4JSink;
import org.apache.log4j.Logger;

public class AddressBookDataLog4JSink extends Log4JSink<AddressBookRecordData> {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AddressBookDataLog4JSink.class));
   private static final char FIELD_SEPARATOR = '\u0001';

   public AddressBookDataLog4JSink(String name, String logCategoryName) {
      super(name, logCategoryName);
   }

   protected String toString(AddressBookRecordData record) {
      StringBuilder builder = new StringBuilder();
      builder.append(DateTimeUtils.getStringForTimestamp(record.createDate)).append('\u0001').append(record.createDate.getTime()).append('\u0001').append(record.submitterUserID).append('\u0001').append(record.contactType).append('\u0001').append(sanitizeContactValue(record.contactValue));
      return builder.toString();
   }

   public static String sanitizeContactValue(String contactValue) {
      if (contactValue == null) {
         log.warn("Found null contact value");
         return null;
      } else {
         boolean hasIllegalCharsReplaced = false;
         contactValue = contactValue.trim();
         StringBuilder contactValueStringBuilder = new StringBuilder(contactValue.length());

         for(int i = 0; i < contactValue.length(); ++i) {
            char c = contactValue.charAt(i);
            if (c < ' ') {
               hasIllegalCharsReplaced = true;
               contactValueStringBuilder.append(' ');
            } else {
               contactValueStringBuilder.append(c);
            }
         }

         if (hasIllegalCharsReplaced) {
            log.warn("Contact value [" + contactValue + "] has some illegal characters");
         }

         return contactValueStringBuilder.toString();
      }
   }
}
