package com.ivt.sockethelper.tcp;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivt.sockethelper.tcp.configure.SocketConfigure;
import com.ivt.sockethelper.tcp.configure.SocketHelper;
import com.ivt.sockethelper.tcp.configure.SocketInputReader;
import com.ivt.sockethelper.tcp.delegate.SocketClientDelegate;
import com.ivt.sockethelper.tcp.delegate.SocketClientReceivingDelegate;
import com.ivt.sockethelper.tcp.delegate.SocketClientSendingDelegate;
import com.ivt.sockethelper.tcp.manager.SocketManager;
import com.ivt.sockethelper.tcp.pocket.SocketResponsePacket;
import com.ivt.sockethelper.tcp.pocket.SocketSendPacket;
import com.ivt.sockethelper.utils.ByteUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket处理类
 */

public class SocketClient {

    final SocketClient self = this;

    private static final String TAG = SocketClient.class.getSimpleName();

    public SocketClient(SocketConfigure socketConfigure) {
        this.socketConfigure = socketConfigure;
    }


    /**
     * Socekt配置管理器
     */
    private SocketConfigure socketConfigure;

    public SocketClient setSocketConfigure(SocketConfigure socketConfigure) {
        this.socketConfigure = socketConfigure;
        return this;
    }

    public SocketConfigure getSocketConfigure() {
        if (this.socketConfigure == null) {
            throw new IllegalArgumentException("SocketConfigure not null");
        }
        return this.socketConfigure;
    }

    /**
     * 主线程Handler,持有SocketClient的弱引用
     */
    private UIHandler uiHandler;

    protected UIHandler getUiHandler() {
        if (this.uiHandler == null) {
            this.uiHandler = new UIHandler(this);
        }
        return this.uiHandler;
    }

    private static class UIHandler extends Handler {

        private WeakReference<SocketClient> referenceSocketClient;

        public UIHandler(@NonNull SocketClient referenceSocketClient) {
            super(Looper.getMainLooper());

            this.referenceSocketClient = new WeakReference<SocketClient>(referenceSocketClient);
        }

        @Override
        public void handleMessage(Message msg) {
            SocketClient socketClient = referenceSocketClient.get();
            if (null == socketClient) {
                return;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 数据包加入消息队列子线程
     */
    private Handler packetEnqueueHandler;

    private HandlerThread handlerThread;

    private Handler getPacketEnqueueHandler() {

        if (this.packetEnqueueHandler == null) {
            if (this.handlerThread == null) {
                handlerThread = new HandlerThread("packet_enqueue_thread");
            }
            if (!this.handlerThread.isAlive()) {
                handlerThread.start();
            }
            this.packetEnqueueHandler = new Handler(handlerThread.getLooper());
        }
        return this.packetEnqueueHandler;
    }

    /**
     * 当前Socket连接状态
     */
    private enum State {
        Disconnected, Connecting, Connected;
    }

    private State state;

    public State getState() {
        if (this.state == null) {
            return State.Disconnected;
        }
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * 是否未连接,已连接，连接中
     *
     * @return
     */
    public boolean isDisconnected() {
        return getState() == State.Disconnected;
    }

    public boolean isConnected() {
        return getState() == State.Connected;
    }

    public boolean isConnecting() {
        return getState() == State.Connecting;
    }

    /**
     * 正在断开连接状态
     */
    private boolean disconnecting;

    protected SocketClient setDisconnecting(boolean disconnecting) {
        this.disconnecting = disconnecting;
        return this;
    }

    public boolean isDisconnecting() {
        return this.disconnecting;
    }


    public SocketSendPacket sendPacket(final SocketSendPacket packet) {
        //TODO 检测本地连接状态，并检测Socket,inputStream状态

        if (packet == null) {
            throw new IllegalArgumentException("In sendPacket, SocketSendPacket cannot be empty");
        }

        //如果是未连接状态，并且已经完全断开连接，则重新连接
        if (!isConnected() && isDisconnected()) {
            connect();
        }
        //已连接    检查发送线程，如果未处于活跃状态，则开启，并将数据包加入发送队列, return null;
        else if (isConnected() && !getSendThread().isAlive()) {
            getSendThread().start();
        }
        //已连接    检查接收线程，如果未处于活跃状态，则开启，并将数据包加入发送队列, return null;
        else if (isConnected() && !getReceiveThread().isAlive()) {
            getReceiveThread().start();
        }
        //正在连接  可以将数据包加入发送队列，等待连接成功后发送,如果连接失败，则会断开连接，并将发送队列数据包返回给发送者
        else if (isConnecting()) {
        }
        //正在断开  说明此时不可以发送数据包，则不加入发送队列，直接 return  packet
        else if (isDisconnecting()) {
            return packet;
        }

        //TODO 本地连接状态正常，但远程连接状态未知，所以发送检测包检测，检测成功后才发送数据包

        self.__i__enqueueNewPacket(packet);

        return null;
    }

    /**
     * 取消发送
     */
    public void cancelSend(final SocketSendPacket packet) {
        //TODO 待修改...
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (getSendingPacketQueue()) {
                    if (self.getSendingPacketQueue().contains(packet)) {
                        self.getSendingPacketQueue().remove(packet);
                        self.__i__onSendPacketCancel(packet, "cancelSend(),主动取消发送,取消发送队列中尚未发送的数据包");
                    }
                }
            }
        }).start();
    }

