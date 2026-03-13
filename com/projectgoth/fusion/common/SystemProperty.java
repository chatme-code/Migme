package com.projectgoth.fusion.common;

import com.projectgoth.fusion.app.dao.db.fusiondb.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class SystemProperty {
   private static final Pattern LIST_SEPARATOR_PATTERN = Pattern.compile("(\\s*;\\s*)+");
   private static final String[] EMPTY_ARRAY = new String[0];
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SystemProperty.class));
   private static final SystemProperty.DatabaseSystemPropertyLoader DB_LOADER = new SystemProperty.DatabaseSystemPropertyLoader();
   private static SystemProperty.SystemPropertyLoader loader;

   public static SystemProperty.SystemPropertyLoader setSystemPropertyLoader(SystemProperty.SystemPropertyLoader newLoader) {
      loader = newLoader;
      return newLoader;
   }

   public static void resetSystemPropertyLoaderToDefault() {
      loader = DB_LOADER;
   }

   public static Map<String, String> getAllSystemProperties() {
      return loader.getSystemProperties();
   }

   public static synchronized void ejbInit(DataSource dataSource) throws SQLException {
      DB_LOADER.ejbInit(dataSource);
   }

   public static void resetCachedProperties() {
      loader.resetSystemProperties();
   }

   public static String get(String propertyName) throws NoSuchFieldException {
      Map<String, String> properties = loader.getSystemProperties();
      String value = (String)properties.get(propertyName.toLowerCase());
      if (value == null) {
         throw new NoSuchFieldException("Undefined system property: " + propertyName);
      } else {
         return value;
      }
   }

   public static int getInt(String propertyName) throws NoSuchFieldException {
      return Integer.parseInt(get(propertyName));
   }

   public static long getLong(String propertyName) throws NoSuchFieldException {
      return Long.parseLong(get(propertyName));
   }

   public static double getDouble(String propertyName) throws NoSuchFieldException {
      return Double.parseDouble(get(propertyName));
   }

   public static boolean getBool(String propertyName) throws NoSuchFieldException {
      return !get(propertyName).equals("0");
   }

   public static String[] getArray(String propertyName) throws NoSuchFieldException {
      String value = get(propertyName);
      return StringUtil.isBlank(value) ? EMPTY_ARRAY : LIST_SEPARATOR_PATTERN.split(value);
   }

   public static int[] getIntArray(String propertyName) throws NoSuchFieldException {
      String[] tokens = getArray(propertyName);
      int[] array = new int[tokens.length];

      for(int i = 0; i < tokens.length; ++i) {
         array[i] = Integer.parseInt(tokens[i]);
      }

      return array;
   }

   public static short[] getShortArray(String propertyName) throws NoSuchFieldException {
      String[] tokens = getArray(propertyName);
      short[] array = new short[tokens.length];

      for(int i = 0; i < tokens.length; ++i) {
         array[i] = Short.parseShort(tokens[i]);
      }

      return array;
   }

   public static double[] getDoubleArray(String propertyName) throws NoSuchFieldException {
      String[] tokens = getArray(propertyName);
      double[] array = new double[tokens.length];

      for(int i = 0; i < tokens.length; ++i) {
         array[i] = Double.parseDouble(tokens[i]);
      }

      return array;
   }

   public static String optGet(String propertyName) {
      Map<String, String> properties = loader.getSystemProperties();
      return (String)properties.get(propertyName.toLowerCase());
   }

   public static String get(String propertyName, String defaultValue) {
      String result = optGet(propertyName);
      return result == null ? defaultValue : result;
   }

   private static byte getByte(String propertyName, byte defaultValue) {
      String result = optGet(propertyName);
      if (result == null) {
         return defaultValue;
      } else {
         try {
            return Byte.parseByte(result);
         } catch (Exception var4) {
            return defaultValue;
         }
      }
   }

   public static int getInt(String propertyName, int defaultValue) {
      String result = optGet(propertyName);
      if (result == null) {
         return defaultValue;
      } else {
         try {
            return Integer.parseInt(result);
         } catch (Exception var4) {
            return defaultValue;
         }
      }
   }

   public static long getLong(String propertyName, long defaultValue) {
      String result = optGet(propertyName);
      if (result == null) {
         return defaultValue;
      } else {
         try {
            return Long.parseLong(result);
         } catch (Exception var5) {
            return defaultValue;
         }
      }
   }

   public static double getDouble(String propertyName, double defaultValue) {
      String result = optGet(propertyName);
      if (result == null) {
         return defaultValue;
      } else {
         try {
            return Double.parseDouble(result);
         } catch (Exception var5) {
            return defaultValue;
         }
      }
   }

   public static boolean getBool(String propertyName, boolean defaultValue) {
      String result = optGet(propertyName);
      if (result == null) {
         return defaultValue;
      } else {
         return !result.equals("0");
      }
   }

   private static String[] optGetStringArray(String propertyName) {
      String value = optGet(propertyName);
      return value != null && !StringUtil.isBlank(value) ? LIST_SEPARATOR_PATTERN.split(value) : null;
   }

   public static String[] getArray(String propertyName, String[] defaultValue) {
      String[] value = optGetStringArray(propertyName);
      return value != null ? value : defaultValue;
   }

   /** @deprecated */
   public static short[] getShortArray(String propertyName, short[] defaultValue) {
      try {
         String[] tokens = optGetStringArray(propertyName);
         if (tokens == null) {
            return defaultValue;
         } else {
            short[] result = new short[tokens.length];

            for(int i = 0; i < tokens.length; ++i) {
               result[i] = Short.parseShort(tokens[i]);
            }

            return result;
         }
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   public static int[] getIntArray(String propertyName, int[] defaultValue) {
      try {
         String[] tokens = optGetStringArray(propertyName);
         if (tokens == null) {
            return defaultValue;
         } else {
            int[] result = new int[tokens.length];

            for(int i = 0; i < tokens.length; ++i) {
               result[i] = Integer.parseInt(tokens[i]);
            }

            return result;
         }
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   private static double[] getDoubleArray(String propertyName, double[] defaultValue) {
      try {
         String[] tokens = optGetStringArray(propertyName);
         if (tokens == null) {
            return defaultValue;
         } else {
            double[] result = new double[tokens.length];

            for(int i = 0; i < tokens.length; ++i) {
               result[i] = Double.parseDouble(tokens[i]);
            }

            return result;
         }
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   public static String get(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      return get(propertyEntry.getName(), propertyEntry.getEntry().getDefaultValue().toString());
   }

   public static String get(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, String defaultValue) {
      return get(propertyEntry.getName(), defaultValue);
   }

   public static byte getByte(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryByte) {
         return getByte(propertyEntry.getName(), (Byte)((SystemPropertyEntities.SystemPropertyEntryByte)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getByte [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getByte [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static int getInt(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryInteger) {
         return getInt(propertyEntry.getName(), (Integer)((SystemPropertyEntities.SystemPropertyEntryInteger)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getInt [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getInt [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static int getByte(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, byte defaultValue) {
      return getByte(propertyEntry.getName(), defaultValue);
   }

   public static int getInt(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, int defaultValue) {
      return getInt(propertyEntry.getName(), defaultValue);
   }

   public static long getLong(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryLong) {
         return getLong(propertyEntry.getName(), (Long)((SystemPropertyEntities.SystemPropertyEntryLong)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getLong [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getLong [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static long getLong(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, long defaultValue) {
      return getLong(propertyEntry.getName(), defaultValue);
   }

   public static boolean getBool(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryBoolean) {
         return getBool(propertyEntry.getName(), (Boolean)((SystemPropertyEntities.SystemPropertyEntryBoolean)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getBool [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getBool [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static boolean getBool(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, boolean defaultValue) {
      return getBool(propertyEntry.getName(), defaultValue);
   }

   public static double getDouble(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryDouble) {
         return getDouble(propertyEntry.getName(), (Double)((SystemPropertyEntities.SystemPropertyEntryDouble)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getDouble [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getDouble [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static double getDouble(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, double defaultValue) {
      return getDouble(propertyEntry.getName(), defaultValue);
   }

   public static String[] getArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryStringArray) {
         return getArray(propertyEntry.getName(), (String[])((SystemPropertyEntities.SystemPropertyEntryStringArray)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getStringArray [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getStringArray string [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static String[] getArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, String[] defaultValue) {
      return getArray(propertyEntry.getName(), defaultValue);
   }

   public static int[] getIntArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryIntegerArray) {
         return getIntArray(propertyEntry.getName(), (int[])((SystemPropertyEntities.SystemPropertyEntryIntegerArray)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getIntArray [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getIntArray string [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static int[] getIntArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, int[] defaultValue) {
      return getIntArray(propertyEntry.getName(), defaultValue);
   }

   public static short[] getShortArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryShortArray) {
         return getShortArray(propertyEntry.getName(), (short[])((SystemPropertyEntities.SystemPropertyEntryShortArray)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getShortArray [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getShortArray string [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static double[] getDoubleArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      if (propertyEntry.getEntry() instanceof SystemPropertyEntities.SystemPropertyEntryDoubleArray) {
         return getDoubleArray(propertyEntry.getName(), (double[])((SystemPropertyEntities.SystemPropertyEntryDoubleArray)propertyEntry.getEntry()).getDefaultValue());
      } else {
         log.error(String.format("incorrect property type for getDoubleArray [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
         throw new IllegalArgumentException(String.format("incorrect property type for getDoubleArray string [%s] [%s]", propertyEntry.getClass().getName(), propertyEntry.getName()));
      }
   }

   public static double[] getDoubleArray(SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry, double[] defaultValue) {
      return getDoubleArray(propertyEntry.getName(), defaultValue);
   }

   public static boolean isValueInArray(String value, SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      boolean found = false;
      if (!StringUtil.isBlank(value)) {
         String[] values = getArray(propertyEntry);
         if (values != null) {
            String[] arr$ = values;
            int len$ = values.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String v = arr$[i$];
               if (value.equalsIgnoreCase(v)) {
                  found = true;
                  break;
               }
            }
         }
      }

      return found;
   }

   public static boolean isValueInIntegerArray(int value, SystemPropertyEntities.SystemPropertyEntryInterface propertyEntry) {
      boolean found = false;
      int[] values = getIntArray(propertyEntry);
      if (values != null) {
         int[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Integer v = arr$[i$];
            if (v.equals(value)) {
               found = true;
               break;
            }
         }
      }

      return found;
   }

   public static <T extends Number> T getNumber(SystemPropertyEntities.SystemPropertyEntryInterface sysProp, Class<T> valueType) {
      String propValue = get(sysProp);
      if (Double.class.equals(valueType)) {
         return Double.valueOf(propValue);
      } else if (Integer.class.equals(valueType)) {
         return Integer.valueOf(propValue);
      } else if (Long.class.equals(valueType)) {
         return Long.valueOf(propValue);
      } else {
         throw new IllegalArgumentException("value type " + valueType + " not supported");
      }
   }

   static {
      loader = DB_LOADER;
   }

   public static class DatabaseSystemPropertyLoader implements SystemProperty.SystemPropertyLoader {
      private static final int LOCAL_CACHE_TIME = 60000;
      private final LazyLoader<Map<String, String>> local_system_properties = new LazyLoader<Map<String, String>>("LOCAL_SYSTEM_PROPERTIES", 60000L) {
         protected Map<String, String> fetchValue() throws SQLException {
            return DatabaseSystemPropertyLoader.this.retrievePropertiesFromDBDirectly();
         }
      };

      public Map<String, String> retrievePropertiesFromDBDirectly() throws SQLException {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            if (SystemProperty.log.isDebugEnabled()) {
               SystemProperty.log.debug("Loading system properties directly from fusion database");
            }

            conn = DBUtils.getFusionReadConnection();
            ps = conn.prepareStatement("select * from system");
            rs = ps.executeQuery();
            ConcurrentHashMap properties = new ConcurrentHashMap();

            while(rs.next()) {
               properties.put(rs.getString("propertyname").toLowerCase(), rs.getString("propertyvalue"));
            }

            ConcurrentHashMap var5 = properties;
            return var5;
         } catch (SQLException var10) {
            SystemProperty.log.error("Failed to get System properties from database directly", var10);
            throw var10;
         } finally {
            DBUtils.closeResource(rs, ps, conn, SystemProperty.log);
         }
      }

      public synchronized void ejbInit(DataSource dataSource) throws SQLException {
         this.local_system_properties.getValue();
      }

      public synchronized void resetSystemProperties() {
      }

      public Map<String, String> getSystemProperties() {
         return (Map)this.local_system_properties.getValue();
      }
   }

   public interface SystemPropertyLoader {
      Map<String, String> getSystemProperties();

      void resetSystemProperties();
   }
}
