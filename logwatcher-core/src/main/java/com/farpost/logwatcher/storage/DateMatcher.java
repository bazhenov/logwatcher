package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
import org.joda.time.DateTime;

/**
 * Matcher по дате возникновения ошибки.
 * <p/>
 * Может фильтровать как по одной дате так и по диапазону дат. В случае если фильтрация
 * происходит по диапазону дат, то начальная дата исключается из поискового диапазона.
 */
public class DateMatcher implements LogEntryMatcher {

	private final DateTime from;
	private final DateTime to;

	public DateMatcher(DateTime date) {
		if (date == null) {
			throw new NullPointerException("Date must not be null");
		}
		this.to = this.from = date;
	}

	/**
	 * Создает matcher для фильтрации по диапазону дат. Если дата выражаемая первым аргументом
	 * больше чем дата выражаемая вторым, то даты будут поменяны местами. Если переданные даты
	 * одинаковы - это семантически эквивалентно использованию конструктора
	 * {@link DateMatcher#DateMatcher(DateTime)}.
	 *
	 * @param from начало диапазона дат (исключается из диапазона поиска)
	 * @param to	 конец диапазона дат
	 */
	public DateMatcher(DateTime from, DateTime to) {
		if (from == null || to == null) {
			throw new NullPointerException("Dates must not be null");
		}
		if (from.isAfter(to)) {
			this.to = from;
			this.from = to;
		} else {
			this.from = from;
			this.to = to;
		}
	}

	public DateTime getDateFrom() {
		return from;
	}

	public DateTime getDateTo() {
		return to;
	}

	public boolean isMatch(LogEntry entry) {
		DateTime date = entry.getDate();
		if (from.equals(to)) {
			return date.equals(from);
		} else {
			return date.isAfter(from) && date.isBefore(to);
		}
	}

	@Override
	public String toString() {
		if (from.isBefore(to)) {
			return "occurred:" + from + "/" + to;
		} else {
			return "occurred:" + from;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DateMatcher that = (DateMatcher) o;
		return to.equals(that.to) && from.equals(that.from);
	}

	@Override
	public int hashCode() {
		return 31 * from.hashCode() + to.hashCode();
	}
}
