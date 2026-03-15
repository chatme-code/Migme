/**
 * Copyright (c) 2013 Project Goth
 *
 * MathUtils.java
 * Created Feb 4, 2014, 4:24:47 PM
 */

package com.projectgoth.util;

/**
 * Static class that contains Math related utilitiy routines.
 * 
 * @author angelorohit
 * 
 */
public class MathUtils {

    private static float EPSILON = 0.0000001f;

    /**
     * Private constructor
     */
    private MathUtils() {
    }

    /**
     * Calculates a power-of-two scale for the source dimensions to expand to
     * the destination dimensions.
     * 
     * @param originalWidth
     *            The width of the source dimension to be scaled
     * @param originalHeight
     *            The height of the source dimension to be scaled
     * @param expectedWidth
     *            The width of the destination dimension to be scaled to.
     * @param expectedHeight
     *            The height of the destination dimension to be scaled to.
     * @return An integer that is the power-of-two scale.
     */
    public static int getPowerOfTwoScale(int originalWidth, int originalHeight, int expectedWidth, int expectedHeight) {
        double expectedSize = Math.max(expectedWidth, expectedHeight);
        double originalSize = Math.min(originalWidth, originalHeight);

        // this is to get power of 2
        int scale = 1;
        if (expectedSize > 0 && originalSize > 0 && originalSize > expectedSize) {
            // do log2(originalSize/ expectedSize)
            double logSize = Math.log(originalSize / expectedSize);
            double exponent = logSize / Math.log(2);
            // rounded down to the nearest power of 2.
            int exponentInt = (int) exponent;
            double pow = Math.pow(2, exponentInt);
            scale = (int) pow;
        }

        return scale;
    }

    /**
     * Compares two floating point values
     * 
     * @param lhs
     *            One of the floating point values to be compared
     * @param rhs
     *            The other floating point value to be compared.
     * @return true if equal and false otherwise.
     */
    public static boolean compareFloats(final float lhs, final float rhs) {
        return (Math.abs(lhs - rhs) < EPSILON);
    }
    
    /**
     * Ensures that a given value is within a max and min range.
     * If the value is less than the minimum, then it is made the min value.
     * If the value is greater than the maximum, then it is made the max value.
     * Otherwise, the original value is returned.
     * @param value The value to be limited within range.
     * @param min   The minimum value below which the given value cannot go.
     * @param max   The maximum value above which the give value cannot go.
     * @return      The range limited value.
     */
    public static int clamp(final int value, final int min, final int max) {
        return Math.max(Math.min(value, max), min);
     }
    public static float clamp(final float value, final float min, final float max) {
        return Math.max(Math.min(value, max), min);
     }
    
    /**
     * Linear interpolation for integers
     * @param a start value
     * @param b end value
     * @param t alpha value (or elapsed time)
     * @return linearly interpolated value between a and b (for t [0..1])
     */
    public static int lerp(int a, int b, float t) {
        return a + (int)((b - a) * t);
    }

    /**
     * Linear interpolation for floats
     * @param a start value
     * @param b end value
     * @param t alpha value (or elapsed time)
     * @return linearly interpolated value between a and b (for t [0..1])
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
