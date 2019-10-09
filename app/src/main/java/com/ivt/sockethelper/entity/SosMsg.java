package com.ivt.sockethelper.entity;

/**
 * Created by feaoes on 2017/9/22.
 */

public class SosMsg {


    private int id;
    private String text;
    private byte Type;

    public SosMsg() {
    }

    public SosMsg(int id, String text, byte type) {
        this.id = id;
        this.text = text;
        Type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte getType() {
        return Type;
    }

    public void setType(byte type) {
        Type = type;
    }
}
