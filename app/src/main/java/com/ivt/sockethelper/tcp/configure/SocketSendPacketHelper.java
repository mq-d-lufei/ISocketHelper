package com.ivt.sockethelper.tcp.configure;

import java.util.Arrays;

/**
 * 发送包配置
 */

public class SocketSendPacketHelper {
    /**
     * 包头，0xA77A，4字节，低字节在前，高字节在后
     */
    private byte[] sendHeaderData;
    /**
     * 发送消息时自动添加的包尾
     */
    private byte[] sendTrailerData;

    /**
     * 发送消息时分段发送的每段大小,每次发送多少个字节
     * 分段发送可以回调进度
     * 此数值表示每次发送byte的长度
     * 不大于0表示不分段
     */
    private int sendSegmentLength;
    /**
     * 是否分段发送
     */
    private boolean isSendSegmentEnabled;
    /**
     * 发送超时时长，超过时长内无法写出，则自动断开连接
     * 仅在每个发送包开始发送时计时，结束后重置计时
     */
    private long sendTimeout;
    /**
     * 是否开启发送超时
     */
    private boolean isSendTimeoutEnabled;

    /**
     * 检测包
     */
    private byte[] sendCheckPacketData;

    public byte[] getSendCheckPacketData() {
        return sendCheckPacketData;
    }

    public void setSendCheckPacketData(byte[] sendCheckPacketData) {
        this.sendCheckPacketData = sendCheckPacketData;
    }

    /**
     * Get
     */


    public byte[] getHeaderData() {
        return sendHeaderData;
    }

    public byte[] getSendTrailerData() {
        return sendTrailerData;
    }

    public int getSendSegmentLength() {
        return sendSegmentLength;
    }

    public boolean isSendSegmentEnabled() {
        if (getSendSegmentLength() <= 0) {
            return false;
        }
        return this.isSendSegmentEnabled;
    }


    public long getSendTimeout() {
        return sendTimeout;
    }

    public boolean isSendTimeoutEnabled() {
        return isSendTimeoutEnabled;
    }

    /**
     * Set
     */
    public SocketSendPacketHelper setHeaderData(byte[] sendHeaderData) {
        if (null != sendHeaderData) {
            this.sendHeaderData = Arrays.copyOf(sendHeaderData, sendHeaderData.length);
        } else {
            this.sendHeaderData = null;
        }
        return this;
    }

    public SocketSendPacketHelper setSendTrailerData(byte[] sendTrailerData) {
        if (sendTrailerData != null) {
            this.sendTrailerData = Arrays.copyOf(sendTrailerData, sendTrailerData.length);
        } else {
            this.sendTrailerData = null;
        }
        return this;
    }

    public SocketSendPacketHelper setSendSegmentLength(int sendSegmentLength) {
        this.sendSegmentLength = sendSegmentLength;
        return this;
    }

    public SocketSendPacketHelper setSendSegmentEnabled(boolean sendSegmentEnabled) {
        isSendSegmentEnabled = sendSegmentEnabled;
        return this;
    }

    public SocketSendPacketHelper setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
        return this;
    }

    public SocketSendPacketHelper setSendTimeoutEnabled(boolean sendTimeoutEnabled) {
        isSendTimeoutEnabled = sendTimeoutEnabled;
        return this;
    }
}
