package com.ivt.sockethelper.tcp.manager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivt.sockethelper.tcp.SocketClient;
import com.ivt.sockethelper.tcp.configure.SocketConfigure;
import com.ivt.sockethelper.tcp.delegate.SocketClientDelegate;
import com.ivt.sockethelper.tcp.delegate.SocketClientReceivingDelegate;
import com.ivt.sockethelper.tcp.delegate.SocketClientSendingDelegate;
import com.ivt.sockethelper.tcp.pocket.SocketResponsePacket;
import com.ivt.sockethelper.tcp.pocket.SocketSendPacket;
import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeader;
import com.ivt.sockethelper.tcp.pocket.message.EmergencyMessageHeaderConst;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static com.ivt.sockethelper.tcp.manager.PacketManager.ErrorCode_Dropline;
import static com.ivt.sockethelper.tcp.manager.PacketManager.ErrorCode_Password;
import static com.ivt.sockethelper.tcp.manager.PacketManager.PACKET_TYPE_HEARTBEAT;
import static com.ivt.sockethelper.tcp.manager.PacketManager.PACKET_TYPE_HEART_LOGIN;

/**
 * Created by feaoes on 2017/12/6.
 */

public class HeartBeatSocketManager {

    private final String TAG = "HeartBeatSocketManager";

    private SocketConfigure socketConfigure;
    private SocketClient socketClient;

    private EmergencyMessageHeader.EmergencyMessageHeaderFormat loginRspFormat;
    private EmergencyMessageHeader loginheadRspHeader;


