package com.ivt.sockethelper.tcp.delegate;

import android.support.annotation.NonNull;

import com.ivt.sockethelper.tcp.SocketClient;
import com.ivt.sockethelper.tcp.pocket.SocketResponsePacket;


/**
 * Socekt 连接状态管理代理类
 */

public interface SocketClientDelegate {
    /**
     * Socket已连接
     */
    void onConnected(SocketClient client);

    /**
     * Socket未连接
     */
    void onDisconnected(SocketClient client);

    /**
     * Socket处理数据
     *
     * @param client         Socket管理类
     * @param responsePacket 响应包
     */
    void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket);

}
