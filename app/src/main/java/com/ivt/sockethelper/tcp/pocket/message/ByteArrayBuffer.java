package com.ivt.sockethelper.tcp.pocket.message;

/**
 * Created by panzongnan on 2017/4/19.
 */
public class ByteArrayBuffer {
	private byte[] buffer;
	private int len;

	public ByteArrayBuffer(int capacity) {
		super();
		if (capacity < 0) {
			throw new IllegalArgumentException("Buffer capacity may not be negative");
		}
		this.buffer = new byte[capacity];
	}

	private void expand(int newlen) {
		byte newbuffer[] = new byte[Math.max(this.buffer.length << 1, newlen)];
		System.arraycopy(this.buffer, 0, newbuffer, 0, this.len);
		this.buffer = newbuffer;
	}

	public void append(final byte[] b, int off, int len) {
		if (b == null) {
			return;
		}
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0)
				|| ((off + len) > b.length)) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return;
		}
		int newlen = this.len + len;
		if (newlen > this.buffer.length) {
			expand(newlen);
		}
		System.arraycopy(b, off, this.buffer, this.len, len);
		this.len = newlen;
	}

	public void insert(final byte[] b, int pos, int off, int len) {
		int moveLen = this.len - pos;
		if (b == null) {
			return;
		}
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0)
				|| ((off + len) > b.length) || moveLen < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return;
		}
		byte[] moveBytes = null;
		if (moveLen > 0) {
			moveBytes = new byte[moveLen];
			System.arraycopy(this.buffer, pos, moveBytes, 0, moveLen);
		}

		int newlen = this.len + len;
		if (newlen > this.buffer.length) {
			expand(newlen);
		}
		System.arraycopy(b, off, this.buffer, pos, len);
		if (moveLen > 0) {
			System.arraycopy(moveBytes, 0, this.buffer, pos + len, moveLen);
		}
		this.len = newlen;
	}

	public void copyValues(final byte[] b, int pos, int off, int len) {
		if (b == null) {
			return;
		}
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0)
				|| ((off + len) > b.length)) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return;
		}

		int newlen = Math.max(pos + len, this.len);
		if (newlen > this.buffer.length) {
			expand(newlen);
		}
		System.arraycopy(b, off, this.buffer, pos, len);
		this.len = newlen;
	}

	public void append(int b) {
		int newlen = this.len + 1;
		if (newlen > this.buffer.length) {
			expand(newlen);
		}
		this.buffer[this.len] = (byte) b;
		this.len = newlen;
	}

	public void append(final char[] b, int off, int len) {
		if (b == null) {
			return;
		}
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) < 0)
				|| ((off + len) > b.length)) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return;
		}
		int oldlen = this.len;
		int newlen = oldlen + len;
		if (newlen > this.buffer.length) {
			expand(newlen);
		}
		for (int i1 = off, i2 = oldlen; i2 < newlen; i1++, i2++) {
			this.buffer[i2] = (byte) b[i1];
		}
		this.len = newlen;
	}
	public void clear() {
		this.len = 0;
	}

	public byte[] toByteArray() {
		byte[] b = new byte[this.len];
		if (this.len > 0) {
			System.arraycopy(this.buffer, 0, b, 0, this.len);
		}
		return b;
	}

	public int byteAt(int i) {
		if (i < 0 || i >= length()) {
//			throw new IndexOutOfBoundsException();
			return -1;
		}
		return (this.buffer[i] & 0xFF);
	}
	public int capacity() {
		return this.buffer.length;
	}
	public int length() {
		return this.len;
	}

	public byte[] buffer() {
		return this.buffer;
	}

	public void setLength(int len) {
		if (len < 0 || len > this.buffer.length) {
			throw new IndexOutOfBoundsException();
		}
		this.len = len;
	}

	public boolean isEmpty() {
		return this.len == 0;
	}

	public boolean isFull() {
		return this.len == this.buffer.length;
	}

	public void setByte(int location, int value) {
		if (location < 0 || value < 0) {
			throw new IllegalArgumentException();
		}
		if (location >= this.buffer.length) {
			expand(location);
		}
		this.buffer[location] = (byte) (value & 0xff);
		if (location >= this.len) {
			this.len = location + 1;
		}
	}
}
