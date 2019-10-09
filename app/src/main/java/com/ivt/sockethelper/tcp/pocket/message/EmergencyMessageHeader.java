package com.ivt.sockethelper.tcp.pocket.message;

import android.annotation.SuppressLint;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * TCP数据文件传输的请求头的封装
 * 
 * @author liuyuge 2015-10-19
 */
public class EmergencyMessageHeader {
	private static final String TAG = "EmergencyMessageHeader";
	private ByteArrayBuffer mBuffer;
	private EmergencyMessageHeaderFormat mFormat;
	private static final int BUFFER_CAPABILITY = 2048;
	private int[] mFieldsPosition;
	private int mCurrentResolvedPos = -1;

	public EmergencyMessageHeader(EmergencyMessageHeaderFormat format) {
		mFormat = format;
		mBuffer = new ByteArrayBuffer(BUFFER_CAPABILITY);
		int len = mFormat.mSequentialFields.size();
		mFieldsPosition = new int[len];
		for (int i = 0; i < len; i++) {
			mFieldsPosition[i] = -1;
		}
		computePosition();
	}

	/**
	 * 计算位置
	 */
	private void computePosition() {
		if (mFieldsPosition != null) {
			int len = mFieldsPosition.length;
			int width;
			for (int i = mCurrentResolvedPos + 1; i < len; i++) {
				if (mFieldsPosition[i] > 0) {
					continue;
				}

				if (i == 0) {
					mFieldsPosition[i] = 0;
					continue;
				}
				String field = mFormat.mSequentialFields.get(i - 1);
				if (field != null) {
					width = mFormat.getFieldWidth(field);
					if (width == EmergencyMessageHeaderFormat.NONDETERMINABLE_WIDTH) {
						String depend = mFormat.mFieldDependencies.get(field);
						if (depend != null) {
							width = getFieldValue(depend);
							if (width > 0) {
								mFieldsPosition[i] = mFieldsPosition[i - 1]
										+ width * 8;
								mCurrentResolvedPos = i;
								continue;
							}
						}
						return;
					} else if (width == -1) {
						return;
					}

					mFieldsPosition[i] = mFieldsPosition[i - 1] + width;
					mCurrentResolvedPos = i;
					continue;
				}
			}
		}
	}

	/**
	 * 设置待解析的数据
	 * 
	 * @param byteBuffer
	 */
	public void setBytes(byte[] byteBuffer) {
		if (!mBuffer.isEmpty()) {
			mBuffer.clear();
		}
		mBuffer.copyValues(byteBuffer, 0, 0, byteBuffer.length);
		computePosition();
	}

	/**
	 * 添加字段
	 * 
	 * @param field
	 * @param values
	 */
	public void packField(String field, byte[] values) {
		if (field == null || values == null) {
			throw new IllegalArgumentException(
					"field is invalid or byte array is null.");
		}

		int cbytes = values.length;
		int position = getFieldPosition(field);
		// 找不到位置
		if (position < 0) {
			throw new UnresolvedPositionException(
					"Can not resolve the position of field(" + field + ").");
		}
		position = (position + 7) / 8;
		int width = mFormat.getFieldWidth(field);
		if (width == EmergencyMessageHeaderFormat.NONDETERMINABLE_WIDTH) {
			String depend = mFormat.mFieldDependencies.get(field);
			if (depend != null) {
				int w = getFieldValue(depend);
				if (w == 0) {
					packField(depend, cbytes);
					w = cbytes;
				}
				cbytes = Math.min(w, cbytes);
				mBuffer.copyValues(values, position, 0, cbytes);
				/*
				 * as we now know the width of this field,update the position of
				 * the next field
				 */
				int idx = mFormat.mSequentialFields.indexOf(field);
				if (idx > 0 && idx < mFieldsPosition.length - 1) {
					mFieldsPosition[idx + 1] = (position + cbytes) * 8;
					computePosition();
				}
			}
		} else if (width < 0) {// 得不到占位的长度
			throw new UnresolveWidthException(
					"Can not resolve the width of field(" + field + ").");
		} else {
			mBuffer.copyValues(values, position, 0, cbytes);
			computePosition();
		}
	}

