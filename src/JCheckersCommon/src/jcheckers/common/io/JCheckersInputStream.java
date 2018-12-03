package jcheckers.common.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import jcheckers.common.util.CryptUtil;

/**
 * 
 * @author SHARIVAN
 * 
 */

public class JCheckersInputStream extends FilterInputStream {

	private static void readFully(InputStream in, byte[] buf, int off, int len) throws JCheckersIOException {
		int k;
		for (int i = 0; i < len; i += k) {
			try {
				k = in.read(buf, i, len - i);
			} catch (IOException e) {
				throw new JCheckersIOException(e);
			}

			if (k == -1)
				throw new JCheckersEOFException();
		}
	}

	private static int readShort(InputStream in) throws JCheckersIOException {
		int i1;
		try {
			i1 = in.read();
		} catch (IOException e) {
			throw new JCheckersIOException(e);
		}
		int i2;
		try {
			i2 = in.read();
		} catch (IOException e) {
			throw new JCheckersIOException(e);
		}
		if ((i1 | i2) < 0)
			throw new JCheckersEOFException();

		return i1 | i2 << 8;
	}

	private int game;
	private byte[] key;
	private byte[] blockBuf;
	private ByteArrayInputStream block;
	private ByteArrayInputStream total;

	private int method = 3;

	private boolean noDecryptNextBlock;

	public JCheckersInputStream(InputStream in, int game) {
		this(in, game, null);
	}

	public JCheckersInputStream(InputStream in, int game, byte[] key) {
		super(in);

		this.game = game;
		this.key = key;

		noDecryptNextBlock = false;
		block = null;
		total = null;
	}

	@Override
	public int available() throws JCheckersIOException {
		if (block == null)
			readBlock();

		return block.available();
	}

	public int getGame() {
		return game;
	}

	public byte[] getKey() {
		return key;
	}

	public int getMethod() {
		return method;
	}

	public void noDecryptNextBlock() {
		noDecryptNextBlock = true;
	}

	private void parseHeader() throws JCheckersIOException {
		int i;
		try {
			i = in.read();
		} catch (IOException e) {
			throwIOException(e);
			return;
		}

		if (i < 0)
			throw new JCheckersEOFException();

		if (i != game)
			throw new InvalidHeader(game, i);

		try {
			i = in.read();
		} catch (IOException e) {
			throwIOException(e);
		}

		if (i < 0)
			throw new JCheckersEOFException();
	}

	@Override
	public int read() throws JCheckersIOException {
		if (block == null || block.available() == 0)
			readBlock();

		return block.read();
	}

	@Override
	public int read(byte buf[], int off, int len) throws JCheckersIOException {
		if (block == null || block.available() == 0)
			readBlock();

		return block.read(buf, off, len);
	}

	private void readBlock() throws JCheckersIOException {
		if (total == null || total.available() == 0)
			readTotal();

		int len = readShort(total);
		blockBuf = new byte[len];

		readFully(total, blockBuf, 0, len);

		if (!noDecryptNextBlock)
			switch (method) {
				case 1:
					CryptUtil.crypt1(blockBuf, 0, len);
					break;
				case 2:
					CryptUtil.crypt2(blockBuf, 0, len);
					break;
				case 3:
					CryptUtil.crypt3(blockBuf, 0, len);
					break;
				case 4:
					CryptUtil.crypt4(blockBuf, 0, len);
					break;
				case 5:
					CryptUtil.crypt5(blockBuf, 0, len, key);
					break;
			}
		else
			noDecryptNextBlock = false;

		block = new ByteArrayInputStream(blockBuf);
	}

	public byte[] readEntireBlock() throws JCheckersIOException {
		if (block == null || block.available() == 0)
			readBlock();

		byte[] buf = new byte[block.available()];
		readFully(block, buf, 0, buf.length);

		return buf;
	}

	private void readTotal() throws JCheckersIOException {
		parseHeader();

		int len = readShort(in);
		byte[] buf = new byte[len];

		readFully(in, buf, 0, len);

		total = new ByteArrayInputStream(buf);
		block = null;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public void skip() {
		block.skip(block.available());
	}

	@Override
	public long skip(long n) throws JCheckersIOException {
		if (block == null || block.available() == 0)
			readBlock();

		return block.skip(n);
	}

	public void throwIOException() throws JCheckersIOException {
		throw new JCheckersIOException(blockBuf);
	}

	public void throwIOException(String message) throws JCheckersIOException {
		throw new JCheckersIOException(blockBuf, message);
	}

	public void throwIOException(String message, Throwable cause) throws JCheckersIOException {
		throw new JCheckersIOException(blockBuf, message, cause);
	}

	public void throwIOException(Throwable cause) throws JCheckersIOException {
		throw new JCheckersIOException(blockBuf, cause);
	}

}
