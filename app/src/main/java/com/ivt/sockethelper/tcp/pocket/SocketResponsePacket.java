package com.ivt.sockethelper.tcp.pocket;

/**
 * Created by feaoes on 2017/11/15.
 */

public class SocketResponsePacket {

    private final SocketResponsePacket self = this;

    private byte[] receivePacket;

    //包头
    private byte[] headerData;
    //四字节，标明除Header之外整包的长度，低字节在前，高字节在后
    private byte[] packetLength;
    //除包头包长以外的数据
    private byte[] packetData;
    //String data
    private String message;
    /**
     * 协议版本号（0x01），1字节
     */
    private byte version = 0x01;
    /**
     * 备用字节（必须填写：0x0000），2字节
     */
    private short unUserd = 0x0000;
    /**
     * 数据包类型，1字节
     */
    private byte packetType;
    private int packetId;
    private int errorCode;
    private int errorDescriptionLength;
    private String errorDescription;
    private int dataLength;
    private String data;
    //包尾
    private byte[] trailerData;

    public byte[] getHeaderData() {
        return headerData;
    }

    public void setHeaderData(byte[] headerData) {
        this.headerData = headerData;
    }

    public byte[] getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(byte[] packetLength) {
        this.packetLength = packetLength;
    }

    public byte[] getPacketData() {
        return packetData;
    }

    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public short getUnUserd() {
        return unUserd;
    }

    public void setUnUserd(short unUserd) {
        this.unUserd = unUserd;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorDescriptionLength() {
        return errorDescriptionLength;
    }

    public void setErrorDescriptionLength(int errorDescriptionLength) {
        this.errorDescriptionLength = errorDescriptionLength;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getTrailerData() {
        return trailerData;
    }

    public byte[] getReceivePacket() {
        return receivePacket;
    }

    public void setReceivePacket(byte[] receivePacket) {
        this.receivePacket = receivePacket;
    }

    public void setTrailerData(byte[] trailerData) {
        this.trailerData = trailerData;
    }
}