	/**
	 * 添加字段
	 * 
	 * @param field
	 * @param value
	 */
	public void packField(String field, int value) {
		if (field == null) {
			throw new IllegalArgumentException("field is invalid.");
		}
		int position = getFieldPosition(field);
		// 拿不到位置
		if (position < 0) {
			throw new UnresolvedPositionException(
					"Can not resolve the position of field(" + field + ").");
		}

		int width = mFormat.getFieldWidth(field);
		// 最多四个字节
		if (width > 32 || width < 0) {
			throw new UnresolveWidthException(
					"Can not resolve the width of field(" + field + ").");
		} else {
			value = value & 0x7FFFFFFF;
			int processBits = 8 - (position % 8);
			int headPos = position / 8;
			int headByte = headPos >= mBuffer.length() ? 0 : mBuffer
					.byteAt(headPos);
			if (processBits != 8) {
				headByte |= ((1 << processBits) - 1) & value;
				mBuffer.setByte(headPos, headByte & 0xff);
			} else {
				processBits = 0;
				headPos--;
			}
			while (width - processBits >= 8) {
				mBuffer.setByte(++headPos, (value >> processBits) & 0xff);
				processBits += 8;
			}
			if (width - processBits > 0) {
				int tailByte = (++headPos) >= mBuffer.length() ? 0 : mBuffer
						.byteAt(headPos);
				tailByte |= (((value >> processBits) & 0xff) << (width - processBits)) & 0xff;
				mBuffer.setByte(headPos, tailByte & 0xff);
			}
			computePosition();
			// 当一个字段的长度取决于另一个字段长度时 先去拿到另一个字段
			int len = mFieldsPosition.length;
			for (int i = mCurrentResolvedPos; i < len; i++) {
				String depended = (String) mFormat.mFieldDependencies
						.get(mFormat.mSequentialFields.get(i));
				if ((null != depended) && depended.equals(field)
						&& (0 == value) && (i + 1 < len)) {
					mFieldsPosition[i + 1] = mFieldsPosition[i];
					break;
				}
			}
		}
	}

	/**
	 * 获得指定的字段的值
	 * 
	 * @param fieldString
	 * @return
	 */
	public int getIntField(String fieldString) {
		if (fieldString == null) {
			return -1;
		}

		if (mBuffer == null || mBuffer.isEmpty()) {
			return -1;
		}

		int nValue = 0;

		int nPos = mFormat.mSequentialFields.indexOf(fieldString);
		int nWidth = mFormat.getFieldWidth(fieldString) / 8;

		if (nWidth == -1 || nPos == -1 || nPos > mFieldsPosition.length) {
			return -1;
		}

		int nIndex = mFieldsPosition[nPos] / 8;

		if (nIndex + nWidth > mBuffer.length()) {
			return -1;
		}

		switch (nWidth) {
		case 0x01:
			nValue = mBuffer.byteAt(nIndex) & 0xFF;
			break;
		case 0x02:
			nValue = mBuffer.byteAt(nIndex) & 0xFF;
			nValue += ((mBuffer.byteAt(nIndex + 1) & 0xFF) << 8);
			break;
		case 0x04:
			nValue = mBuffer.byteAt(nIndex) & 0xFF;
			nValue += ((mBuffer.byteAt(nIndex + 1) & 0xFF) << 8);
			nValue += ((mBuffer.byteAt(nIndex + 2) & 0xFF) << 16);
			nValue += ((mBuffer.byteAt(nIndex + 3) & 0xFF) << 24);
			break;
		default:
			break;
		}

		return nValue;
	}

	/**
	 * 获取指定字段的字节数组
	 * 
	 * @param fieldString
	 * @return
	 */
	public byte[] getByteArrayField(String fieldString) {
		if (fieldString == null) {
			return null;
		}

		if (mBuffer == null || mBuffer.isEmpty()) {
			return null;
		}

		int nPos = mFormat.mSequentialFields.indexOf(fieldString);
		int nWidth = mFormat.getFieldWidth(fieldString);

		if (nWidth == -1 || nPos == -1 || nPos > mFieldsPosition.length) {
			return null;
		}

		// int nLen = BUFFER_CAPABILITY > mBuffer.length()? BUFFER_CAPABILITY :
		// mBuffer.length();
		byte[] fieldArray = null; // new byte[nLen];

		int nIndex = mFieldsPosition[nPos] / 8;

		if (nWidth == EmergencyMessageHeaderFormat.NONDETERMINABLE_WIDTH) {
			String dependString = mFormat.mFieldDependencies.get(fieldString);
			if (dependString != null) {
				int nFieldLen = getFieldValue(dependString); // get field length
																// number
				if (nFieldLen > 0 && (nIndex + nFieldLen <= mBuffer.length())) {
					fieldArray = new byte[nFieldLen];
					System.arraycopy(mBuffer.buffer(), nIndex, fieldArray, 0,
							nFieldLen);
				}
			}
		} else if (nWidth > 0) {
			nWidth = nWidth / 8;
			fieldArray = new byte[nWidth];
			System.arraycopy(mBuffer.buffer(), nIndex, fieldArray, 0, nWidth);
		}

		return fieldArray;
	}

