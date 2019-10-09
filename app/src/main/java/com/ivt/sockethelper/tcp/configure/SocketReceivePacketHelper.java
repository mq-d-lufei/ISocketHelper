package com.ivt.sockethelper.tcp.configure;

import java.util.Arrays;

/**
 * Created by feaoes on 2017/9/18.
 */

public class SocketReceivePacketHelper {
    /**
     * 读取策略
     */
    private int readStrategy;
    /**
     * 响应包头
     */
    private byte[] receiveHeaderData;

    public byte[] getReceiveHeaderData() {
        return receiveHeaderData;
    }

    public SocketReceivePacketHelper setReceiveHeaderData(byte[] receiveHeaderData) {
        if (null != receiveHeaderData) {
            this.receiveHeaderData = Arrays.copyOf(receiveHeaderData, receiveHeaderData.length);
        } else {
            this.receiveHeaderData = null;
        }
        return this;
    }

    /**
     * 响应包尾
     */
    private byte[] receiveTrailerData;

    public SocketReceivePacketHelper setReceiveTrailerData(byte[] receiveTrailerData) {
        if (receiveTrailerData != null) {
            this.receiveTrailerData = Arrays.copyOf(receiveTrailerData, receiveTrailerData.length);
        } else {
            this.receiveTrailerData = null;
        }
        return this;
    }

    public byte[] getReceiveTrailerData() {
        return this.receiveTrailerData;
    }

    /**
     * 分段接收消息，每段长度，仅在按长度读取时有效
     * 若设置大于0时，receiveSegmentEnabled自动变更为true，反之亦然
     * 设置后可手动变更receiveSegmentEnabled
     */
    private int receiveSegmentLength;

    public int getReceiveSegmentLength() {
        return receiveSegmentLength;
    }

    public SocketReceivePacketHelper setReceiveSegmentLength(int receiveSegmentLength) {
        this.receiveSegmentLength = receiveSegmentLength;
        return this;
    }

    /**
     * 是否分段接收数据，若receiveSegmentLength不大于0，返回false
     */
    private boolean receiveSegmentEnabled;

    public SocketReceivePacketHelper setReceiveSegmentEnabled(boolean receiveSegmentEnabled) {
        this.receiveSegmentEnabled = receiveSegmentEnabled;
        return this;
    }

    public boolean isReceiveSegmentEnabled() {
        if (getReceiveSegmentLength() <= 0) {
            return false;
        }
        return this.receiveSegmentEnabled;
    }

    /**
     * 读取超时时长，超过时长没有读取到任何消息自动断开连接
     */
    private long receiveTimeout;

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * 是否开启接收超时
     */
    private boolean receiveTimeoutEnabled;

    public boolean isReceiveTimeoutEnabled() {
        return receiveTimeoutEnabled;
    }

    public SocketReceivePacketHelper setReceiveTimeoutEnabled(boolean receiveTimeoutEnabled) {
        this.receiveTimeoutEnabled = receiveTimeoutEnabled;
        return this;
    }
}
