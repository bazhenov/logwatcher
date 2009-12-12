package org.bazhenov.logging;

import java.util.concurrent.atomic.AtomicInteger;

public class AttributeValue {

	private final String value;
	private final AtomicInteger count;

	public AttributeValue(String value, int count) {
		this.value = value;
		this.count = new AtomicInteger(count);
	}

	public String getValue() {
		return value;
	}

	public int getCount() {
		return count.get();
	}

	public void increment() {
		count.getAndIncrement();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AttributeValue that = (AttributeValue) o;

		return count.get() == that.count.get() && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		int result = value != null
			? value.hashCode()
			: 0;
		result = 31 * result + (count != null
			? count.hashCode()
			: 0);
		return result;
	}

	public String toString() {
		return "[" + getValue() + ":" + getCount() + "]";
	}
}
