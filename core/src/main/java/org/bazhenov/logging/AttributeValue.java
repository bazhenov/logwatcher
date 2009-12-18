package org.bazhenov.logging;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AttributeValue - это wrapper класс для группированного значения аттрибута.
 * Этот класс описывает пару: значение аттрибута + количество его вхождений.
 */
public class AttributeValue {

	private final String value;
	private final AtomicInteger count;

	public AttributeValue(String value, int count) {
		if ( value == null ) {
			throw new NullPointerException("Attribute value can not be null");
		}
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

	/**
	 * Добавляет переданный AttributeValue к текущему. Добавление подразумевает сложение
	 * количества вхождений.
	 * <code>
	 * AttributeValue v1 = new AttributeValue("foo", 12);
	 * AttributeValue v2 = new AttributeValue("foo", 15);
	 *
	 * v1.add(v2);
	 * v1.getCount(); // 27
	 * </code>
	 * Данный метод меняет исходный AttributeValue, но тем не менее этот метод thread safe.
	 * @param value добавляемое AttributeValue
	 */
	public void add(AttributeValue value) {
		count.addAndGet(value.getCount());
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

		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return "[" + getValue() + ":" + getCount() + "]";
	}
}
