package net.spy.memcached;

public class SockIOPool {
    private static SockIOPool instance;

    private String[] servers;
    private int initConn = 3;
    private int minConn = 3;
    private int maxConn = 10;
    private long maxIdle = 1000L * 60 * 5;
    private long maintSleep = 30000L;
    private boolean nagle = false;
    private int socketTO = 3000;
    private int socketConnectTO = 3000;

    private SockIOPool() {
    }

    public static SockIOPool getInstance() {
        if (instance == null) {
            instance = new SockIOPool();
        }
        return instance;
    }

    public static SockIOPool getInstance(String poolName) {
        return getInstance();
    }

    public void setServers(String[] servers) {
        this.servers = servers;
    }

    public String[] getServers() {
        return servers;
    }

    public void setInitConn(int initConn) {
        this.initConn = initConn;
    }

    public void setMinConn(int minConn) {
        this.minConn = minConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public void setMaxIdle(long maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaintSleep(long maintSleep) {
        this.maintSleep = maintSleep;
    }

    public void setNagle(boolean nagle) {
        this.nagle = nagle;
    }

    public void setSocketTO(int socketTO) {
        this.socketTO = socketTO;
    }

    public void setSocketConnectTO(int socketConnectTO) {
        this.socketConnectTO = socketConnectTO;
    }

    public void initialize() {
    }

    public void shutDown() {
    }

    public boolean isInitialized() {
        return false;
    }
}
