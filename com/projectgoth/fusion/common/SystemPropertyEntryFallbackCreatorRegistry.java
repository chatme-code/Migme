package com.projectgoth.fusion.common;

class SystemPropertyEntryFallbackCreatorRegistry {
   public static SystemPropertyEntryFallbackCreator getInstance(SystemPropertyEntities.SystemPropertyEntryInterface rootProperty) {
      SystemPropertyEntities.SystemPropertyEntry<?> rootPropertyEntry = rootProperty.getEntry();
      if (rootPropertyEntry instanceof SystemPropertyEntities.SystemPropertyEntryBoolean) {
         return SystemPropertyEntryFallbackCreatorRegistry.Singletons.getSystemPropertyEntryBooleanFallbackCreator();
      } else if (rootPropertyEntry instanceof SystemPropertyEntities.SystemPropertyEntryInteger) {
         return SystemPropertyEntryFallbackCreatorRegistry.Singletons.getSystemPropertyEntryIntegerFallbackCreator();
      } else if (rootPropertyEntry instanceof SystemPropertyEntities.SystemPropertyEntryString) {
         return SystemPropertyEntryFallbackCreatorRegistry.Singletons.getSystemPropertyEntryStringFallbackCreator();
      } else if (rootPropertyEntry instanceof SystemPropertyEntities.SystemPropertyEntryStringArray) {
         return SystemPropertyEntryFallbackCreatorRegistry.Singletons.getSystemPropertyEntryStringArrayFallbackCreator();
      } else {
         throw new UnsupportedOperationException("unsupported root systempropertyentryinterface " + rootPropertyEntry.getName() + " entry type is " + rootPropertyEntry.getClass());
      }
   }

   private static class Singletons {
      private static final SystemPropertyEntryFallbackCreator systemPropertyEntryBooleanFallbackCreator = new SystemPropertyEntryFallbackCreator() {
         public SystemPropertyEntities.SystemPropertyEntry<Boolean> create(final SystemPropertyEntities.SystemPropertyEntryInterface defaultFallbackProperty) {
            SystemPropertyEntities.SystemPropertyEntry<?> defaultFallbackPropertyEntry = defaultFallbackProperty.getEntry();
            return new SystemPropertyEntities.SystemPropertyEntryBoolean(defaultFallbackPropertyEntry.getName(), (Boolean)null) {
               public Boolean getDefaultValue() {
                  return SystemProperty.getBool(defaultFallbackProperty);
               }
            };
         }
      };
      private static final SystemPropertyEntryFallbackCreator systemPropertyEntryIntegerFallbackCreator = new SystemPropertyEntryFallbackCreator() {
         public SystemPropertyEntities.SystemPropertyEntry<Integer> create(final SystemPropertyEntities.SystemPropertyEntryInterface defaultFallbackProperty) {
            SystemPropertyEntities.SystemPropertyEntry<?> defaultFallbackPropertyEntry = defaultFallbackProperty.getEntry();
            return new SystemPropertyEntities.SystemPropertyEntryInteger(defaultFallbackPropertyEntry.getName(), (Integer)null) {
               public Integer getDefaultValue() {
                  return SystemProperty.getInt(defaultFallbackProperty);
               }
            };
         }
      };
      private static final SystemPropertyEntryFallbackCreator systemPropertyEntryStringFallbackCreator = new SystemPropertyEntryFallbackCreator() {
         public SystemPropertyEntities.SystemPropertyEntry<String> create(final SystemPropertyEntities.SystemPropertyEntryInterface defaultFallbackProperty) {
            SystemPropertyEntities.SystemPropertyEntry<?> defaultFallbackPropertyEntry = defaultFallbackProperty.getEntry();
            return new SystemPropertyEntities.SystemPropertyEntryString(defaultFallbackPropertyEntry.getName(), (String)null) {
               public String getDefaultValue() {
                  return SystemProperty.get(defaultFallbackProperty);
               }
            };
         }
      };
      private static final SystemPropertyEntryFallbackCreator systemPropertyEntryStringArrayFallbackCreator = new SystemPropertyEntryFallbackCreator() {
         public SystemPropertyEntities.SystemPropertyEntry<String[]> create(final SystemPropertyEntities.SystemPropertyEntryInterface defaultFallbackProperty) {
            SystemPropertyEntities.SystemPropertyEntry<?> defaultFallbackPropertyEntry = defaultFallbackProperty.getEntry();
            return new SystemPropertyEntities.SystemPropertyEntryStringArray(defaultFallbackPropertyEntry.getName(), (String[])null) {
               public String[] getDefaultValue() {
                  return SystemProperty.getArray(defaultFallbackProperty);
               }
            };
         }
      };

      public static SystemPropertyEntryFallbackCreator getSystemPropertyEntryIntegerFallbackCreator() {
         return systemPropertyEntryIntegerFallbackCreator;
      }

      public static SystemPropertyEntryFallbackCreator getSystemPropertyEntryStringFallbackCreator() {
         return systemPropertyEntryStringFallbackCreator;
      }

      public static SystemPropertyEntryFallbackCreator getSystemPropertyEntryStringArrayFallbackCreator() {
         return systemPropertyEntryStringArrayFallbackCreator;
      }

      public static SystemPropertyEntryFallbackCreator getSystemPropertyEntryBooleanFallbackCreator() {
         return systemPropertyEntryBooleanFallbackCreator;
      }
   }
}
