package com.ivt.sockethelper.tcp.manager;

import android.util.Log;

import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeader;
import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeaderConst;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feaoes on 2017/11/10.
 */

public class PacketManagerImpl implements PacketManager {

    private String TAG = "SocketSendPacketImpl";
    //登录包
    private EmergencyMessageHeader.EmergencyMessageHeaderFormat loginFormat;
    private EmergencyMessageHeader loginPacket;
    //心跳包
    private EmergencyMessageHeader.EmergencyMessageHeaderFormat heartBeatFormat;
    private EmergencyMessageHeader heartBeatHeader;
    //xml数据包
    private EmergencyMessageHeader.EmergencyMessageHeaderFormat dataFormat;
    private EmergencyMessageHeader dataHeader;

    //原子性Integer
    private static final AtomicInteger IDAtomic = new AtomicInteger();

    private PacketManagerImpl() {

        loginFormat = EmergencyMessageHeader.EmergencyMessageHeaderFormat.makeFormat(EmergencyMessageHeaderConst.HEADER_USER_LOGIN);
        loginPacket = new EmergencyMessageHeader(loginFormat);

        heartBeatFormat = EmergencyMessageHeader.EmergencyMessageHeaderFormat.makeFormat(EmergencyMessageHeaderConst.HEADER_SEND_HEARTBEAT);
        heartBeatHeader = new EmergencyMessageHeader(heartBeatFormat);

        dataFormat = EmergencyMessageHeader.EmergencyMessageHeaderFormat.makeFormat(EmergencyMessageHeaderConst.HEADER_SEND_DATA);
        dataHeader = new EmergencyMessageHeader(dataFormat);
    }

    private static final class HolderClass {
        private static final PacketManagerImpl instance = new PacketManagerImpl();
    }

    public static PacketManagerImpl getInstance() {
        return HolderClass.instance;
    }


    /**
     * 获取XML数据包
     *
     * @param data xml数据
     */
    @Override
    public byte[] getXMLDataPacketBytes(byte[] data, String doctorId) {
        try {

            setPublicPacketFields(dataHeader, PACKET_TYPE_DATA, doctorId, null);
            dataHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID, IDAtomic.getAndIncrement());
            dataHeader.packField(EmergencyMessageHeaderConst.FIELD_XML_DATA_LENGTH, data.length);
            dataHeader.packField(EmergencyMessageHeaderConst.FIELD_XML_DATA, data);
            dataHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH, dataHeader.getLength() - 2);
            return dataHeader.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 登录包
     *
     * @param userName
     * @param passwordMD5
     * @param deviceUuid       设备ID
     * @param isHeartBearLogin 是否是心跳登录类型
     * @return
     */
    @Override
    public byte[] getLoginPacketBytes(String userName, String passwordMD5, String deviceUuid, String doctorId, boolean isHeartBearLogin) {
        try {
            int packetType = PACKET_TYPE_HEART_LOGIN;

            if (!isHeartBearLogin) {
                packetType = PACKET_TYPE_DATA_LOGIN;
            }

            setPublicPacketFields(loginPacket, packetType, doctorId, deviceUuid);

            int packetId = IDAtomic.getAndIncrement() + 1;
            Log.e(TAG, "getLoginPacketBytes(),packetId: " + packetId + " --packetType：" + packetType);
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID, packetId);
            byte[] tempUserName = userName.getBytes("UTF-8");
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_USERNAME_LENGTH, tempUserName.length);
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_USERNAME, tempUserName);
            byte[] tempPassword = passwordMD5.getBytes("UTF-8");
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_PASSWORD_MD5_DIGEST, tempPassword);
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_PASSWORD_MD5_DIGEST_LENGTH, tempPassword.length);
            loginPacket.packField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH, loginPacket.getLength() - 2);
            return loginPacket.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "error - getLoginPacketBytes: " + e.toString());
        }
        return null;
    }

    /**
     * 心跳包
     *
     * @return
     */
    @Override
    public byte[] getHeartBeatPacketBytes(String doctorId) {
        try {
            setPublicPacketFields(heartBeatHeader, PACKET_TYPE_HEARTBEAT, doctorId, null);
            heartBeatHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID, IDAtomic.getAndIncrement());
            heartBeatHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH, heartBeatHeader.getLength() - 2);
            byte[] heartBeatBytes = heartBeatHeader.getBytes();
            Log.e(TAG, "心跳包:len = " + (heartBeatHeader.getLength() - 2) + "bytes = " + heartBeatBytes.length
                    + "mHBPacketID = " + IDAtomic.toString());
            return heartBeatBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置公共包成员
     */
    @Override
    public void setPublicPacketFields(EmergencyMessageHeader packet, int packetType, String doctorId, String deviceUuid) {
        try {
            packet.clear();
            packet.packField(EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 0x7AA7);
            packet.packField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH, 0);
            packet.packField(EmergencyMessageHeaderConst.FIELD_VERSION, 0x01);
            packet.packField(EmergencyMessageHeaderConst.FIELD_UNUSED, 0x0000);
            packet.packField(EmergencyMessageHeaderConst.FIELD_PACKET_TYPE, packetType);
            packet.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID, 0);
            if (deviceUuid != null) {
                byte[] tempDeviceUuid = deviceUuid.getBytes("UTF-8");
                loginPacket.packField(EmergencyMessageHeaderConst.FIELD_DEVICETOKEN_LENGTH, tempDeviceUuid.length);
                loginPacket.packField(EmergencyMessageHeaderConst.FIELD_DEVICETOKEN, tempDeviceUuid);
            }

            byte[] tempDoctorIdBytes = doctorId.getBytes("UTF-8");
            packet.packField(EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH, tempDoctorIdBytes.length);
            packet.packField(EmergencyMessageHeaderConst.FIELD_DOCID, tempDoctorIdBytes);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("catch", "e.toString: " + e.toString());
        }
    }
}
