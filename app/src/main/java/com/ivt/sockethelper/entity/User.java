package com.ivt.sockethelper.entity;

/**
 * Created by feaoes on 2017/11/16.
 */

public class User {

    private int id;
    private String docId;
    private String username;
    private String password;
    private String md5Password;
    private String deviceUUID;

    private boolean isLogin;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMd5Password() {
        return md5Password;
    }

    public void setMd5Password(String md5Password) {
        this.md5Password = md5Password;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", docId='" + docId + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", md5Password='" + md5Password + '\'' +
                ", deviceUUID='" + deviceUUID + '\'' +
                ", isLogin=" + isLogin +
                '}';
    }
}
