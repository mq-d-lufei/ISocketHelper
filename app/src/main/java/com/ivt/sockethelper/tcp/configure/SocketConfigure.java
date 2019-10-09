package com.ivt.sockethelper.tcp.configure;

/**
 * Socket配置类
 */

public class SocketConfigure {

    private SocketType socketType;
    private SocketHelper socketHelper;
    private SocketSendPacketHelper socketSendPacketHelper;
    private SocketReceivePacketHelper socketReceivePacketHelper;
    private SocketHeartBeatHelper socketHeartBeatHelper;

    public SocketConfigure(SocketType socketType) {
        this.socketType = socketType;
    }

    /**
     * 双链路Socket
     * <p>
     * TYPE_DATA    数据链路
     * TYPE_HEART_BEAT  心跳链路
     */
    public enum SocketType {
        TYPE_DATA, TYPE_HEART_BEAT;
    }

    public SocketType getSocketType() {
        if (null == socketType)
            return SocketType.TYPE_HEART_BEAT;
        return socketType;
    }

    public SocketHelper getSocketHelper() {
        return socketHelper;
    }

    public SocketConfigure setSocketHelper(SocketHelper socketHelper) {
        this.socketHelper = socketHelper;
        return this;
    }

    public SocketSendPacketHelper getSocketSendPacketHelper() {
        return socketSendPacketHelper;
    }

    public SocketConfigure setSocketSendPacketHelper(SocketSendPacketHelper socketSendPacketHelper) {
        this.socketSendPacketHelper = socketSendPacketHelper;
        return this;
    }

    public SocketReceivePacketHelper getSocketReceivePacketHelper() {
        return socketReceivePacketHelper;
    }

    public SocketConfigure setSocketReceivePacketHelper(SocketReceivePacketHelper socketReceivePacketHelper) {
        this.socketReceivePacketHelper = socketReceivePacketHelper;
        return this;
    }

    public SocketHeartBeatHelper getSocketHeartBeatHelper() {
        return socketHeartBeatHelper;
    }

    public SocketConfigure setSocketHeartBeatHelper(SocketHeartBeatHelper socketHeartBeatHelper) {
        this.socketHeartBeatHelper = socketHeartBeatHelper;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketConfigure that = (SocketConfigure) o;

        return socketType == that.socketType;
    }

    @Override
    public int hashCode() {
        return socketType.hashCode();
    }
}
