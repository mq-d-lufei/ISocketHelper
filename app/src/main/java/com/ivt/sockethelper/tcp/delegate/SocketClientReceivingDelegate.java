package com.ivt.sockethelper.tcp.delegate;


import com.ivt.sockethelper.tcp.SocketClient;
import com.ivt.sockethelper.tcp.pocket.SocketResponsePacket;

/**
 * Socket接收数据是的相关操作抽象
 */

public interface SocketClientReceivingDelegate {
    /**
     * 开始接收响应包
     *
     * @param client
     * @param packet
     */
    void onReceivePacketBegin(SocketClient client, SocketResponsePacket packet);

    /**
     * 接收完成
     *
     * @param client
     * @param packet
     */
    void onReceivePacketEnd(SocketClient client, SocketResponsePacket packet);

    /**
     * 取消接收
     *
     * @param client
     * @param packet
     */
    void onReceivePacketCancel(SocketClient client, SocketResponsePacket packet);

    /**
     * 接收进度回调
     *
     * @param client         Socket管理类
     * @param packet         响应数据包
     * @param progress       进度
     * @param receivedLength 已接收的字节数
     */
    void onReceivingPacketInProgress(SocketClient client, SocketResponsePacket packet, float progress, int receivedLength);


}
