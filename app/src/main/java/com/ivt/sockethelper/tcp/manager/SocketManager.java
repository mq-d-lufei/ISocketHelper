package com.ivt.sockethelper.tcp.manager;

import android.util.Log;

import com.ivt.sockethelper.MyApplication;
import com.ivt.sockethelper.entity.NetConfig;
import com.ivt.sockethelper.entity.User;
import com.ivt.sockethelper.tcp.SocketClient;
import com.ivt.sockethelper.tcp.configure.SocketConfigure;
import com.ivt.sockethelper.tcp.configure.SocketHeartBeatHelper;
import com.ivt.sockethelper.tcp.configure.SocketHelper;
import com.ivt.sockethelper.tcp.configure.SocketReceivePacketHelper;
import com.ivt.sockethelper.tcp.configure.SocketSendPacketHelper;
import com.ivt.sockethelper.tcp.pocket.CheckPacket;
import com.ivt.sockethelper.tcp.pocket.SocketSendPacket;
import com.ivt.sockethelper.utils.DeviceUuidUtil;

/**
 * Created by feaoes on 2017/11/15.
 */

public class SocketManager {

    private SocketManager() {
    }


    private static class HolderClass {
        private static final SocketManager instance = new SocketManager();
    }

    public static SocketManager getInstance() {
        return HolderClass.instance;
    }



    User localUser;

    public User getLocalUser() {


        if (null == localUser) {
            localUser = new User();
            localUser.setDocId("3017");
            localUser.setUsername("mrf");
            localUser.setPassword("000000");
            localUser.setMd5Password("E9D5C2CA1DE40BE12E66CC579D11B9FF");
            //Lenovo
            //localUser.setDeviceUUID("ba9f2428-e1e2-3ec5-8918-8f1d1becde72");
            localUser.setDeviceUUID("fc8cb791-2ec7-3a28-93a4-53f7695138b9");
        }

        return localUser;
    }

    public User getRemoteUser() {

        User remoteUser = new User();
        remoteUser.setDocId("6974");
        remoteUser.setUsername("szmrf");
        remoteUser.setPassword("000000");
        remoteUser.setMd5Password("E9D5C2CA1DE40BE12E66CC579D11B9FF");
        remoteUser.setDeviceUUID("a66b0e2d-4a7e-34b6-9ada-2264ab3d53e3");

        return remoteUser;
    }

    public String getDeviceUUID() {
        String deviceUuid = new DeviceUuidUtil(MyApplication.getInstance()).getDeviceUuid();
        Log.e("deviceUuid", "deviceUuid: " + deviceUuid);
        return deviceUuid;
    }

    /**
     * 登录包
     */
    public SocketSendPacket getSocketLoginPacket(boolean isHeartBeatLogin) {

        String username = getLocalUser().getUsername();
        String md5Password = getLocalUser().getMd5Password();
        String uuID = getLocalUser().getDeviceUUID();
        //String uuID = getDeviceUUID();
        String doctorId = getLocalUser().getDocId();
        getDeviceUUID();
        //
        byte[] loginPacketBytes = PacketManagerImpl.getInstance().getLoginPacketBytes(username, md5Password, uuID, doctorId, isHeartBeatLogin);
        return new SocketSendPacket().setSendPacket(loginPacketBytes);
    }

    /**
     * 心跳包
     */
    public SocketSendPacket getSocketHeartBeatPacket() {
        byte[] heartBeatPacketBytes = PacketManagerImpl.getInstance().getHeartBeatPacketBytes(getLocalUser().getDocId());
        return new SocketSendPacket().setSendPacket(heartBeatPacketBytes);
    }

    /**
     * 数据包
     */
    public SocketSendPacket getSocketXMLDataPacket() {
        byte[] data = new byte[]{12, 34, 56, 78, 90};
        byte[] xmlDataPacketBytes = PacketManagerImpl.getInstance().getXMLDataPacketBytes(data, getLocalUser().getDocId());
        return new SocketSendPacket().setSendPacket(xmlDataPacketBytes);
    }

    /**
     * 检测包
     */
    public SocketSendPacket getSocketCheckPacket() {
        byte[] checkPacket = new CheckPacket().getCheckPacket(getLocalUser().getDocId());
        return new SocketSendPacket().setSendPacket(checkPacket);
    }

    public SocketClient getSocketClient(SocketConfigure socketConfigure) {
        if (null == socketConfigure)
            throw new IllegalStateException("getSocketClient(): socketConfigure not null");
        SocketConfigure.SocketType socketType = socketConfigure.getSocketType();
        if (socketType != SocketConfigure.SocketType.TYPE_DATA && socketType != SocketConfigure.SocketType.TYPE_HEART_BEAT) {
            throw new IllegalStateException("socketType error");
        }
        return new SocketClient(socketConfigure);
    }


    public SocketConfigure builderSocketConfig(SocketConfigure.SocketType socketType) {
        //包头数据
        byte[] packetHeader = new byte[]{-89, 122};
        //helper
        SocketHelper socketHelper = new SocketHelper(NetConfig.localIP, NetConfig.localPORT);
        //send
        SocketSendPacketHelper socketSendPacketHelper = new SocketSendPacketHelper();
        socketSendPacketHelper.setHeaderData(packetHeader)
                .setSendSegmentEnabled(true)
                .setSendSegmentLength(512);
        //receive
        SocketReceivePacketHelper socketReceivePacketHelper = new SocketReceivePacketHelper();
        //byte[] bytes = ByteUtil.intToBytesLow(0xA77A);
        socketReceivePacketHelper.setReceiveHeaderData(packetHeader)
                .setReceiveSegmentEnabled(true)
                .setReceiveSegmentLength(512);
        //heartBeat
        SocketHeartBeatHelper socketHeartBeatHelper = new SocketHeartBeatHelper();
        socketHeartBeatHelper.setHeartBeatInterval(30).setSendHeartBeatEnabled(true);
        //config
        return new SocketConfigure(socketType)
                .setSocketHelper(socketHelper)
                .setSocketSendPacketHelper(socketSendPacketHelper)
                .setSocketReceivePacketHelper(socketReceivePacketHelper)
                .setSocketHeartBeatHelper(socketHeartBeatHelper);

    }


}
