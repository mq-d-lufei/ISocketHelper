package com.ivt.sockethelper.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 需求的MD5加密过程: 
 * 需将密码由utf-8编码转换为unicode字节数组，再进行MD5编码，
 * 最后将加密后的字节数组转换为utf-8编码的字符串，字母需大写。
 * 
 * @author IVT
 */
public class MD5 {

	/**
	 * 获取MD5加密值
	 * @param pwd
	 * @return
	 */
	public static String getMD5(String pwd) {
		StringBuffer  buf = null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] pwd_bytes = toUnicode2LE(pwd);
			md.update(pwd_bytes);

			byte[] pwd_md = md.digest();

			buf = new StringBuffer("");
			int i;
			for (int offset = 0; offset < pwd_md.length; offset++) {
				i = pwd_md[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return buf.toString().toUpperCase();
	}

	/**
	 * Unicode To LE
	 * @param str
	 * @return
	 */
	public static byte[] toUnicode2LE(String str) {
		if (null == str) {
			return null;
		} else if (str.length() <= 0) {
			return new byte[1];
		}

		byte[] bytes = null;
		try {
			byte[] ucs = str.getBytes("UNICODE");

			int offset = 0;
			if (((ucs[0] == (byte) 0xFF) && (ucs[1] == (byte) 0xFE))
					|| ((ucs[0] == (byte) 0xFE) && (ucs[1] == (byte) 0xFF))) {
				offset += 2;
			}

			int len = ucs.length - offset;
			bytes = new byte[len];
			for (int i = 0; i < len; i++) {
				bytes[i] = ucs[i + offset];
			}

			byte temp;
			// UCS-2 Big Endian
			if ((ucs[0] == (byte) 0xFE) && (ucs[1] == (byte) 0xFF)) {
				for (int i = 0; i < len / 2; i++) {
					temp = bytes[2 * i];
					bytes[2 * i] = bytes[2 * i + 1];
					bytes[2 * i + 1] = temp;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bytes;
	}

}
