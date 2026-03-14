package com.projectgoth.fusion.common;

import java.io.File;

public abstract class ConfigUtils {
   public static final String CONFIG_ENV_NAME = "config.dir";
   public static final String ROOT_LOG_FOLDER_ENV_NAME = "log.dir";
   public static final String LOG_FILENAME = "log.filename";
   public static final String DEFAULT_INSTANCE_NAME = "java";

   public static final String getConfigDirectory() {
      if (System.getProperty("config.dir") != null) {
         return System.getProperty("config.dir").endsWith(File.separator) ? System.getProperty("config.dir") : System.getProperty("config.dir") + File.separator;
      } else {
         return "";
      }
   }

   public static final String getDefaultLog4jConfigFilename() {
      return getConfigDirectory() + "log4j.xml";
   }

   public static final String getApplicationLog4jFilename(String applicationName) {
      return getConfigDirectory() + "log4j." + applicationName + ".xml";
   }

   public static String getLoggerName(Class clazz) {
      return clazz.getName().startsWith("com.projectgoth.fusion") ? clazz.getName().substring(23) : clazz.getName();
   }

   public static String getRootLogFolder() {
      if (!StringUtil.isBlank(System.getProperty("log.dir"))) {
         return System.getProperty("log.dir").endsWith(File.separator) ? System.getProperty("log.dir") : System.getProperty("log.dir") + File.separator;
      } else {
         return "";
      }
   }

   public static boolean mkdirsRootLogFolder() {
      String rootLogFolder = getRootLogFolder();
      if (StringUtil.isBlank(rootLogFolder)) {
         return false;
      } else {
         File logFolder = new File(rootLogFolder);
         return !logFolder.exists() ? logFolder.mkdirs() : false;
      }
   }

   public static final String getInstanceName() {
      String logFilename = System.getProperty("log.filename");
      return StringUtil.isBlank(logFilename) ? "java" : logFilename;
   }
}
