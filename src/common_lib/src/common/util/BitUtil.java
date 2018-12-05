package common.util;

import java.util.Iterator;

public class BitUtil {

	private static class BitIterator implements Iterator<Integer> {

		int bits;
		boolean highToLow;

		int bitPos;
		boolean bit;

		BitIterator(int bits, boolean highToLow) {
			this.bits = bits;
			this.highToLow = highToLow;

			if (highToLow)
				bitPos = 31;
			else
				bitPos = 0;

			bit = false;
		}

		@Override
		public boolean hasNext() {
			return bits != 0;
		}

		@Override
		public Integer next() {
			while (bits != 0) {
				int result = nextBit();

				if (bit)
					return result;
			}

			return -1;
		}

		int nextBit() {
			bit = testBit(bitPos, bits);
			bits = clearBit(bitPos, bits);

			int result = bitPos;

			if (highToLow)
				bitPos--;
			else
				bitPos++;

			return result;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Unsuported method remove().");
		}

	}

	public static int bitCount(int bits) {
		int result = 0;
		for (int i = 0; i < 32; i++)
			if ((bits & 1 << i) != 0)
				result++;

		return result;
	}

	public static Iterable<Integer> bitIterable(final int bits) {
		return () -> bitIterator(bits);
	}

	public static Iterable<Integer> bitIterable(final int bits, final boolean highToLow) {
		return () -> bitIterator(bits, highToLow);
	}

	public static Iterator<Integer> bitIterator(int bits) {
		return bitIterator(bits, false);
	}

	public static Iterator<Integer> bitIterator(int bits, boolean highToLow) {
		return new BitIterator(bits, highToLow);
	}

	public static int clearBit(int bitPos, int bits) {
		return bits & ~(1 << bitPos);
	}

	public static int highBit(int bits) {
		return highBit(bits, 31, 0);
	}

	public static int highBit(int bits, int start, int end) {
		for (int i = start; i >= end; i--)
			if ((bits & 1 << i) != 0)
				return i;

		return -1;
	}

	public static int lowBit(int bits) {
		return lowBit(bits, 0, 31);
	}

	public static int lowBit(int bits, int start, int end) {
		for (int i = start; i <= end; i++)
			if ((bits & 1 << i) != 0)
				return i;

		return -1;
	}

	public static int setBit(int bitPos, boolean bit, int bits) {
		if (bit)
			return clearBit(bitPos, bits);

		return setBit(bitPos, bits);
	}

	public static int setBit(int bitPos, int bits) {
		return bits | 1 << bitPos;
	}

	public static boolean testBit(int bitPos, int bits) {
		return (bits & 1 << bitPos) != 0;
	}

	public static int toggleBit(int bitPos, int bits) {
		return bits ^ 1 << bitPos;
	}

}
