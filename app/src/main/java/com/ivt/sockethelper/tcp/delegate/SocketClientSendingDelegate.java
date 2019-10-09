package com.ivt.sockethelper.tcp.delegate;


import com.ivt.sockethelper.tcp.SocketClient;
import com.ivt.sockethelper.tcp.pocket.SocketSendPacket;

/**
 * Socket发送数据时的相关操作抽象
 */

public interface SocketClientSendingDelegate {
    /**
     * 开始发送
     */
    void onSendPacketBegin(SocketClient client, SocketSendPacket packet);

    /**
     * 发送完成
     */
    void onSendPacketEnd(SocketClient client, SocketSendPacket packet);

    /**
     * 取消发送
     */
    void onSendPacketCancel(SocketClient client, SocketSendPacket packet, String reason);

    /**
     * 发送进度回调
     *
     * @param client       Socket管理类
     * @param packet       数据包
     * @param progress     进度（0.0f - 1.0f）
     * @param sendedLength 已发送的字节数
     */
    void onSendingPacketInProgress(SocketClient client, SocketSendPacket packet, float progress, int sendedLength);
}