    public void onCreate() {
        //登陆响应包格式
        loginRspFormat = EmergencyMessageHeader.EmergencyMessageHeaderFormat
                .makeFormat(EmergencyMessageHeaderConst.HEADER_RESPONSE_USER_LOGIN);
        loginheadRspHeader = new EmergencyMessageHeader(loginRspFormat);

        //
        socketConfigure = SocketManager.getInstance().builderSocketConfig(SocketConfigure.SocketType.TYPE_HEART_BEAT);

        socketClient = SocketManager.getInstance().getSocketClient(socketConfigure);

        socketClient.registerSocketClientDelegate(new SocketClientDelegate() {
            @Override
            public void onConnected(SocketClient client) {
                Log.e(TAG, "SocketClient: onConnected");
                SocketSendPacket socketLoginPacket = SocketManager.getInstance().getSocketLoginPacket(true);
                socketClient.sendPacket(socketLoginPacket);
            }

            @Override
            public void onDisconnected(SocketClient client) {
                Log.e(TAG, "SocketClient: onDisconnected");

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //socketClient.connect();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }
                }.execute();

            }

            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                Log.e(TAG, "SocketClient: onResponse");
                if (null != responsePacket.getPacketData()) {

                    byte[] headerData = responsePacket.getHeaderData();
                    byte[] packetLength = responsePacket.getPacketLength();
                    byte[] packetData = responsePacket.getPacketData();

                    int length = headerData.length + packetLength.length + packetData.length;
                    byte[] newBuffer = new byte[length];
                    System.arraycopy(headerData, 0, newBuffer, 0, headerData.length);
                    System.arraycopy(packetLength, 0, newBuffer, headerData.length, packetLength.length);
                    System.arraycopy(packetData, 0, newBuffer, headerData.length + packetLength.length, packetData.length);


                    loginheadRspHeader.clear();
                    loginheadRspHeader.setBytes(newBuffer);

                    msgDispense(loginheadRspHeader, responsePacket.getPacketData());

                }
            }
        });

        socketClient.registerSocketClientSendingDelegate(new SocketClientSendingDelegate() {
            @Override
            public void onSendPacketBegin(SocketClient client, SocketSendPacket packet) {
                Log.e(TAG, "SocketClient: onSendPacketBegin: ");

            }

            @Override
            public void onSendPacketEnd(SocketClient client, SocketSendPacket packet) {
                Log.e(TAG, "SocketClient: onSendPacketEnd: packet:" + Arrays.toString(packet.getSendPacket()));

            }

            @Override
            public void onSendPacketCancel(SocketClient client, SocketSendPacket packet, String reason) {
                Log.e(TAG, "SocketClient: onSendPacketCancel: reason: " + reason);

            }

            @Override
            public void onSendingPacketInProgress(SocketClient client, SocketSendPacket packet, float progress, int sendedLength) {
                Log.e(TAG, "SocketClient: onSendingPacketInProgress: progress：" + progress + "--- sendedLength:" + sendedLength);
            }
        });

        socketClient.registerSocketClientReceiveDelegate(new SocketClientReceivingDelegate() {
            @Override
            public void onReceivePacketBegin(SocketClient client, SocketResponsePacket packet) {
                Log.e(TAG, "SocketClient: onReceivePacketBegin: ");
            }

            @Override
            public void onReceivePacketEnd(SocketClient client, SocketResponsePacket packet) {
                Log.e(TAG, "SocketClient: onReceivePacketEnd: ");

            }

            @Override
            public void onReceivePacketCancel(SocketClient client, SocketResponsePacket packet) {
                Log.e(TAG, "SocketClient: onReceivePacketCancel: ");
            }

            @Override
            public void onReceivingPacketInProgress(SocketClient client, SocketResponsePacket packet, float progress, int receivedLength) {
                Log.e(TAG, "SocketClient: onReceivingPacketInProgress: " + packet.hashCode() + " : " + progress + " : " + receivedLength);
            }
        });

        socketClient.connect();
    }


    /**
     * 检查包类型
     */
    public void msgDispense(EmergencyMessageHeader result, byte[] bytes) {
        Log.e(TAG, "msgDispense(),switch (packetType)");

        int packetType = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_PACKET_TYPE);
        int error = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_ERROR_CODE);

        try {
            byte[] data = new byte[]{115, 117, 99, 99, 101, 115, 115};

            String res = new String(data,"UTF-8");

            Log.e(TAG, "res: " + res);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        responseLogger();

        Log.e(TAG, "msgDispense: packetType: " + packetType);
        Log.e(TAG, "msgDispense: error: " + error);

        switch (packetType) {
            case PACKET_TYPE_HEARTBEAT:
                receiveHeartBeatRspFromServer(loginheadRspHeader);
                break;
            case PACKET_TYPE_HEART_LOGIN:
                receiveLoginRspFromServer(loginheadRspHeader);
                break;
            default:
                Log.e(TAG, "msgDispense(),default： ");
                break;
        }
    }

    /***
     * 接收服务器心跳包的反馈
     */
    private void receiveHeartBeatRspFromServer(EmergencyMessageHeader header) {
        int mRE_HBPacketID = header.getIntField(EmergencyMessageHeaderConst.FIELD_PACKET_ID);
        Log.e(TAG, "receiveHeartBeatRspFromServer(),接收服务器心跳包的反馈:mRE_HBPacketID: " + mRE_HBPacketID);
    }

    /***
     * 接收服务器登录的反馈
     */
    private void receiveLoginRspFromServer(EmergencyMessageHeader header) {
        int error = header.getIntField(EmergencyMessageHeaderConst.FIELD_ERROR_CODE);
        if (error == 0x00) {
            Log.e("socket", "receiveLoginRspFromServer" + "登录成功开始心跳机制");
            Log.e(TAG, "receiveLoginRspFromServer(),error == SUCCESS，登录成功开始心跳机制");

            //心跳链路发送心跳包，数据链路不发送
            socketClient.getHeartBeatCountDownTimer().start();
        } else if (error == ErrorCode_Dropline || error == ErrorCode_Password) {
            Log.e(TAG, "receiveLoginRspFromServer(),error == ErrorCode_Dropline || error == ErrorCode_Password,resetHeart()/restLogin()");
            Log.e("parsePushMsg", "ErrorCode_Dropline or ErrorCode_Password");
            socketClient.disconnect();
        } else {
            Log.e(TAG, "receiveLoginRspFromServer(),error == other,restLogin()");

            SocketSendPacket socketLoginPacket = SocketManager.getInstance().getSocketLoginPacket(true);
            socketClient.sendPacket(socketLoginPacket);
        }
    }


    public void onDestory() {
        socketClient.disconnect();
        socketClient = null;
        socketConfigure = null;
    }


    public static byte[] bytesMerger(byte[] byte_1, byte[] byte_2, byte[] byte_3) {
        byte[] newByte = new byte[byte_1.length + byte_2.length + byte_3.length];
        System.arraycopy(byte_1, 0, newByte, 0, byte_1.length);
        System.arraycopy(byte_2, 0, newByte, byte_1.length, byte_2.length);
        //System.arraycopy(byte_3, 0, newByte, byte_2.length, byte_3.length);
        return newByte;
    }


    public void responseLogger(){
        int FIELD_IDENTIFIER = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_IDENTIFIER);
        int FIELD_PACKAGE_LENGTH = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH);
        int FIELD_VERSION = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_VERSION);
        int FIELD_UNUSED = loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_UNUSED);
        int FIELD_PACKET_TYPE =  loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_PACKET_TYPE);
        int FIELD_PACKET_ID =  loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_PACKET_ID);
        int FIELD_ERROR_CODE =  loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_ERROR_CODE);
        int FIELD_ERROR_DESCRIPTION_LENGTH =  loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_ERROR_DESCRIPTION_LENGTH);
        int FIELD_ERROR_DESCRIPTION =  loginheadRspHeader.getIntField(EmergencyMessageHeaderConst.FIELD_ERROR_DESCRIPTION);

        StringBuilder sb = new StringBuilder();
        sb.append("FIELD_IDENTIFIER: ").append(FIELD_IDENTIFIER)
                .append("\nFIELD_PACKAGE_LENGTH: ").append(FIELD_PACKAGE_LENGTH)
                .append("\nFIELD_VERSION: ").append(FIELD_VERSION)
                .append("\nFIELD_UNUSED: ").append(FIELD_UNUSED)
                .append("\nFIELD_PACKET_TYPE: ").append(FIELD_PACKET_TYPE)
                .append("\nFIELD_PACKET_ID: ").append(FIELD_PACKET_ID)
                .append("\nFIELD_ERROR_CODE: ").append(FIELD_ERROR_CODE)
                .append("\nFIELD_ERROR_DESCRIPTION_LENGTH: ").append(FIELD_ERROR_DESCRIPTION_LENGTH)
                .append("\nFIELD_ERROR_DESCRIPTION: ").append(FIELD_ERROR_DESCRIPTION);

        Log.e(TAG, sb.toString());
    }

}
