package com.projectgoth.fusion.common;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class HashObjectUtils {
   public static Hashtable dataObjectToHashtable(Object dataObject) {
      Hashtable hashData = new Hashtable();
      if (dataObject == null) {
         return hashData;
      } else {
         try {
            Class c = dataObject.getClass();
            Field[] fields = c.getFields();

            for(int i = 0; i < fields.length; ++i) {
               Object value = fields[i].get(dataObject);
               if (value != null) {
                  if (value instanceof String) {
                     hashData.put(fields[i].getName(), String.valueOf(value));
                  } else if (value instanceof Integer) {
                     hashData.put(fields[i].getName(), String.valueOf(value));
                  } else if (value instanceof Long) {
                     hashData.put(fields[i].getName(), String.valueOf(value));
                  } else if (value instanceof Double) {
                     hashData.put(fields[i].getName(), String.valueOf(value));
                  } else if (value instanceof Enum) {
                     hashData.put(fields[i].getName(), String.valueOf(value));
                  } else if (value instanceof Date) {
                     Date date = (Date)value;
                     hashData.put(fields[i].getName(), String.valueOf(date.getTime() / 1000L));
                  } else if (value instanceof Boolean) {
                     Boolean bool = (Boolean)value;
                     String rval = "0";
                     if (bool) {
                        rval = "1";
                     }

                     hashData.put(fields[i].getName(), rval);
                  } else {
                     int z;
                     if (value instanceof String[]) {
                        Vector v = new Vector();
                        String[] arr$ = (String[])((String[])value);
                        z = arr$.length;

                        for(int i$ = 0; i$ < z; ++i$) {
                           String s = arr$[i$];
                           v.add(s);
                        }

                        hashData.put(fields[i].getName(), v);
                     } else if (value instanceof Object) {
                        Class internalClass = value.getClass();
                        Field[] internalFields = internalClass.getFields();

                        for(z = 0; z < internalFields.length; ++z) {
                           Object internalValue = internalFields[z].get(value);
                           if (internalValue != null) {
                              if (internalValue instanceof String) {
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                              } else if (internalValue instanceof Integer) {
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                              } else if (internalValue instanceof Long) {
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                              } else if (internalValue instanceof Double) {
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                              } else if (internalValue instanceof Enum) {
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                              } else if (internalValue instanceof Date) {
                                 Date date = (Date)internalValue;
                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(date.getTime() / 1000L));
                              } else if (internalValue instanceof Boolean) {
                                 Boolean bool = (Boolean)internalValue;
                                 String rval = "0";
                                 if (bool) {
                                    rval = "1";
                                 }

                                 hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), rval);
                              } else if (!(value instanceof String[])) {
                                 if (internalValue instanceof Object) {
                                 }
                              } else {
                                 Vector v = new Vector();
                                 String[] arr$ = (String[])((String[])value);
                                 int len$ = arr$.length;

                                 for(int i$ = 0; i$ < len$; ++i$) {
                                    String s = arr$[i$];
                                    v.add(s);
                                 }

                                 hashData.put(fields[i].getName(), v);
                              }
                           }
                        }
                     }
                  }
               }
            }

            return hashData;
         } catch (SecurityException var15) {
         } catch (IllegalArgumentException var16) {
         } catch (IllegalAccessException var17) {
         }

         return null;
      }
   }

   public static Hashtable dataObjectToHashtableWithNulls(Object dataObject) {
      try {
         Hashtable hashData = new Hashtable();
         Class c = dataObject.getClass();
         Field[] fields = c.getFields();

         for(int i = 0; i < fields.length; ++i) {
            Object value = fields[i].get(dataObject);
            if (value instanceof String) {
               hashData.put(fields[i].getName(), String.valueOf(value));
            } else if (value instanceof Integer) {
               hashData.put(fields[i].getName(), String.valueOf(value));
            } else if (value instanceof Long) {
               hashData.put(fields[i].getName(), String.valueOf(value));
            } else if (value instanceof Double) {
               hashData.put(fields[i].getName(), String.valueOf(value));
            } else if (value instanceof Enum) {
               hashData.put(fields[i].getName(), String.valueOf(value));
            } else if (value instanceof Date) {
               Date date = (Date)value;
               hashData.put(fields[i].getName(), String.valueOf(date.getTime() / 1000L));
            } else if (value instanceof Boolean) {
               Boolean bool = (Boolean)value;
               String rval = "0";
               if (bool) {
                  rval = "1";
               }

               hashData.put(fields[i].getName(), rval);
            } else if (value == null) {
               hashData.put(fields[i].getName(), "null");
            } else if (value instanceof Object) {
               Class internalClass = value.getClass();
               Field[] internalFields = internalClass.getFields();

               for(int z = 0; z < internalFields.length; ++z) {
                  Object internalValue = internalFields[z].get(value);
                  if (internalValue instanceof String) {
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                  } else if (internalValue instanceof Integer) {
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                  } else if (internalValue instanceof Long) {
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                  } else if (internalValue instanceof Double) {
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                  } else if (internalValue instanceof Enum) {
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(internalValue));
                  } else if (internalValue instanceof Date) {
                     Date date = (Date)internalValue;
                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), String.valueOf(date.getTime() / 1000L));
                  } else if (internalValue instanceof Boolean) {
                     Boolean bool = (Boolean)internalValue;
                     String rval = "0";
                     if (bool) {
                        rval = "1";
                     }

                     hashData.put(fields[i].getName() + '.' + internalFields[z].getName(), rval);
                  } else if (value == null) {
                     hashData.put(fields[i].getName(), "null");
                  } else if (internalValue instanceof Object) {
                  }
               }
            }
         }

         return hashData;
      } catch (SecurityException var12) {
      } catch (IllegalArgumentException var13) {
      } catch (IllegalAccessException var14) {
      }

      return null;
   }

   public static Object stringArrayToDataObject(String[] keys, String[] values, Object obj) {
      try {
         for(int i = 0; i < keys.length; ++i) {
            try {
               Field field = obj.getClass().getField(keys[i]);
               if (field.getType().toString().equals("class java.lang.String")) {
                  if (values[i] != null) {
                     field.set(obj, values[i]);
                  }
               } else if (field.getType().toString().equals("class java.util.Date")) {
                  if (values[i] != null) {
                     field.set(obj, new Date(Long.valueOf(values[i]) * 1000L));
                  }
               } else if (field.getType().toString().equals("class java.lang.Integer")) {
                  if (values[i] != null) {
                     field.set(obj, Integer.valueOf(values[i]));
                  }
               } else if (field.getType().toString().equals("class java.lang.Double")) {
                  if (values[i] != null) {
                     field.set(obj, Double.valueOf(values[i]));
                  }
               } else if (field.getType().toString().equals("class java.lang.Boolean")) {
                  if (values[i] != null) {
                     if (values[i].equals("1")) {
                        field.set(obj, true);
                     } else {
                        field.set(obj, false);
                     }
                  }
               } else if (field.getType().toString().indexOf("Enum") > 0 && values[i] != null) {
                  field.set(obj, Enum.valueOf(field.getType(), values[i]));
               }
            } catch (NoSuchFieldException var5) {
            }
         }

         return obj;
      } catch (Exception var6) {
         System.out.println(var6.getMessage());
         return null;
      }
   }

   public static Object stringArrayToDataObject(String[] stringArrayData, Object obj) {
      try {
         Field[] fields = obj.getClass().getFields();

         for(int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            if (field.getType().toString().equals("class java.lang.String")) {
               if (stringArrayData[i] != null) {
                  field.set(obj, stringArrayData[i]);
               }
            } else if (field.getType().toString().equals("class java.util.Date")) {
               if (stringArrayData[i] != null) {
                  field.set(obj, new Date(Long.valueOf(stringArrayData[i])));
               }
            } else if (field.getType().toString().equals("class java.lang.Integer")) {
               if (stringArrayData[i] != null) {
                  field.set(obj, Integer.valueOf(stringArrayData[i]));
               }
            } else if (field.getType().toString().equals("class java.lang.Double")) {
               if (stringArrayData[i] != null) {
                  field.set(obj, Double.valueOf(stringArrayData[i]));
               }
            } else if (field.getType().toString().equals("class java.lang.Boolean")) {
               if (stringArrayData[i] != null) {
                  if (stringArrayData[i].equals("1")) {
                     field.set(obj, true);
                  } else {
                     field.set(obj, false);
                  }
               }
            } else if (field.getType().toString().indexOf("Enum") > 0 && stringArrayData[i] != null) {
               field.set(obj, Enum.valueOf(field.getType(), stringArrayData[i]));
            }
         }

         return obj;
      } catch (IllegalAccessException var5) {
         System.out.println(var5.getMessage());
         return null;
      }
   }

   public static void appendPagingMarker(Vector vectorToAdd, int currentPage, int numRows, int numEntries) {
      Hashtable markerHash = new Hashtable();
      markerHash.put("page", currentPage + 1);
      markerHash.put("numEntries", numRows);
      if (numRows == 0) {
         markerHash.put("numPages", 0);
      }

      if (numRows > 0 && numRows / numEntries == 0) {
         markerHash.put("numPages", 1);
      } else {
         double numRowsD = (double)numRows;
         double numEntriesD = (double)numEntries;
         markerHash.put("numPages", (int)Math.ceil(numRowsD / numEntriesD));
      }

      vectorToAdd.add(markerHash);
   }
}
