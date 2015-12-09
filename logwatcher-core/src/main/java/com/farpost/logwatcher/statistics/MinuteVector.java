package com.farpost.logwatcher.statistics;

import com.google.common.primitives.Longs;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.google.common.base.Preconditions.*;

public final class MinuteVector {

	public static final int SIZE = 1440;
	/**
	 * Circular buffer of long values each describing the count of events in particular minute of the day.
	 * <p/>
	 * Each value consist of day marker (2 high order bytes) and event count (6 low order bytes).
	 */
	private final long[] v;
	private static final DateTime zeroPoint = new DateTime(2000, 1, 1, 0, 0);

	public MinuteVector(long[] v) {
		checkArgument(v.length == SIZE);
		this.v = checkNotNull(v);
	}

	public MinuteVector() {
		this(new long[SIZE]);
	}

	public MinuteVector(byte[] raw) {
		this.v = reconstructFromByteArray(raw);
	}

	private static long[] reconstructFromByteArray(byte[] raw) {
		checkArgument(raw.length == SIZE * 8);
		ByteArrayInputStream is = new ByteArrayInputStream(raw);
		byte[] b = new byte[8];
		long[] v = new long[SIZE];
		try {
			for (int i = 0; i < SIZE; i++) {
				checkState(is.read(b) == 8);
				v[i] = Longs.fromByteArray(b);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return v;
	}

	public void increment(DateTime dateTime) {
		increment(dateTime, 1);
	}

	public void increment(DateTime dateTime, int delta) {
		int offset = dateTime.getMinuteOfDay();
		long diffInDays = diffInDays(dateTime);
		if (dayMarker(v[offset]) == diffInDays) {
			v[offset] += delta;
		} else if (diffInDays > dayMarker(v[offset])) {
			v[offset] = buildValue(diffInDays, 1);
		}
	}

	public long get(int offset) {
		DateTime now = DateTime.now();
		offset = index(offset, now);
		if (dayMarker(v[offset]) == diffInDays(now)) {
			return v[offset] & 0xFFFFFFFFFFFFL;
		}
		return 0;
	}

	private static int index(int offset, DateTime now) {
		int index = now.getMinuteOfDay() + offset;
		return index < 0 ? SIZE + index : index;
	}

	/**
	 * @param dateTime the arbitrary point in time
	 * @return the number of full days from 1 January, 2000.
	 */
	private static long diffInDays(DateTime dateTime) {
		long diffInDays = (dateTime.getMillis() - zeroPoint.getMillis()) / (1000 * 86400);
		checkState(diffInDays >= 0 && diffInDays <= Short.MAX_VALUE);
		return diffInDays;
	}

	private static long buildValue(long diffInDays, int sdf) {
		return (diffInDays << 48) | sdf;
	}

	private static long dayMarker(long value) {
		return value >> 48;
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			for (long l : v)
				os.write(Longs.toByteArray(l));
			return os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MinuteVector)) return false;
		MinuteVector that = (MinuteVector) o;
		return Arrays.equals(v, that.v);

	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(v);
	}
}
