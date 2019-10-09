package com.ivt.sockethelper.tcp.configure;


import com.ivt.sockethelper.utils.StringValidation;

import java.net.InetSocketAddress;

/**
 * Created by feaoes on 2017/9/18.
 */

public class SocketHelper {

    /**
     * 远程IP
     */
    private String remoteIP;
    /**
     * 远程端口
     */
    private String remotePort;
    /**
     * 连接超时时间
     */
    private int connectionTimeout;
    /**
     * 默认连接超时时间15s
     */
    public static final int DefaultConnectionTimeout = 1000 * 15;
    /**
     * 编码
     */
    private String charsetName = "UTF-8";


    public SocketHelper() {
        this(null, null);
    }

    public SocketHelper(String remoteIP, String remotePort) {
        this(remoteIP, remotePort, DefaultConnectionTimeout);
    }

    public SocketHelper(String remoteIP, String remotePort, int connectionTimeout) {
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * 判断ip、port、timeout合法性
     */
    public void checkValidation() {
      /*  if (!StringValidation.validateRegex(getRemoteIP(), StringValidation.RegexIP)) {
            throw new IllegalArgumentException("we need a correct remote IP to connect. Current is " + getRemoteIP());
        }*/

        if (!StringValidation.validateRegex(getRemotePort(), StringValidation.RegexPort)) {
            throw new IllegalArgumentException("we need a correct remote port to connect. Current is " + getRemotePort());
        }

        if (getConnectionTimeout() < 0) {
            throw new IllegalArgumentException("we need connectionTimeout > 0. Current is " + getConnectionTimeout());
        }
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(getRemoteIP(), getRemotePortIntegerValue());
    }

    public int getRemotePortIntegerValue() {
        if (getRemotePort() == null) {
            return 0;
        }
        return Integer.valueOf(getRemotePort());
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getCharsetName() {
        return charsetName;
    }
}
