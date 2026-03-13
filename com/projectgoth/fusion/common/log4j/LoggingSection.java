package com.projectgoth.fusion.common.log4j;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;

public class LoggingSection {
   private static String getName(Enum<?> e) {
      return e.name();
   }

   private static String getNamespace(Enum<?> e) {
      return e.getDeclaringClass().getSimpleName();
   }

   public static boolean isEnabled(LoggingSection.LoggingSectionInterface section) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.LoggingSections.ENABLED)) {
         return false;
      } else {
         String loggingFeatureNamespace = SystemPropertyEntities.getNameWithNamespace("LoggingSections", "ENABLED");
         String loggingForGroupFeaturePropertyName = SystemPropertyEntities.getNameWithNamespace(loggingFeatureNamespace, section.getLoggingSectionNamespace());
         boolean loggingForGroupEnabled = SystemProperty.getBool(loggingForGroupFeaturePropertyName, true);
         if (!loggingForGroupEnabled) {
            return false;
         } else {
            String loggingForSectionPropertyName = SystemPropertyEntities.getNameWithNamespace(loggingForGroupFeaturePropertyName, section.getSectionName());
            return SystemProperty.getBool(loggingForSectionPropertyName, true);
         }
      }
   }

   public static Level getLogLevel(LoggingSection.LoggingSectionInterface section) {
      String logLevelPropertyName = SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getNameWithNamespace(SystemPropertyEntities.getNameWithNamespace("LoggingSections", "LEVEL"), section.getLoggingSectionNamespace()), section.getSectionName());
      String logLevelName = SystemProperty.get((String)logLevelPropertyName, (String)null);
      if (logLevelName == null) {
         return Level.DEBUG;
      } else {
         Level level = (Level)LoggingSection.SingletonHolder.getLevelNameMap().get(StringUtil.trimmedUpperCase(logLevelName));
         return level == null ? Level.DEBUG : level;
      }
   }

   public static enum AuthServiceI implements LoggingSection.LoggingSectionInterface {
      CreateCredCheckPasswd(true, Level.INFO),
      UpdateCredCheckPasswd(true, Level.INFO),
      UpdateFusionCredCheckPasswd(true, Level.INFO);

      private boolean enabledByDefault;
      private Level defaultLogLevel;

      private AuthServiceI(boolean enabledByDefault, Level level) {
         this.enabledByDefault = enabledByDefault;
         this.defaultLogLevel = level;
      }

      public boolean isEnabledByDefault() {
         return this.enabledByDefault;
      }

      public String getSectionName() {
         return LoggingSection.getName(this);
      }

      public Level getDefaultLogLevel() {
         return this.defaultLogLevel;
      }

      public String getLoggingSectionNamespace() {
         return LoggingSection.getNamespace(this);
      }
   }

   public interface LoggingSectionInterface {
      boolean isEnabledByDefault();

      String getSectionName();

      Level getDefaultLogLevel();

      String getLoggingSectionNamespace();
   }

   private static class SingletonHolder {
      private static final Map<String, Level> levelNameMap = buildMap();

      private static Map<String, Level> buildMap() {
         Map<String, Level> m = new HashMap();
         m.put("DEBUG", Level.DEBUG);
         m.put("INFO", Level.INFO);
         m.put("WARN", Level.WARN);
         m.put("ERROR", Level.ERROR);
         m.put("FATAL", Level.FATAL);
         m.put("OFF", Level.OFF);
         return Collections.unmodifiableMap(m);
      }

      public static Map<String, Level> getLevelNameMap() {
         return levelNameMap;
      }
   }
}
