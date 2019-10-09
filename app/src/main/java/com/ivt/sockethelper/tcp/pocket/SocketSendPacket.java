package com.ivt.sockethelper.tcp.pocket;

/**
 * Created by feaoes on 2017/11/15.
 */

public class SocketSendPacket {


    byte[] sendPacket;

    public SocketSendPacket() {
    }

    public SocketSendPacket(byte[] sendPacket) {
        this.sendPacket = sendPacket;
    }


    public byte[] getSendPacket() {
        return sendPacket;
    }

    public SocketSendPacket setSendPacket(byte[] sendPacket) {
        this.sendPacket = sendPacket;
        return this;
    }
}
