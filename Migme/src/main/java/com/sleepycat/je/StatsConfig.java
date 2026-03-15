package com.sleepycat.je;
public class StatsConfig {
    public static final StatsConfig DEFAULT = new StatsConfig();
    public StatsConfig setClear(boolean clear) { return this; }
    public StatsConfig setFast(boolean fast) { return this; }
}
