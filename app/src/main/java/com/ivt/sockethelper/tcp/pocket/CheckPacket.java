package com.ivt.sockethelper.tcp.pocket;

import android.text.TextUtils;

import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeader;
import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeaderConst;


/**
 * 检测包
 */
public class CheckPacket {
    private byte[] mCheckPacket;
    private final static int PACKET_TYPE_CHECK = 0x14;
    private final static String Tag = "CheckSocketPacket";
    private EmergencyMessageHeader.EmergencyMessageHeaderFormat CheckFormat;
    private EmergencyMessageHeader CheckHeader;
    private String mDocid = "";

    public CheckPacket() {
        CheckFormat = EmergencyMessageHeader.EmergencyMessageHeaderFormat
                .makeFormat(EmergencyMessageHeaderConst.HEADER_SEND_HEARTBEAT);
        CheckHeader = new EmergencyMessageHeader(CheckFormat);
    }

    /**
     * 获取检测包
     */
    public byte[] getCheckPacket(String mDocid) {
        if (TextUtils.isEmpty(mDocid) || this.mDocid.equals(mDocid)) {
            return mCheckPacket;
        }
        this.mDocid = mDocid;
        mCheckPacket = generatePacket();
        return mCheckPacket;
    }

    /**
     * @return
     */
    private byte[] generatePacket() {
        try {
            CheckHeader.clear();
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_IDENTIFIER,
                    0x7AA7);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
                    0);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_VERSION, 0x01);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_UNUSED, 0x0000);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
                    PACKET_TYPE_CHECK);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID, 0);
            byte[] temp = mDocid.getBytes("UTF-8");
            CheckHeader.packField(
                    EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH,
                    temp.length);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_DOCID, temp);
            CheckHeader.packField(
                    EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
                    PACKET_TYPE_CHECK);
            CheckHeader.packField(EmergencyMessageHeaderConst.FIELD_PACKET_ID,
                    0);
            CheckHeader.packField(
                    EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
                    CheckHeader.getLength() - 2);
        } catch (Exception e) {
            return null;
        }
        return CheckHeader.getBytes();
    }


}
