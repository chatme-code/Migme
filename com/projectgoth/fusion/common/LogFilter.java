package com.projectgoth.fusion.common;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class LogFilter {
   private Logger log;
   private SystemPropertyEntities.SystemPropertyEntryInterface filterSysprop;
   private static AtomicReference<String> currentFilter = new AtomicReference();
   private static AtomicReference<Pattern> pattern = new AtomicReference();

   public LogFilter(Logger log, SystemPropertyEntities.SystemPropertyEntryInterface filterSysprop) {
      this.log = log;
      this.filterSysprop = filterSysprop;
   }

   public Logger getLogger() {
      return this.log;
   }

   private boolean isFilterBlank() {
      String filter = SystemProperty.get(this.filterSysprop);
      if (StringUtil.isBlank(filter)) {
         return true;
      } else {
         if (!filter.equals(currentFilter.get())) {
            currentFilter.set(filter);
            pattern.set(Pattern.compile(filter));
         }

         return false;
      }
   }

   private boolean isAllowed(Object message) {
      if (message == null) {
         return true;
      } else if (this.isFilterBlank()) {
         return true;
      } else {
         return !((Pattern)pattern.get()).matcher(message.toString()).matches();
      }
   }

   private boolean isAllowed(Object message, Throwable t) {
      if (message == null && t == null) {
         return true;
      } else if (this.isFilterBlank()) {
         return true;
      } else if (((Pattern)pattern.get()).matcher(message.toString()).matches()) {
         return false;
      } else {
         return !((Pattern)pattern.get()).matcher(t.toString()).matches();
      }
   }

   public void debug(Object message) {
      if (this.log.isDebugEnabled() && this.isAllowed(message)) {
         this.log.debug(message);
      }

   }

   public void debug(Object message, Throwable t) {
      if (this.log.isDebugEnabled() && this.isAllowed(message, t)) {
         this.log.debug(message, t);
      }

   }

   public void error(Object message) {
      if (this.isAllowed(message)) {
         this.log.error(message);
      }

   }

   public void error(Object message, Throwable t) {
      if (this.isAllowed(message, t)) {
         this.log.error(message, t);
      }

   }

   public void fatal(Object message) {
      if (this.isAllowed(message)) {
         this.log.fatal(message);
      }

   }

   public void fatal(Object message, Throwable t) {
      if (this.isAllowed(message, t)) {
         this.log.fatal(message, t);
      }

   }

   public void info(Object message) {
      if (this.isAllowed(message)) {
         this.log.info(message);
      }

   }

   public void info(Object message, Throwable t) {
      if (this.isAllowed(message, t)) {
         this.log.info(message, t);
      }

   }

   public void warn(Object message) {
      if (this.isAllowed(message)) {
         this.log.warn(message);
      }

   }

   public void warn(Object message, Throwable t) {
      if (this.isAllowed(message, t)) {
         this.log.warn(message, t);
      }

   }

   public boolean isDebugEnabled() {
      return this.log.isDebugEnabled();
   }
}
