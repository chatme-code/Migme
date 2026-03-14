/**
 * Copyright (c) 2013 Project Goth
 *
 * MathUtils.java
 * Created Feb 4, 2014, 4:24:47 PM
 */

package com.projectgoth.util;

import java.util.List;

public final class ArrayUtils {


    private ArrayUtils() {
    }

    public static boolean inArray(long[] array, long element) {
        for (long l : array) {
            if (l == element) {
                return true;
            }
        }
        return false;
    }

    public static long[] toLongArray(List<Long> list)  {
        long[] ret = new long[list.size()];
        int i = 0;
        for (Long e : list)
            ret[i++] = e.longValue();
        return ret;
    }

    /**
     * Checks whether an array of type T is null or empty.
     * @param arr   The array to be checked.
     * @return      true if the array is null or empty and false otherwise.
     */
    public static <T> boolean isEmpty(T[] arr) {
        return (arr == null || arr.length == 0);
    }
    
    /**
     * Checks whether a given list parameterized as List<T> contains an object of a given class type.
     * @param list          The {@link List} of type List<T>
     * @param classType     The class type which extends from T.
     * @return              true if an object of the given classType was found in the list.
     */
    public static <T> boolean containsObjectOfType(List<T> list, Class<? extends T> classType) {
        for (final T object : list) {
            if (object.getClass().equals(classType)) {
                return true;
            }
        }
        
        return false;
    }
}