    /**
     * 发送包阻塞队列
     */
    private LinkedBlockingQueue<SocketSendPacket> sendingPacketQueue;

    public LinkedBlockingQueue<SocketSendPacket> getSendingPacketQueue() {
        if (null == sendingPacketQueue)
            this.sendingPacketQueue = new LinkedBlockingQueue<>();
        return sendingPacketQueue;
    }

    /**
     * Socket输入流读取类
     */
    private SocketInputReader socketInputReader;

    protected SocketClient setSocketInputReader(SocketInputReader socketInputReader) {
        this.socketInputReader = socketInputReader;
        return this;
    }

    protected SocketInputReader getSocketInputReader() throws IOException {
        if (this.socketInputReader == null) {
            this.socketInputReader = new SocketInputReader(getRunningSocket().getInputStream());
        }
        return this.socketInputReader;
    }

    /**
     * Socket对象
     */
    private Socket runningSocket;

    public Socket getRunningSocket() {
        if (null == runningSocket)
            runningSocket = new Socket();
        return runningSocket;
    }

    public void setRunningSocket(Socket runningSocket) {
        this.runningSocket = runningSocket;
    }

    /**
     * 连接Socket
     */
    public void connect() {
        //1、判断Socket是否已经断开连接，已经是未连接状态才去连接
        if (!isDisconnected()) {
            return;
        }
        //2、检查Socket配置信息
        if (getSocketConfigure().getSocketHelper() == null) {
            throw new IllegalArgumentException("we need a SocketHepler to connect");
        }
        //3、判断ip、port、timeout合法性
        getSocketConfigure().getSocketHelper().checkValidation();
        //4、设置当前Socket状态
        setState(State.Connecting);
        //5、开启连接线程
        if (!getConnectionThread().isAlive()) {
            getConnectionThread().start();
        }
    }

    /**
     * 未连接
     */
    public void disconnect() {
        if (isDisconnected() || isDisconnecting()) {
            return;
        }
        //正在断开连接
        setDisconnecting(true);

        getDisconnectionThread().start();
    }

    /**
     * 连接线程
     */
    private ConnectionThread connectionThread;

    public ConnectionThread getConnectionThread() {
        if (null == connectionThread) {
            connectionThread = new ConnectionThread();
        }
        return connectionThread;
    }

    public void setConnectionThread(ConnectionThread connectionThread) {
        this.connectionThread = connectionThread;
    }

    /**
     * 连接线程处理
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                SocketHelper socketHelper = self.getSocketConfigure().getSocketHelper();

                if (Thread.interrupted()) {
                    return;
                }

                //判断Socket链路类型
                SocketConfigure.SocketType socketType = self.getSocketConfigure().getSocketType();

              /*  if (socketType == SocketConfigure.SocketType.TYPE_HEART_BEAT) {
                    //设置读取数据超时时间（毫秒），0为永不超时
                    self.getRunningSocket().setSoTimeout(0);
                } else if (socketType == SocketConfigure.SocketType.TYPE_DATA) {
                    //设置读取数据超时时间（毫秒），30毫秒超时时间
                    self.getRunningSocket().setSoTimeout(30 * 1000);
                }*/

                //Socket连接
                self.getRunningSocket().connect(socketHelper.getInetSocketAddress(), socketHelper.getConnectionTimeout());

                if (Thread.interrupted()) {
                    return;
                }

                int soTimeout = self.getRunningSocket().getSoTimeout();

