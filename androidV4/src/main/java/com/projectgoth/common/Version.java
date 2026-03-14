/**
 * Project Goth Version.java Mar 14, 2012 - 3:11:44 PM
 */

package com.projectgoth.common;

/**
 * @author warrenbalcos
 * @date Mar 14, 2012
 */
public class Version {

    private static final String TAG                  = Version.class.getSimpleName();

    /**
     * Client platform
     * 
     * ex. platform = android platform = blackberry
     */
    private static final String platform             = DefaultConfig.PLATFORM;

    /** Useragent format */
    private static final String USER_AGENT_FORMAT_V2 = "mig33/%s/%s.%s.%s.%s";

    private static String       major                = DefaultConfig.VERSION_MAJOR;
    private static String       minor                = DefaultConfig.VERSION_MINOR;
    private static String       patch                = DefaultConfig.BUILD_PATCH;
    private static String       vasTrackingId;

    private static String       debugId;

    public static String getUserAgent() {
        Object[] params = { platform, getMajor(), getMinor(), getPatch(), getVasTrackingId() };
        return String.format(USER_AGENT_FORMAT_V2, params);
    }

    /**
     * Convert version string (major*100 + minor) to number
     * 
     * @return
     */
    public static int getVersionNumber() {
        return (Integer.parseInt(getMajor()) * 100) + Integer.parseInt(getMinor());
    }

    public static String getVersionNumberString() {
        return getMajor() + Constants.DOTSTR + getMinor();
    }

    public static String getVersionNumberWithPatch() {
        return getMajor() + Constants.DOTSTR + getMinor() + Constants.DOTSTR + getPatch();
    }

    public static String getVersionText() {
        String[] params = { getMajor(), getMinor(), getPatch(), getVasTrackingId() };

        StringBuffer versionStr = new StringBuffer();
        int size = params.length;
        for (int i = 0; i < size; i++) {
            versionStr.append(params[i]);
            if (i < size - 1) {
                versionStr.append(Constants.DOTSTR);
            }
        }

        if (Config.isDebug()) {
            versionStr.append(getDebugIdText());
        }
        return versionStr.toString();
    }

    /**
     * @return the major
     */
    private static String getMajor() {
        return major;
    }

    /**
     * @param major
     *            the major to set
     */
    @SuppressWarnings("unused")
    private static void setMajor(String value) {
        if (value != null) {
            major = value;
        }
    }

    /**
     * @return the minor
     */
    private static String getMinor() {
        return minor;
    }

    /**
     * @param minor
     *            the minor to set
     */
    @SuppressWarnings("unused")
    private static void setMinor(String value) {
        if (value != null) {
            minor = value;
        }
    }

    /**
     * @return the patch
     */
    public static String getPatch() {
        return patch;
    }

    /**
     * @param patch
     *            the patch to set
     */
    @SuppressWarnings("unused")
    private static void setPatch(String value) {
        if (value != null) {
            patch = value;
        }
    }

    /**
     * @return the vasTrackingId
     */
    public static String getVasTrackingId() {
        return vasTrackingId;
    }

    /**
     * @param vasTrackingId
     *            the vasTrackingId to set
     */
    private static void setVasTrackingId(String value) {
        if (value != null) {
            vasTrackingId = value;
        }
    }

    /**
     * @return the debugId
     */
    public static String getDebugId() {
        return debugId;
    }

    public static String getDebugIdText() {
        return "(" + debugId + ")";
    }

    /**
     * @param debugId
     *            the debugId to set
     */
    public static void setDebugId(String debugId) {
        Version.debugId = debugId;
    }

    /**
     * initialize version properties
     */
    public static void init(String vasId) {
        Logger.debug.log(TAG, "processing properties...");

        setVasTrackingId(vasId);

        Logger.debug.log(TAG, "major: " + major + " minor: " + minor + " patch: " + patch + " vas: " + vasId);
    }
}