	public byte[] getBytes() {
		return mBuffer.toByteArray();
	}

	/**
	 * 清空数组
	 */
	public void clear() {
		if (!mBuffer.isEmpty()) {
			mBuffer.clear();
		}
		int len = mFormat.mSequentialFields.size();
		for (int i = 0; i < len; i++) {
			mFieldsPosition[i] = -1;
		}
		mCurrentResolvedPos = -1;
		computePosition();
	}

	public int getLength() {
		return mBuffer.length();
	}

	/**
	 * 获得依赖其他字段的值
	 * 
	 * @param depend
	 * @return
	 */
	private int getFieldValue(String depend) {
		int pos = getFieldPosition(depend);
		if (pos < 0) {
			return -1;
		}
		int width = mFormat.getFieldWidth(depend);
		if (width < 0 || width > 32) {
			return -1;
		}

		int head = 0;
		int body = 0;
		int tail = 0;
		int cb = 0;
		if (pos % 8 != 0) {
			cb = 8 - (pos % 8);
			head = (pos / 8 >= mBuffer.length()) ? 0 : mBuffer.byteAt(pos / 8);
			head &= ((1 << cb) - 1);
			width -= cb;
		}
		pos = ((pos + 7) / 8) * 8;

		int tailBits = width % 8;
		int bodyBytes = width / 8;
		int offset = pos / 8;
		while (bodyBytes > 0) {
			body += ((offset >= mBuffer.length() ? 0 : mBuffer.byteAt(offset)) << (offset * 8 - pos));
			bodyBytes--;
			offset++;
		}
		if (tailBits != 0) {
			tail = (offset >= mBuffer.length() ? 0 : mBuffer.byteAt(offset))
					& (0xff - ((1 << (8 - tailBits)) - 1));
		}
		return head + (body << cb) + (tail << (cb + (offset * 8 - pos)));
	}

	/**
	 * 获得字段位置
	 * 
	 * @param field
	 * @return
	 */
	private int getFieldPosition(String field) {
		int idx = mFormat.mSequentialFields.indexOf(field);
		if (idx < 0) {
			return -1;
		}
		if (idx == 0) {
			return 0;
		}
		if (mFieldsPosition[idx] > 0) {
				return mFieldsPosition[idx];
		}
		return -1;
	}

	public static class EmergencyMessageHeaderFormat {
		private             LinkedList<String>         mSequentialFields     = new LinkedList<String>();
		private             Hashtable<String, Integer> mFieldsWidth          = new Hashtable<String, Integer>();
		private             Hashtable<String, String>  mFieldDependencies    = new Hashtable<String, String>();
		public static final int                        NONDETERMINABLE_WIDTH = Integer.MAX_VALUE;

		public EmergencyMessageHeaderFormat() {
			if (mSequentialFields == null) {
				mSequentialFields = new LinkedList<String>();
			}
			mSequentialFields.clear();
			if (mFieldsWidth == null) {
				mFieldsWidth = new Hashtable<String, Integer>();
			}
			mFieldsWidth.clear();
			if (mFieldDependencies == null) {
				mFieldDependencies = new Hashtable<String, String>();
			}
			mFieldDependencies.clear();
		}