                Log.e(TAG, "soTimeout: " + soTimeout);

                self.setState(SocketClient.State.Connected);

                self.setLastSendHeartBeatMessageTime(System.currentTimeMillis());
                self.setLastSendMessageTime(NoSendingTime);
                self.setLastReceiveMessageTime(System.currentTimeMillis());

                self.setSendingPacket(null);
                self.setReceivingResponsePacket(null);

                //重置连接线程
                self.setConnectionThread(null);

                //开始已连接操作
                self.__i__onConnected();


            } catch (IOException e) {
                e.printStackTrace();

                self.disconnect();
            }

        }
    }

    /**
     * 未连接线程
     */
    private DisconnectionThread disconnectionThread;

    public DisconnectionThread getDisconnectionThread() {
        if (null == disconnectionThread)
            this.disconnectionThread = new DisconnectionThread();
        return disconnectionThread;
    }

    public void setDisconnectionThread(DisconnectionThread disconnectionThread) {
        this.disconnectionThread = disconnectionThread;
    }

    /**
     * 未连接线程处理
     */
    private class DisconnectionThread extends Thread {
        @Override
        public void run() {
            super.run();
            //关闭连接线程
            if (null != self.connectionThread) {
                self.getConnectionThread().interrupt();
                self.setConnectionThread(null);
            }
            //关闭Socket以及输入输出流
            if (!self.getRunningSocket().isClosed() || self.isConnecting()) {
                try {
                    self.getRunningSocket().getOutputStream().close();
                    self.getRunningSocket().getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        self.getRunningSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    self.setRunningSocket(null);
                }
            }

            if (self.sendThread != null) {
                self.getSendThread().interrupt();
                self.setSendThread(null);
            }

            if (self.receiveThread != null) {
                self.getReceiveThread().interrupt();
                self.setReceiveThread(null);
            }

            if (self.heartBeatCountDownTimer != null) {
                self.heartBeatCountDownTimer.cancel();
            }

            self.setDisconnecting(false);
            self.setState(SocketClient.State.Disconnected);
            self.setSocketInputReader(null);
            //      self.setSocketConfigure(null);

            if (null != self.getSendingPacket()) {
                self.__i__onSendPacketCancel(self.getSendingPacket(), "DisconnectionThread(),正在断开连接,正在发送中的数据包被取消发送");
                self.setSendingPacket(null);
            }

            SocketSendPacket packet;
            while (null != (packet = self.getSendingPacketQueue().poll())) {
                self.__i__onSendPacketCancel(packet, "DisconnectionThread(),正在断开连接,发送队列中存在的所有数据包被取消发送");
            }

            if (null != self.getReceivingResponsePacket()) {
                self.__i__onReceivePacketCancel(self.getReceivingResponsePacket());
                self.setReceivingResponsePacket(null);
            }
            self.setDisconnectionThread(null);

            self.__i__onDisconnected();
            /**
             * 未完待续.....................
             */

        }
    }

    /**
     * 发送线程
     */
    private SendThread sendThread;

    public SendThread getSendThread() {
        if (null == sendThread)
            this.sendThread = new SendThread();
        return sendThread;
    }

    public void setSendThread(SendThread sendThread) {
        this.sendThread = sendThread;
    }

    /**
     * LinkedBlockingQueue  http://jiangzhengjun.iteye.com/blog/683593
     * put         添加一个元素                如果队列满，则阻塞
     * take        移除并返回队列头部的元素     如果队列为空，则阻塞
     */
    /**
     * 发送线程处理
     */
    private class SendThread extends Thread {
        @Override
        public void run() {
            super.run();

            SocketSendPacket packet;

            try {
                while (self.isConnected()   //已连接状态
                        && !self.isDisconnecting() //正在断开连接时不再发送数据
                        && !Thread.interrupted()
                        && null != (packet = self.getSendingPacketQueue().take())) {
                    //设置发送包
                    self.setSendingPacket(packet);
                    self.setLastSendMessageTime(System.currentTimeMillis());

                    try { //防止异常后结束while循环

                        //数据包
                        byte[] sendPacket = packet.getSendPacket();

                        //通知数据发送失败
                        if (null == sendPacket || sendPacket.length == 0) {
                            self.__i__onSendPacketCancel(packet, "SendThread(),取消发送,数据包不能为空或长度不能为0");
                            self.setSendingPacket(null);
                            continue;
                        }

                        int sendPaketLength = sendPacket.length;

                        //已发送数据包长度
                        int sendedPacketLength = 0;

                        int segmentLength = self.getRunningSocket().getSendBufferSize();
                        //是否分段发送
                        if (self.getSocketConfigure().getSocketSendPacketHelper().isSendSegmentEnabled()) {
                            //分段发送大小，计算发送数据长度
                            segmentLength = Math.min(segmentLength, self.getSocketConfigure().getSocketSendPacketHelper().getSendSegmentLength());
                        }

                        int offset = 0;

                        while (offset < sendPaketLength) {
                            int end = offset + segmentLength;
                            end = Math.min(end, sendPaketLength);
                            self.getRunningSocket().getOutputStream().write(sendPacket, offset, end - offset);
                            self.getRunningSocket().getOutputStream().flush();
                            self.setLastSendMessageTime(System.currentTimeMillis());

                            sendedPacketLength += end - offset;

                            self.__i__onSendingPacketInProgress(packet, sendedPacketLength);

                            offset = end;
                        }

                        self.__i__onSendPacketEnd(packet);

                        self.setSendingPacket(null);

                        //发送时计时，结束后重置计时
                        self.setLastSendMessageTime(NoSendingTime);

                    } catch (Exception e) {
                        Log.e(TAG, "SendThread->while内部：" + e.toString());
                        e.printStackTrace();

                        if (self.getSendingPacket() != null) {
                            self.__i__onSendPacketCancel(self.getSendingPacket(), "SendThread(),while内部,取消发送,发送数据时出现异常,正在发送中的数据被取消发送");
                            self.setSendingPacket(null);
                        }

                        //TODO 发送检测包，检测Socket状态，发送失败则断开连接，
                        if (e instanceof IOException) {
                            //断开连接,断开连接后，心跳链路重连，数据链路检测是否有发送失败数据，若有则重连发送，重复n次后停止,直到下次主动发送是重连,
                            disconnect();
                        }

                    } // try end

                } //while end
            } catch (InterruptedException e) {
                Log.e(TAG, "SendThread->while外部：" + e.toString());
                e.printStackTrace();

                if (self.getSendingPacket() != null) {
                    self.__i__onSendPacketCancel(self.getSendingPacket(), "SendThread(),while外部,取消发送,发送数据时出现异常,正在发送中的数据被取消发送");
                    self.setSendingPacket(null);
                }
            }
        }
    }

    /**
     * 发送检测包
     */
    private void sendCheckPacket() {

        byte[] sendCheckPacketData = self.getSocketConfigure().getSocketSendPacketHelper().getSendCheckPacketData();

        if (null != sendCheckPacketData) {
            try {
                self.getRunningSocket().getOutputStream().write(sendCheckPacketData);
                self.getRunningSocket().getOutputStream().flush();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.toString());
                //发生数据包失败，断开连接
                disconnect();
            }
        }
    }

    /**
     * 接收线程
     */
    private ReceiveThread receiveThread;

    public ReceiveThread getReceiveThread() {
        if (null == receiveThread)
            this.receiveThread = new ReceiveThread();
        return receiveThread;
    }

    public void setReceiveThread(ReceiveThread receiveThread) {
        this.receiveThread = receiveThread;
    }

    /**
     * 接收线程处理
     * <p>
     * 心跳链路：永不超时，每30s发送一次心跳包，若发送心跳包之前连接已断开，则会检查重连；若之后连接断开或接收线程已死，会接受数据失败，等到30s后再次发送心跳包时做检查。
     * 数据链路：有超时时间，以最近两次接收数据包时间为时间间隔，若大于超时时间，则超时，断开连接。
     * <p>
     * 数据接收到-1时，可能被踢下线，则重连重登录
     */
    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                while (self.isConnected()
                        && self.getSocketInputReader() != null
                        && !Thread.interrupted()) {
                    SocketResponsePacket packet = new SocketResponsePacket();
                    self.setReceivingResponsePacket(packet);

                    byte[] headerData = self.getSocketConfigure().getSocketReceivePacketHelper().getReceiveHeaderData();

                    Log.e(TAG, "headerData: " + Arrays.toString(headerData));
                    int headerDataLength = headerData == null ? 0 : headerData.length;


                    //四字节，标明除Header 之外整包的长度，低字节在前，高字节在后
                    int packetLengthDataLength = 4;

                    int dataLength = 0;
                    int receivedPacketLength = 0;

                    self.__i__onReceivePacketBegin(packet);

                    if (headerDataLength > 0) {
                        //根据包头字节数据，先获取包头数据
                        byte[] data = self.getSocketInputReader().readToData(headerData, true);

                        if (null != data) {
                            self.setLastReceiveMessageTime(System.currentTimeMillis());
                            packet.setHeaderData(data);
                        } else {
                            Log.e(TAG, "readToData return null, no packetData has been obtained");
                        }

                        receivedPacketLength += headerDataLength;
                    }

                    //根据包长数据的字节长度，获取包长度字节内容
                    byte[] data = self.getSocketInputReader().readToLength(packetLengthDataLength);

                    if (null != data) {

                        self.setLastReceiveMessageTime(System.currentTimeMillis());
                        packet.setPacketLength(data);
                        Log.e("socket", "读取包长内容为: " + Arrays.toString(data));

                        receivedPacketLength += packetLengthDataLength;

                        //包长数据以及后面的数据包长度
                        int bodyTrailerLength = ByteUtil.byteToIntLow(data);

                        Log.e("socket", "数据包内容的长度：bodyTrailerLength: " + bodyTrailerLength);

                        dataLength = bodyTrailerLength - packetLengthDataLength;
                    }
                    if (dataLength > 0) {
                        Log.e("socket", "开始获取数据包内容...");
                        //2097152b = 2M
                        int segmentLength = self.getRunningSocket().getReceiveBufferSize();
                        if (self.getSocketConfigure().getSocketReceivePacketHelper().isReceiveSegmentEnabled()) {
                            Log.e("socket", "getReceiveBufferSize： " + segmentLength);
                            Log.e("socket", "segmentLength： " + self.getSocketConfigure().getSocketReceivePacketHelper().getReceiveSegmentLength());
                            segmentLength = Math.min(segmentLength, self.getSocketConfigure().getSocketReceivePacketHelper().getReceiveSegmentLength());
                        }
                        Log.e("socket", "segmentLength：final:  " + segmentLength);
                        int offset = 0;
                        while (offset < dataLength) {
                            int end = offset + segmentLength;
                            end = Math.min(end, dataLength);
                            Log.e("socket", "最后需要读取的数据长度为: " + end);

                            data = self.getSocketInputReader().readToLength(end - offset);

                            Log.e("socket", "读取数据包内容为: " + Arrays.toString(data));

                            self.setLastReceiveMessageTime(System.currentTimeMillis());

                            if (packet.getPacketData() == null) {
                                packet.setPacketData(data);
                                Log.e("socket", "首次设置数据包内容为: " + Arrays.toString(data));
                            } else {
                                byte[] mergedData = new byte[packet.getPacketData().length + data.length];

                                System.arraycopy(packet.getPacketData(), 0, mergedData, 0, packet.getPacketData().length);
                                System.arraycopy(data, 0, mergedData, packet.getPacketData().length, data.length);

                                packet.setPacketData(mergedData);
                                Log.e("socket", "再次合并数据包内容为: " + Arrays.toString(data));
                            }

                            receivedPacketLength += end - offset;

                            self.__i__onReceivingPacketInProgress(packet, receivedPacketLength, dataLength);

                            offset = end;
                        }

                        self.__i__onReceivePacketEnd(packet);
                        self.__i__onReceiveResponse(packet);
                        self.setReceivingResponsePacket(null);

                    } else if (dataLength <= 0) {
                        self.__i__onReceivePacketCancel(packet);
                        self.setReceivingResponsePacket(null);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "ReceiveThread(),Exception: " + e.toString());
                e.printStackTrace();

                if (self.getReceivingResponsePacket() != null) {
                    self.__i__onReceivePacketCancel(self.getReceivingResponsePacket());
                    self.setReceivingResponsePacket(null);
                }
            }
        }
    }

    private void __i__onReceivingPacketInProgress(final SocketResponsePacket packet, final int receivedLength, final int dataLength) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - getLastReceiveProgressCallbackTime() < (1000 / 24)) {
            return;
        }

        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivingPacketInProgress(packet, receivedLength, dataLength);
                }
            });
            return;
        }

        float progress = receivedLength / (float) (dataLength);

        if (getSocketClientReceivingDelegates().size() > 0) {
            ArrayList<SocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<SocketClientReceivingDelegate>) getSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onReceivingPacketInProgress(this, packet, progress, receivedLength);
            }
        }

        setLastReceiveProgressCallbackTime(System.currentTimeMillis());
    }

    private void __i__onReceiveResponse(@NonNull final SocketResponsePacket responsePacket) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceiveResponse(responsePacket);
                }
            });
            return;
        }

        setLastReceiveMessageTime(System.currentTimeMillis());

        if (getSocketClientDelegates().size() > 0) {
            ArrayList<SocketClientDelegate> delegatesCopy =
                    (ArrayList<SocketClientDelegate>) getSocketClientDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onResponse(this, responsePacket);
            }
        }
    }

    /**
     * 倒计时工具类，用于检测发送心跳包时间，发送超时，读取超时
     * <p>
     * 每隔1秒调用一次onTick()
     * 倒计时结束时调用onFinish()
     */
    private CountDownTimer heartBeatCountDownTimer;

    public CountDownTimer getHeartBeatCountDownTimer() {
        if (null == this.heartBeatCountDownTimer) {
            this.heartBeatCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {  //同步方法
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            self.__i__onTimeTick();
                        }
                    }).start();

                }

                @Override
                public void onFinish() {
                    if (self.isConnected()) {
                        this.start();
                    }
                }
            };
        }
        return this.heartBeatCountDownTimer;
    }

    private void __i__onTimeTick() {
        if (!self.isConnected())
            return;

        long currentTime = System.currentTimeMillis();

        /**
         * 只在心跳链路发送心跳包，当前时间与最后一次发送心跳包时间间隔超过指定时间，则重新发送心跳包，并记录时间
         */
        if (getSocketConfigure().getSocketType() == SocketConfigure.SocketType.TYPE_HEART_BEAT &&
                getSocketConfigure().getSocketHeartBeatHelper().isSendHeartBeatEnabled()) {
            if (currentTime - getLastSendHeartBeatMessageTime() >= getSocketConfigure().getSocketHeartBeatHelper().getHeartBeatInterval()) {
                __i__sendHeartBeat();
                setLastSendHeartBeatMessageTime(System.currentTimeMillis());
            }
        }

        /**
         * 检测是否接收超时，如果（当前时间 - 最后一次接收数据包时间）超过指定时间，则断开Socket连接
         * 心跳链路永不超时
         * 数据链路暂时设置超时时间为30s
         */
        if (getSocketConfigure().getSocketReceivePacketHelper().isReceiveTimeoutEnabled()) {
            if (currentTime - getLastReceiveMessageTime() >= getSocketConfigure().getSocketReceivePacketHelper().getReceiveTimeout()) {
                disconnect();
            }
        }

        /**
         * 检测是否发送超时，如果（当前时间 - 最后一次发送数据包时间）超过指定时间，则断开Socket连接
         * 心跳链路永不超时
         * 数据链路暂时设置超时时间为30s
         */
        if (getSocketConfigure().getSocketSendPacketHelper().isSendTimeoutEnabled() && getLastSendMessageTime() != NoSendingTime) {
            if (currentTime - getLastSendMessageTime() >= getSocketConfigure().getSocketSendPacketHelper().getSendTimeout()) {
                disconnect();
            }
        }
    }

    /**
     * 发送心跳包
     */
    private void __i__sendHeartBeat() {
        if (!isConnected())
            return;

        if (getSocketConfigure() == null
                || getSocketConfigure().getSocketHeartBeatHelper() == null
                || !getSocketConfigure().getSocketHeartBeatHelper().isSendHeartBeatEnabled()) {
            return;
        }

        final SocketSendPacket packet = SocketManager.getInstance().getSocketHeartBeatPacket();
        /**
         * 未完待续...
         */
        self.__i__enqueueNewPacket(packet);
    }


    /**
     * 在子线程中,按顺序将发送包加入发送队列
     */
    private void __i__enqueueNewPacket(final SocketSendPacket packet) {

        getPacketEnqueueHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    getSendingPacketQueue().put(packet);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 正在发送中的数据包
     */
    private SocketSendPacket sendingPacket;

    protected SocketClient setSendingPacket(SocketSendPacket sendingPacket) {
        this.sendingPacket = sendingPacket;
        return this;
    }

    protected SocketSendPacket getSendingPacket() {
        return this.sendingPacket;
    }

    /**
     * 正在接收的响应包
     */
    private SocketResponsePacket receivingResponsePacket;

    protected SocketClient setReceivingResponsePacket(SocketResponsePacket receivingResponsePacket) {
        this.receivingResponsePacket = receivingResponsePacket;
        return this;
    }

    protected SocketResponsePacket getReceivingResponsePacket() {
        return this.receivingResponsePacket;
    }

    private long lastReceiveProgressCallbackTime;

    protected SocketClient setLastReceiveProgressCallbackTime(long lastReceiveProgressCallbackTime) {
        this.lastReceiveProgressCallbackTime = lastReceiveProgressCallbackTime;
        return this;
    }

    protected long getLastReceiveProgressCallbackTime() {
        return this.lastReceiveProgressCallbackTime;
    }

    /**
     * 记录上次发送数据片段的时间
     * 仅在每个发送包开始发送时计时，结束后重置计时
     * NoSendingTime 表示当前没有在发送数据
     */
    private final static long NoSendingTime = -1;
    private long lastSendMessageTime = NoSendingTime;

    protected SocketClient setLastSendMessageTime(long lastSendMessageTime) {
        this.lastSendMessageTime = lastSendMessageTime;
        return this;
    }

    protected long getLastSendMessageTime() {
        return this.lastSendMessageTime;
    }

    /**
     * 记录上次接收到消息的时间
     */
    private long lastReceiveMessageTime;

    protected SocketClient setLastReceiveMessageTime(long lastReceiveMessageTime) {
        this.lastReceiveMessageTime = lastReceiveMessageTime;
        return this;
    }

    protected long getLastReceiveMessageTime() {
        return this.lastReceiveMessageTime;
    }


    /**
     * 记录上次发送心跳包的时间
     */
    private long lastSendHeartBeatMessageTime;

    protected SocketClient setLastSendHeartBeatMessageTime(long lastSendHeartBeatMessageTime) {
        this.lastSendHeartBeatMessageTime = lastSendHeartBeatMessageTime;
        return this;
    }

    protected long getLastSendHeartBeatMessageTime() {
        return this.lastSendHeartBeatMessageTime;
    }

    /**
     * SocketClientDelegate
     */
    private ArrayList<SocketClientDelegate> socketClientDelegates;

    protected ArrayList<SocketClientDelegate> getSocketClientDelegates() {
        if (this.socketClientDelegates == null) {
            this.socketClientDelegates = new ArrayList<SocketClientDelegate>();
        }
        return this.socketClientDelegates;
    }

    /**
     * SocketClientReceivingDelegate
     */
    private ArrayList<SocketClientReceivingDelegate> socketClientReceivingDelegates;

    protected ArrayList<SocketClientReceivingDelegate> getSocketClientReceivingDelegates() {
        if (this.socketClientReceivingDelegates == null) {
            this.socketClientReceivingDelegates = new ArrayList<SocketClientReceivingDelegate>();
        }
        return this.socketClientReceivingDelegates;
    }

    /**
     * SocketClientSendingDelegate
     */
    private ArrayList<SocketClientSendingDelegate> socketClientSendingDelegates;

    protected ArrayList<SocketClientSendingDelegate> getSocketClientSendingDelegates() {
        if (this.socketClientSendingDelegates == null) {
            this.socketClientSendingDelegates = new ArrayList<SocketClientSendingDelegate>();
        }
        return this.socketClientSendingDelegates;
    }

    /**
     * Socket已连接通知
     */
    private void __i__onConnected() {
        /**
         * 切换到主线程通知连接状态
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onConnected();
                }
            });
            return;
        }
        //通知已连接
        ArrayList<SocketClientDelegate> delegatesCopy = (ArrayList<SocketClientDelegate>) getSocketClientDelegates().clone();
        int count = delegatesCopy.size();
        for (int i = 0; i < count; i++) {
            delegatesCopy.get(i).onConnected(self);
        }

        //开启发送线程、接收线程、心跳线程
        if (!getSendThread().isAlive())
            getSendThread().start();
        if (!getReceiveThread().isAlive())
            getReceiveThread().start();
        //心跳链路发送心跳包，数据链路不发送
        //getHeartBeatCountDownTimer().start();
    }

    /**
     * 开始发送数据通知
     */
    private void __i__onSendPacketBegin(final SocketSendPacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketBegin(packet);
                }
            });
            return;
        }

        if (getSocketClientDelegates().size() > 0) {
            ArrayList<SocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<SocketClientSendingDelegate>) getSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onSendPacketBegin(this, packet);
            }
        }
    }

    /**
     * 发送数据失败通知
     */
    private void __i__onSendPacketCancel(final SocketSendPacket packet, final String reason) {
        /**
         * 切换到主线程通知连接状态
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketCancel(packet, reason);
                }
            });
        }

        if (getSocketClientDelegates().size() > 0) {
            ArrayList<SocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<SocketClientSendingDelegate>) getSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onSendPacketCancel(this, packet, reason);
                //TODO 数据包被取消发送后，统一处理，给出取消原因，
            }
        }
    }

    /**
     * 数据发送完成通知
     */
    private void __i__onSendPacketEnd(final SocketSendPacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendPacketEnd(packet);
                }
            });
            return;
        }

        if (getSocketClientDelegates().size() > 0) {
            ArrayList<SocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<SocketClientSendingDelegate>) getSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onSendPacketEnd(this, packet);
            }
        }
    }

    private void __i__onSendingPacketInProgress(final SocketSendPacket packet, final int sendedLength) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onSendingPacketInProgress(packet, sendedLength);
                }
            });
            return;
        }

        float progress = sendedLength / (float) (packet.getSendPacket().length);

        if (getSocketClientDelegates().size() > 0) {
            ArrayList<SocketClientSendingDelegate> delegatesCopy =
                    (ArrayList<SocketClientSendingDelegate>) getSocketClientSendingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onSendingPacketInProgress(this, packet, progress, sendedLength);
            }
        }
    }

    private void __i__onDisconnected() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onDisconnected();
                }
            });
            return;
        }

        ArrayList<SocketClientDelegate> delegatesCopy =
                (ArrayList<SocketClientDelegate>) getSocketClientDelegates().clone();
        int count = delegatesCopy.size();
        for (int i = 0; i < count; ++i) {
            delegatesCopy.get(i).onDisconnected(this);
        }
    }

    private void __i__onReceivePacketBegin(final SocketResponsePacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivePacketBegin(packet);
                }
            });
            return;
        }

        if (getSocketClientReceivingDelegates().size() > 0) {
            ArrayList<SocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<SocketClientReceivingDelegate>) getSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onReceivePacketBegin(this, packet);
            }
        }
    }

    private void __i__onReceivePacketEnd(final SocketResponsePacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivePacketEnd(packet);
                }
            });
            return;
        }

        if (getSocketClientReceivingDelegates().size() > 0) {
            ArrayList<SocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<SocketClientReceivingDelegate>) getSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onReceivePacketEnd(this, packet);
            }
        }
    }

    private void __i__onReceivePacketCancel(final SocketResponsePacket packet) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    self.__i__onReceivePacketCancel(packet);
                }
            });
            return;
        }

        if (getSocketClientReceivingDelegates().size() > 0) {
            ArrayList<SocketClientReceivingDelegate> delegatesCopy =
                    (ArrayList<SocketClientReceivingDelegate>) getSocketClientReceivingDelegates().clone();
            int count = delegatesCopy.size();
            for (int i = 0; i < count; ++i) {
                delegatesCopy.get(i).onReceivePacketCancel(this, packet);
            }
        }
    }


    /**
     * 注册监听回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient registerSocketClientDelegate(SocketClientDelegate delegate) {
        if (!getSocketClientDelegates().contains(delegate)) {
            getSocketClientDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册监听回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient removeSocketClientDelegate(SocketClientDelegate delegate) {
        getSocketClientDelegates().remove(delegate);
        return this;
    }

    /**
     * 注册信息发送回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient registerSocketClientSendingDelegate(SocketClientSendingDelegate delegate) {
        if (!getSocketClientSendingDelegates().contains(delegate)) {
            getSocketClientSendingDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册信息发送回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient removeSocketClientSendingDelegate(SocketClientSendingDelegate delegate) {
        getSocketClientSendingDelegates().remove(delegate);
        return this;
    }

    /**
     * 注册信息接收回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient registerSocketClientReceiveDelegate(SocketClientReceivingDelegate delegate) {
        if (!getSocketClientReceivingDelegates().contains(delegate)) {
            getSocketClientReceivingDelegates().add(delegate);
        }
        return this;
    }

    /**
     * 取消注册信息接收回调
     *
     * @param delegate 回调接收者
     */
    public SocketClient removeSocketClientReceiveDelegate(SocketClientReceivingDelegate delegate) {
        getSocketClientReceivingDelegates().remove(delegate);
        return this;
    }

}
