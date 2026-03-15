package com.mig33.rabbitmqclient.settings;
public abstract class Settings {
    public abstract String getHost();
    public abstract int getPort();
    public abstract String getVirtualHost();
    public abstract String getUsername();
    public abstract String getPassword();
    public abstract int getConnectTimeoutMillis();
    public abstract int getRequestedHeartBeatSecs();
    public abstract int getMaxPendingAsyncCloseConnTask();
    public abstract int getWaitChannelReturnTimeoutMillis();
}
