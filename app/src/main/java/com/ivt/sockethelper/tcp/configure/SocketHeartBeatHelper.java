package com.ivt.sockethelper.tcp.configure;

/**
 * Created by feaoes on 2017/9/18.
 */

public class SocketHeartBeatHelper {
    /**
     * 心跳间隔时间
     */
    private long heartBeatInterval;
    /**
     * 是否发送心跳包
     */
    private boolean isSendHeartBeatEnabled;

    public boolean isSendHeartBeatEnabled() {
        return isSendHeartBeatEnabled;
    }

    public SocketHeartBeatHelper setSendHeartBeatEnabled(boolean sendHeartBeatEnabled) {
        isSendHeartBeatEnabled = sendHeartBeatEnabled;
        return this;
    }

    public long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public SocketHeartBeatHelper setHeartBeatInterval(long heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
        return this;
    }
}
