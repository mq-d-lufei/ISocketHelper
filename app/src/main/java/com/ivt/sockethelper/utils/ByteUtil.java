package com.ivt.sockethelper.utils;

/**
 * Java ： 高字节前-->>低字节后
 * C++  :  低字节前-->>高字节后
 */

/**
 * short  十     12 & 0xFF
 * 二     0000 0000 0000 1100
 * &      0000 0000 1111 1111
 * =      0000 0000 0000 1100
 */

public class ByteUtil {


    public static void main(String[] args) {
        int x = 0xA77A;

        byte[] b = intToBytesHeight(x);
        int i1 = byteToIntHeight(b);
        byte[] b2 = intToBytesLow(x);
        int i2 = byteToIntLow(b2);
        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + "*");
        }
        System.out.println("--------------------b:--" + b.length + "---i1:" + i1);
        for (int i = 0; i < b2.length; i++) {
            System.out.print(b2[i] + "*");
        }
        System.out.println("--------------------b2:--" + b2.length + "--i2:" + i2);


        /**
         * short to byte[]
         */
        short headerData = 12659;

        System.out.println("-----------short to byte-----------");

        byte[] bytes = shortToBytesHeight(headerData);
        short height = byteToShortHeight(bytes);
        byte[] bytes1 = shortToBytesLow(headerData);
        short low = byteToShortLow(bytes1);

        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i] + "*");
        }
        System.out.println("--------------------bytes:--" + bytes.length + "---height:" + height);
        for (int i = 0; i < bytes1.length; i++) {
            System.out.print(bytes1[i] + "*");
        }
        System.out.println("--------------------bytes1:--" + bytes1.length + "--low:" + low);


        int bb = Integer.parseInt("BB", 16);
        System.out.println("------bb------" + bb);
        System.out.println("------bb------" + (byte)bb);


    }


    /**
     * 转换short为byte
     * 号位在前，低位在后
     *
     * @param s 需要转换的short
     * @return byte[] 转换后的字节数组
     */
    public static byte[] shortToBytesHeight(short s) {
        byte[] shortToBytes = new byte[2];
        shortToBytes[0] = (byte) (s >> 8);
        shortToBytes[1] = (byte) s;
        return shortToBytes;
    }

    public static short byteToShortHeight(byte[] b) {
        return (short) (((b[0] << 8) | b[1] & 0xff));
    }

    /**
     * 低字节前，高字节后
     */
    public static byte[] shortToBytesLow(short s) {
        byte[] data = new byte[2];
        data[0] = (byte) s;
        data[1] = (byte) (s >> 8);
        return data;
    }

    public static short byteToShortLow(byte[] b) {
        return (short) (((b[1] << 8) | b[0] & 0xff));
    }


    /**
     * 转换int为byte数组
     * 高字节前 低字节后
     *
     * @param
     */
    public static byte[] intToBytesHeight(int packetLength) {
        byte[] data = new byte[4];
        data[3] = (byte) (packetLength & 0xFF);
        data[2] = (byte) ((packetLength >> 8) & 0xFF);
        data[1] = (byte) ((packetLength >> 16) & 0xFF);
        data[0] = (byte) ((packetLength >> 24) & 0xFF);
        return data;
    }

    public static int byteToIntHeight(byte[] data) {
        return (data[3] & 0xFF) + ((data[2] & 0xFF) << 8) + ((data[1] & 0xFF) << 16) + ((data[0] & 0xFF) << 24);
    }

    /**
     * 转换int为byte数组
     * 低字节前 高址节后
     *
     * @param x
     */
    public static byte[] intToBytesLow(int x) {
        byte[] data = new byte[4];
        data[3] = (byte) (x >> 24);
        data[2] = (byte) (x >> 16);
        data[1] = (byte) (x >> 8);
        data[0] = (byte) x;
        return data;
    }

    public static int byteToIntLow(byte[] data) {
        return ((data[3] & 0xff) << 24)
                | ((data[+2] & 0xff) << 16)
                | ((data[+1] & 0xff) << 8) | ((data[0] & 0xff));
    }


    /**************************************/


    /**
     * 转换short为byte
     *
     * @param b
     * @param s     需要转换的short
     * @param index
     */
    public static void putShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
     * @return
     */
    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }

    /**
     * 转换int为byte数组
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void putInt(byte[] bb, int x, int index) {
        bb[index + 3] = (byte) (x >> 24);
        bb[index + 2] = (byte) (x >> 16);
        bb[index + 1] = (byte) (x >> 8);
        bb[index + 0] = (byte) (x >> 0);
    }

    /**
     * 通过byte数组取到int
     *
     * @param bb
     * @param index 第几位开始
     * @return
     */
    public static int getInt(byte[] bb, int index) {
        return (int) ((((bb[index + 3] & 0xff) << 24)
                | ((bb[index + 2] & 0xff) << 16)
                | ((bb[index + 1] & 0xff) << 8) | ((bb[index + 0] & 0xff) << 0)));
    }

    /**
     * 转换long型为byte数组
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void putLong(byte[] bb, long x, int index) {
        bb[index + 7] = (byte) (x >> 56);
        bb[index + 6] = (byte) (x >> 48);
        bb[index + 5] = (byte) (x >> 40);
        bb[index + 4] = (byte) (x >> 32);
        bb[index + 3] = (byte) (x >> 24);
        bb[index + 2] = (byte) (x >> 16);
        bb[index + 1] = (byte) (x >> 8);
        bb[index + 0] = (byte) (x >> 0);
    }

    /**
     * 通过byte数组取到long
     *
     * @param bb
     * @param index
     * @return
     */
    public static long getLong(byte[] bb, int index) {
        return ((((long) bb[index + 7] & 0xff) << 56)
                | (((long) bb[index + 6] & 0xff) << 48)
                | (((long) bb[index + 5] & 0xff) << 40)
                | (((long) bb[index + 4] & 0xff) << 32)
                | (((long) bb[index + 3] & 0xff) << 24)
                | (((long) bb[index + 2] & 0xff) << 16)
                | (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
    }

    /**
     * 字符到字节转换
     *
     * @param ch
     * @return
     */
    public static void putChar(byte[] bb, char ch, int index) {
        int temp = (int) ch;
        // byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            bb[index + i] = new Integer(temp & 0xff).byteValue(); // 将最高位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
    }

    /**
     * 字节到字符转换
     *
     * @param b
     * @return
     */
    public static char getChar(byte[] b, int index) {
        int s = 0;
        if (b[index + 1] > 0)
            s += b[index + 1];
        else
            s += 256 + b[index + 0];
        s *= 256;
        if (b[index + 0] > 0)
            s += b[index + 1];
        else
            s += 256 + b[index + 0];
        char ch = (char) s;
        return ch;
    }

    /**
     * float转换byte
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void putFloat(byte[] bb, float x, int index) {
        // byte[] b = new byte[4];
        int l = Float.floatToIntBits(x);
        for (int i = 0; i < 4; i++) {
            bb[index + i] = new Integer(l).byteValue();
            l = l >> 8;
        }
    }

    /**
     * 通过byte数组取得float
     *
     * @param b
     * @param index
     * @return
     */
    public static float getFloat(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    /**
     * double转换byte
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void putDouble(byte[] bb, double x, int index) {
        // byte[] b = new byte[8];
        long l = Double.doubleToLongBits(x);
        for (int i = 0; i < 4; i++) {
            bb[index + i] = new Long(l).byteValue();
            l = l >> 8;
        }
    }

    /**
     * 通过byte数组取得float
     *
     * @param b
     * @param index
     * @return
     */
    public static double getDouble(byte[] b, int index) {
        long l;
        l = b[0];
        l &= 0xff;
        l |= ((long) b[1] << 8);
        l &= 0xffff;
        l |= ((long) b[2] << 16);
        l &= 0xffffff;
        l |= ((long) b[3] << 24);
        l &= 0xffffffffl;
        l |= ((long) b[4] << 32);
        l &= 0xffffffffffl;
        l |= ((long) b[5] << 40);
        l &= 0xffffffffffffl;
        l |= ((long) b[6] << 48);
        l &= 0xffffffffffffffl;
        l |= ((long) b[7] << 56);
        return Double.longBitsToDouble(l);
    }
}
