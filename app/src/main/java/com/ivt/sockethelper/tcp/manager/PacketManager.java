package com.ivt.sockethelper.tcp.manager;

import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeader;

/**
 * Created by feaoes on 2017/11/10.
 */

public interface PacketManager {

    // 成功
    int SUCCESS = 0x00;
    // 用户名不存在
    int ErrorCode_UserName = 0x01;
    // 密码错误
    int ErrorCode_Password = 0x02;
    // 数据上传失败
    int ErrorCode_Data = 0x03;
    // 掉线
    int ErrorCode_Dropline = 0x04;
    // WebService调用失败
    int ErrorCode_Web = 0x05;
    // 数据包格式错误
    int ErrorCode_Packet = 0x06;
    // 服务器忙，传输失败（包含数据库操作失败）
    int ErrorCode_Busy = 0x07;
    // 用户未登录
    int ErrorCode_Login = 0x08;
    // 数据包类型错误
    int ErrorCode_PacketType = 0x09;
    // 数据包版本错误
    int ErrorCode_PacketVerSion = 0x0A;
    // 包的类型
    int PACKET_TYPE_HEART_LOGIN = 0x10;
    // 心跳包
    int PACKET_TYPE_HEARTBEAT = 0x11;
    // 数据包
    int PACKET_TYPE_DATA = 0x12;
    // 数据链路登录包
    int PACKET_TYPE_DATA_LOGIN = 0x13;

    void setPublicPacketFields(EmergencyMessageHeader packet, int packetType, String doctorId, String deviceUuid);

    byte[] getXMLDataPacketBytes(byte[] data, String doctorId);

    byte[] getLoginPacketBytes(String userName, String passwordMD5, String deviceUuid, String doctorId, boolean isHeartBearLogin);

    byte[] getHeartBeatPacketBytes(String doctorId);

}