		/**
		 * 格式化包的规则 指定字段占得位置及长度
		 *
		 * @param name
		 * @return
		 */
		public static EmergencyMessageHeaderFormat makeFormat(final String name) {
			if (name == null) {
				return null;
			}

			EmergencyMessageHeaderFormat format = new EmergencyMessageHeaderFormat();
			if (name.equals(EmergencyMessageHeaderConst.HEADER_SEND_HEARTBEAT)) {
				format.addHeaderField(
						// header
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID,
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH);
			}
			// 用户验证
			else if (name.equals(EmergencyMessageHeaderConst.HEADER_USER_LOGIN)) {
				format.addHeaderField(
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN,
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN_LENGTH)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID,
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_USERNAME_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_USERNAME,
								EmergencyMessageHeaderConst.FIELD_USERNAME_LENGTH)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PASSWORD_MD5_DIGEST_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PASSWORD_MD5_DIGEST,
								EmergencyMessageHeaderConst.FIELD_PASSWORD_MD5_DIGEST_LENGTH);
			}
			// 数据包
			else if (name.equals(EmergencyMessageHeaderConst.HEADER_SEND_DATA)) {
				// publicRequestHeader(format);
				format.addHeaderField(
						// header
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16)
						.addHeaderField(
								// header
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN,
								EmergencyMessageHeaderConst.FIELD_DEVICETOKEN_LENGTH)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DOCID,
								EmergencyMessageHeaderConst.FIELD_DOCID_LENGTH)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_XML_DATA_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_XML_DATA,
								EmergencyMessageHeaderConst.FIELD_XML_DATA_LENGTH);

			}
			// 数据响应包
			else if (name
					.equals(EmergencyMessageHeaderConst.HEADER_RESPONSE_DATA)
					|| name.equals(EmergencyMessageHeaderConst.HEADER_RESPONSE_SERVICE)
					|| name.equals(EmergencyMessageHeaderConst.HEADER_RESPONSE_USER_LOGIN)) {

				format.addHeaderField(
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_ERROR_CODE, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_ERROR_DESCRIPTION_LENGTH,
								16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_ERROR_DESCRIPTION,
								EmergencyMessageHeaderConst.FIELD_ERROR_DESCRIPTION_LENGTH);
			}
			// 心跳响应包
			else if (name
					.equals(EmergencyMessageHeaderConst.HEADER_RESPONSE_HEARTBEAT)) {

				format.addHeaderField(
						// header
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16);

			}
			// 服务器主动推送包
			else if (name
					.equals(EmergencyMessageHeaderConst.HEADER_SERVICE_PUSH_NOTIFICATION)) {
				format.addHeaderField(
						EmergencyMessageHeaderConst.FIELD_IDENTIFIER, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKAGE_LENGTH,
								32)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_VERSION, 8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_UNUSED, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_TYPE,
								8)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_PACKET_ID, 16)
						.addHeaderField(
								EmergencyMessageHeaderConst.FIELD_DATA_LENGTH,
								32)
						.addHeaderField(EmergencyMessageHeaderConst.FIELD_DATA,
								EmergencyMessageHeaderConst.FIELD_DATA_LENGTH);

			}
			return format;
		}

		/**
		 * 添加字段
		 *
		 * @param name
		 * @param width
		 * @return
		 */
		@SuppressLint("UseValueOf")
		public EmergencyMessageHeaderFormat addHeaderField(String name,
														   int width) {
			if (width <= 0)
				return null;
			mFieldsWidth.put(name, new Integer(width));
			mSequentialFields.add(name);
			return this;
		}

		@SuppressLint("UseValueOf")
		public EmergencyMessageHeaderFormat addHeaderField(String name,
														   String dependField) {
			mFieldsWidth.put(name, new Integer(NONDETERMINABLE_WIDTH));
			mSequentialFields.add(name);
			mFieldDependencies.put(name, dependField);
			return this;
		}

		@SuppressLint("UseValueOf")
		public EmergencyMessageHeaderFormat insertHeaderField(String name,
															  int width, String afterWhat) {
			if (width <= 0)
				return null;
			int after = mSequentialFields.indexOf(afterWhat);
			if (after < 0) {
				return null;
			}
			mFieldsWidth.put(name, new Integer(width));
			mSequentialFields.add(after + 1, name);
			return this;
		}

		public int getFieldWidth(String name) {
			if (!mFieldsWidth.containsKey(name)) {
				return -1;
			}
			Integer value = mFieldsWidth.get(name);
			return value.intValue();
		}

		@SuppressLint("UseValueOf")
		public void setFieldWidth(String name, int width) {
			if (!mFieldsWidth.contains(name) || width <= 0) {
				return;
			}
			mFieldsWidth.put(name, new Integer(width));
		}
	}

	public class UnresolvedPositionException extends IllegalArgumentException {
		private static final long serialVersionUID = 2309363315936474275L;

		public UnresolvedPositionException() {
			super();
		}

		public UnresolvedPositionException(String s) {
			super(s);
		}
	}

	public class UnresolveWidthException extends IllegalArgumentException {
		private static final long serialVersionUID = 2158974570066193176L;

		public UnresolveWidthException() {
			super();
		}

		public UnresolveWidthException(String s) {
			super(s);
		}
	}
}
