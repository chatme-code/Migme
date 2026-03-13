package com.projectgoth.fusion.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

public class MessageBundle {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageBundle.class));
   public static final Locale DEFAULT_LOCALE;
   public static final String MESSAGE_BUNDLE_DIR_KEY = "messageResourceBundle.dir";
   public static final String DEFAULT_BUNDLE = "resource.fusion";
   public static final ClassLoader LOADER;
   public static final Locale INDONESIAN_LOCALE;

   private static ClassLoader getDefaultLoader() {
      String loaderDir = null;
      if (null == System.getProperty("messageResourceBundle.dir")) {
         System.setProperty("messageResourceBundle.dir", ConfigUtils.getConfigDirectory());
         loaderDir = ConfigUtils.getConfigDirectory();
      } else {
         loaderDir = System.getProperty("messageResourceBundle.dir").endsWith(File.separator) ? System.getProperty("messageResourceBundle.dir") : System.getProperty("messageResourceBundle.dir") + File.separator;
      }

      File file = new File(loaderDir);

      try {
         URL[] urls = new URL[]{file.toURI().toURL()};
         return new URLClassLoader(urls);
      } catch (MalformedURLException var3) {
         log.error("Unable to load loader from default directory [" + loaderDir + "]");
         return null;
      }
   }

   public static ResourceBundle getBundle() throws MissingResourceException {
      return getBundle("resource.fusion", DEFAULT_LOCALE);
   }

   public static ResourceBundle getBundle(String bundleName) throws MissingResourceException {
      return getBundle(bundleName, DEFAULT_LOCALE);
   }

   public static ResourceBundle getBundle(String bundleName, Locale locale) throws MissingResourceException {
      try {
         return getBundle(bundleName, locale, LOADER);
      } catch (Exception var3) {
         throw new MissingResourceException("Unable to load resource bundle for " + bundleName + " " + locale.toString(), "MessageBundle", bundleName);
      }
   }

   public static ResourceBundle getBundle(String bundleName, Locale locale, ClassLoader classLoader) throws MissingResourceException {
      return ResourceBundle.getBundle(bundleName, locale, classLoader);
   }

   public static String getMessage(String key) {
      return getMessage(key);
   }

   public static String getMessage(String key, Object... parameters) {
      try {
         return MessageFormat.format(getBundle().getString(key), parameters);
      } catch (MissingResourceException var3) {
         return key;
      }
   }

   public static String getMessage(String bundleName, String key) {
      return getMessage(bundleName, key);
   }

   public static String getMessage(String bundleName, String key, Object... parameters) {
      try {
         return MessageFormat.format(getBundle(bundleName).getString(key), parameters);
      } catch (MissingResourceException var4) {
         log.error("Unable to get resourceBundle [" + bundleName + "] " + var4.getMessage());
         return key;
      }
   }

   public static String getMessage(String bundleName, Locale locale, String key) {
      try {
         return getBundle(bundleName, locale).getString(key);
      } catch (MissingResourceException var4) {
         log.error("Unable to get resourceBundle [" + bundleName + "] " + var4.getMessage());
         return key;
      }
   }

   public static String getMessage(String bundleName, Locale locale, String key, Object... parameters) {
      try {
         return MessageFormat.format(getBundle(bundleName, locale).getString(key), parameters);
      } catch (MissingResourceException var5) {
         log.error("Unable to get resourceBundle [" + bundleName + "] " + var5.getMessage());
         return key;
      }
   }

   public static String getMessage(String bundleName, Locale locale, ClassLoader loader, String key, Object... parameters) {
      try {
         return MessageFormat.format(getBundle(bundleName, locale, loader).getString(key), parameters);
      } catch (MissingResourceException var6) {
         log.error(var6.getMessage());
         return key;
      }
   }

   static {
      DEFAULT_LOCALE = Locale.ENGLISH;
      LOADER = getDefaultLoader();
      INDONESIAN_LOCALE = new Locale("id", "ID");
   }
}
