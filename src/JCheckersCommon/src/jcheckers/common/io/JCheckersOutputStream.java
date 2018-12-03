package jcheckers.common.io;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jcheckers.common.util.CryptUtil;

public class JCheckersOutputStream extends FilterOutputStream {

	private static int parseIP(String host) {
		int p = 0;
		int result = 0;
		for (int i = 0; i < 4; i++) {
			int p0 = p;
			p = host.indexOf('.', p0 + 1);
			String s = host.substring(p0, p - 1);
			result <<= 8;
			result |= Integer.parseInt(s);
		}
		return result;
	}

	private static void writeShort(OutputStream out, int value) throws IOException {
		out.write(value & 0xff);
		out.write(value >>> 8 & 0xff);
	}

	private int game;
	private byte[] key;

	private ByteArrayOutputStream block;
	private ByteArrayOutputStream total;
	private int method;
	private boolean noEncryptNextBlock;

	public JCheckersOutputStream(OutputStream out, int game) {
		this(out, game, null);
	}

	public JCheckersOutputStream(OutputStream out, int game, byte[] key) {
		super(out);

		this.game = game;
		this.key = key;

		method = 3;
		noEncryptNextBlock = false;

		block = new ByteArrayOutputStream();
		total = new ByteArrayOutputStream();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	public void emergencyOpen(String host, int port) throws IOException {
		out.write(port & 0x000000ff);
		out.write(port & 0x0000ff00);
		out.write(port & 0x00ff0000);
		out.write(port & 0xff000000);

		int h = parseIP(host);

		out.write(h & 0xff000000);
		out.write(h & 0x00ff0000);
		out.write(h & 0x0000ff00);
		out.write(h & 0x000000ff);

		out.flush();
	}

	@Override
	public void flush() throws IOException {
		if (block.size() > 0)
			flushBlock();

		out.write(game);
		out.write(1);
		writeShort(out, total.size());
		total.writeTo(out);
		total.reset();
		out.flush();
	}

	public void flushBlock() {
		byte[] buf = block.toByteArray();

		block.reset();
		if (!noEncryptNextBlock)
			switch (method) {
				case 1:
					CryptUtil.crypt1(buf, 0, buf.length);
					break;
				case 2:
					CryptUtil.crypt2(buf, 0, buf.length);
					break;
				case 3:
					CryptUtil.crypt3(buf, 0, buf.length);
					break;
				case 4:
					CryptUtil.crypt4(buf, 0, buf.length);
					break;
				case 5:
					CryptUtil.crypt5(buf, 0, buf.length, key);
					break;
			}
		else
			noEncryptNextBlock = false;

		try {
			writeShort(total, buf.length);
		} catch (IOException e) {
		}
		total.write(buf, 0, buf.length);
	}

	public byte[] getKey() {
		return key;
	}

	public int getMethod() {
		return method;
	}

	public void noEncryptNextBlock() {
		noEncryptNextBlock = true;
	}

	void sendBuff(JCheckersOutputStream stream, byte[] abyte0, int i, int j) throws IOException {
		stream.out.write(abyte0, i, j);
		stream.out.flush();
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	@Override
	public void write(byte[] buf) {
		write(buf, 0, buf.length);
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		block.write(buf, off, len);
	}

	@Override
	public void write(int value) {
		block.write(value);
	}

}
