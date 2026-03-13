package com.projectgoth.fusion.common;

import com.danga.MemCached.MemCachedClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class MemCachedClientWrapper {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCachedClientWrapper.class));
   public static int MAX_KEY_LENGTH = 250;
   private static final Map<MemCachedUtils.Instance, MemCachedClient> clientMap = new HashMap();

   public static MemCachedClient getMemCachedClient(MemCachedUtils.Instance instance) {
      return (MemCachedClient)clientMap.get(instance);
   }

   public static boolean add(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object value) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).add(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), value, keySpace.getExpiryDate());
   }

   public static boolean add(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object value, long ttl) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).add(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), value, ttl > 0L ? new Date(System.currentTimeMillis() + ttl) : keySpace.getExpiryDate());
   }

   public static boolean set(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object value) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).set(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), value, keySpace.getExpiryDate());
   }

   public static boolean set(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object value, long ttl) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).set(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), value, ttl > 0L ? new Date(System.currentTimeMillis() + ttl) : keySpace.getExpiryDate());
   }

   public static boolean delete(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).delete(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key));
   }

   public static long incr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return incr(keySpace, key, 1L);
   }

   public static long incr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, long incrValue) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).incr(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), incrValue);
   }

   public static long decr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return decr(keySpace, key, 1L);
   }

   public static long decr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, long incrValue) {
      return ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).decr(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key), incrValue);
   }

   public static long addOrIncr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return addOrIncr(keySpace, key, keySpace.getCacheTime());
   }

   public static long addOrIncr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, long ttl) {
      return addOrIncr(keySpace, key, 1L, ttl);
   }

   public static long addOrIncr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, long incrValue, long ttl) {
      boolean ret = add(keySpace, key, incrValue, ttl);
      return ret ? incrValue : incr(keySpace, key, incrValue);
   }

   public static Object get(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      String fullKey = MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key);
      return fullKey.length() > MAX_KEY_LENGTH ? null : ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).get(fullKey);
   }

   public static long getCounter(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      String fullKey = MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key);
      return fullKey.length() > MAX_KEY_LENGTH ? -1L : ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getCounter(fullKey);
   }

   public static <TContext> long getCounter(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      String fullKey = MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key);
      if (fullKey.length() > MAX_KEY_LENGTH) {
         return -1L;
      } else {
         long currentCounter = getCounter(keySpace, key);
         if (currentCounter >= 0L) {
            return currentCounter;
         } else {
            long initialCounter = -1L;
            MemCachedDistributedLock.LockInstance lockInstance = MemCachedDistributedLock.getDistributedLock(initialCounterValueProvider.getInitialValueLockKeySpace(keySpace, key, contextData), initialCounterValueProvider.getInitialValueLockKeyName(keySpace, key, contextData), initialCounterValueProvider.getInitialValueLockWaitTime(keySpace, key, contextData), -1L);
            if (lockInstance != null) {
               try {
                  currentCounter = getCounter(keySpace, key);
                  if (currentCounter >= 0L) {
                     initialCounter = currentCounter;
                  } else {
                     initialCounter = initialCounterValueProvider.getInitialValue(keySpace, key, contextData);
                     if (initialCounter >= 0L) {
                        set(keySpace, key, initialCounter, initialCounterValueProvider.getInitialValueTimeToLive(keySpace, key, contextData));
                     }
                  }
               } finally {
                  lockInstance.release(false);
               }
            }

            return initialCounter;
         }
      }
   }

   public static <TContext> long incr(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, long incrValue, TContext contextData) {
      String fullKey = MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key);
      if (fullKey.length() > MAX_KEY_LENGTH) {
         return -1L;
      } else {
         long newValue = incr(keySpace, key, incrValue);
         if (newValue >= 0L) {
            return newValue;
         } else {
            newValue = -1L;
            MemCachedDistributedLock.LockInstance lockInstance = MemCachedDistributedLock.getDistributedLock(initialCounterValueProvider.getInitialValueLockKeySpace(keySpace, key, contextData), initialCounterValueProvider.getInitialValueLockKeyName(keySpace, key, contextData), initialCounterValueProvider.getInitialValueLockWaitTime(keySpace, key, contextData), -1L);
            if (lockInstance != null) {
               try {
                  newValue = incr(keySpace, key, incrValue);
                  if (newValue < 0L) {
                     long initialCounter = initialCounterValueProvider.getInitialValue(keySpace, key, contextData);
                     if (initialCounter >= 0L) {
                        add(keySpace, key, initialCounter, initialCounterValueProvider.getInitialValueTimeToLive(keySpace, key, contextData));
                        newValue = incr(keySpace, key, incrValue);
                     }
                  }
               } finally {
                  lockInstance.release(false);
               }
            }

            return newValue;
         }
      }
   }

   public static String getString(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return toString(get(keySpace, key));
   }

   public static Integer getInt(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return toInt(get(keySpace, key));
   }

   public static Long getLong(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return toLong(get(keySpace, key));
   }

   public static Double getDouble(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      return toDouble(get(keySpace, key));
   }

   public static Map<String, Object> getMulti(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      Map<String, Object> map = new HashMap();
      Iterator i$ = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, keys)).entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Object> e = (Entry)i$.next();
         map.put(((String)e.getKey()).substring(keySpace.getName().length() + 1), e.getValue());
      }

      return map;
   }

   public static boolean deletePaged(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      Integer numPages = getInt(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGES"));
      if (null == numPages) {
         return false;
      } else {
         boolean b = true;

         for(Integer i = 0; i < numPages; i = i + 1) {
            b &= delete(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGE", i.toString()));
         }

         return b & delete(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGES"));
      }
   }

   public static Object getPaged(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key) {
      Integer numPages = getInt(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGES"));
      if (null == numPages) {
         return null;
      } else {
         List<String> keys = new ArrayList();

         for(Integer i = 0; i < numPages; i = i + 1) {
            keys.add(MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGE", i.toString()));
         }

         Map<String, Object> multiMap = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, (String[])keys.toArray(new String[0])));
         String firstKey = MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key, "PAGE", "0");
         Object first = multiMap.get(firstKey);
         if (null == first) {
            return null;
         } else {
            multiMap.remove(firstKey);

            for(Integer i = 1; i < numPages; i = i + 1) {
               Object page = multiMap.get(MemCachedKeyUtils.getFullKeyFromStrings(keySpace.getName(), key, "PAGE", i.toString()));
               if (null == page) {
                  return null;
               }

               try {
                  if (first instanceof Collection) {
                     ((Collection)first).addAll((Collection)page);
                  } else {
                     if (!(first instanceof Map)) {
                        throw new ClassCastException();
                     }

                     ((Map)first).putAll((Map)page);
                  }
               } catch (Exception var10) {
                  log.error("unable to retrive paged list", var10);
                  return null;
               }
            }

            return first;
         }
      }
   }

   public static boolean setPaged(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object items) {
      return setPaged(keySpace, key, items, (Integer)null);
   }

   public static boolean setPaged(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Object items, Integer pageSize) {
      boolean b = true;
      if (null == items) {
         return false;
      } else {
         Integer finalPageSize = pageSize == null ? keySpace.getPageSize() : pageSize;
         if (null == finalPageSize) {
            throw new IllegalArgumentException("Pagesize for " + keySpace + " cannot be null");
         } else {
            boolean isCollection = false;
            int pages;
            int numItems;
            Iterator iter;
            if (items instanceof Collection) {
               numItems = ((Collection)items).size();
               pages = numItems / finalPageSize + 1;
               iter = ((Collection)items).iterator();
               isCollection = true;
            } else {
               if (!(items instanceof Map)) {
                  throw new IllegalArgumentException("Only Collections & Maps can be paged - you passed in " + items.getClass());
               }

               numItems = ((Map)items).size();
               pages = numItems / finalPageSize + 1;
               iter = ((Map)items).entrySet().iterator();
            }

            int idx = 0;

            for(Integer i = 0; i < pages; i = i + 1) {
               int upperBound = i * finalPageSize + finalPageSize;
               upperBound = upperBound > numItems ? numItems : upperBound;

               try {
                  Object sub;
                  for(sub = items.getClass().newInstance(); idx < upperBound; ++idx) {
                     if (isCollection) {
                        ((Collection)sub).add(iter.next());
                     } else {
                        Entry e = (Entry)iter.next();
                        ((Map)sub).put(e.getKey(), e.getValue());
                     }
                  }

                  b &= set(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGE", i.toString()), sub);
               } catch (Exception var15) {
                  return false;
               }
            }

            return b ? set(keySpace, MemCachedKeyUtils.getFullKeyFromStrings(key, "PAGES"), pages) : b;
         }
      }
   }

   public static Map<String, String> getMultiString(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      Map<String, String> map = new HashMap();
      Iterator i$ = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, keys)).entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Object> e = (Entry)i$.next();
         map.put(((String)e.getKey()).substring(keySpace.getName().length() + 1), toString(e.getValue()));
      }

      return map;
   }

   public static Map<String, Integer> getMultiInt(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      Map<String, Integer> map = new HashMap();
      Iterator i$ = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, keys)).entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Object> e = (Entry)i$.next();
         map.put(((String)e.getKey()).substring(keySpace.getName().length() + 1), toInt(e.getValue()));
      }

      return map;
   }

   public static Map<String, Long> getMultiLong(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      Map<String, Long> map = new HashMap();
      Iterator i$ = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, keys)).entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Object> e = (Entry)i$.next();
         map.put(((String)e.getKey()).substring(keySpace.getName().length() + 1), toLong(e.getValue()));
      }

      return map;
   }

   public static Map<String, Double> getMultiDouble(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String[] keys) {
      Map<String, Double> map = new HashMap();
      Iterator i$ = ((MemCachedClient)clientMap.get(keySpace.getMemCachedInstance())).getMulti(MemCachedKeyUtils.getFullKeysForKeySpace(keySpace, keys)).entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Object> e = (Entry)i$.next();
         map.put(((String)e.getKey()).substring(keySpace.getName().length() + 1), toDouble(e.getValue()));
      }

      return map;
   }

   private static String toString(Object obj) {
      return obj == null ? null : obj.toString();
   }

   private static Integer toInt(Object obj) {
      if (obj != null && !(obj instanceof Integer)) {
         try {
            return Integer.valueOf(obj.toString());
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return (Integer)obj;
      }
   }

   private static Long toLong(Object obj) {
      if (obj != null && !(obj instanceof Long)) {
         try {
            return Long.valueOf(obj.toString());
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return (Long)obj;
      }
   }

   private static Double toDouble(Object obj) {
      if (obj != null && !(obj instanceof Double)) {
         try {
            return Double.valueOf(obj.toString());
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return (Double)obj;
      }
   }

   static {
      MemCachedUtils.Instance[] arr$ = MemCachedUtils.Instance.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MemCachedUtils.Instance instance = arr$[i$];
         log.info("Setting up MemCachedClientWrapper for instance: " + instance);
         clientMap.put(instance, MemCachedUtils.getMemCachedClient(instance));
      }

   }

   public interface InitialCounterValueProvider<TContext> {
      MemCachedKeySpaces.MemCachedKeySpaceInterface getInitialValueLockKeySpace(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, TContext var3);

      String getInitialValueLockKeyName(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, TContext var3);

      long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, TContext var3);

      long getInitialValueLockWaitTime(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, TContext var3);

      long getInitialValueTimeToLive(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, TContext var3);
   }
}
