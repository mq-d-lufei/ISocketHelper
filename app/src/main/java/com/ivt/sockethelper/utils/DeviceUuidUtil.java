package com.ivt.sockethelper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * 获得设备的唯一ID
 * 2015-10-26
 */
public class DeviceUuidUtil {
	protected static final String PREFS_FILE      = "device_id.xml";
	protected static final String PREFS_DEVICE_ID = "device_id";

	protected static UUID uuid;

	public DeviceUuidUtil(Context context) {

		if (uuid == null) {
			synchronized (DeviceUuidUtil.class) {
				if (uuid == null) {
					final SharedPreferences prefs = context
							.getSharedPreferences(PREFS_FILE, 0);
					final String id = prefs.getString(PREFS_DEVICE_ID, null);

					if (id != null) {
						uuid = UUID.fromString(id);

					} else {

						final String androidId = Secure
								.getString(context.getContentResolver(),
										Secure.ANDROID_ID);
						try {
							if (!"9774d56d682e549c".equals(androidId)) {
								uuid = UUID.nameUUIDFromBytes(androidId
										.getBytes("utf8"));
							} else {
								final String deviceId = ((TelephonyManager) context
										.getSystemService(Context.TELEPHONY_SERVICE))
										.getDeviceId();
								uuid = deviceId != null ? UUID
										.nameUUIDFromBytes(deviceId
												.getBytes("utf8")) : UUID
										.randomUUID();
							}
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
						prefs.edit()
								.putString(PREFS_DEVICE_ID, uuid.toString())
								.commit();
					}

				}
			}
		}

	}

	public String getDeviceUuid() {
		return uuid.toString();
	}
}