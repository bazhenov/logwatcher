package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
import com.farpost.timepoint.Date;

/**
 * Matcher по дате возникновения ошибки.
 * <p/>
 * Может фильтровать как по одной дате так и по диапазону дат. В случае если фильтрация
 * происходит по диапазону дат, то начальная дата исключается из поискового диапазона.
 */
public class DateMatcher implements LogEntryMatcher {

	private final Date from;
	private final Date to;

	public DateMatcher(Date date) {
		if (date == null) {
			throw new NullPointerException("Date must not be null");
		}
		this.to = this.from = date;
	}

	/**
	 * Создает matcher для фильтрации по диапазону дат. Если дата выражаемая первым аргументом
	 * больше чем дата выражаемая вторым, то даты будут поменяны местами. Если переданные даты
	 * одинаковы - это семантически эквивалентно использованию конструктора
	 * {@link DateMatcher#DateMatcher(Date)}.
	 *
	 * @param from начало диапазона дат (исключается из диапазона поиска)
	 * @param to	 конец диапазона дат
	 */
	public DateMatcher(Date from, Date to) {
		if (from == null || to == null) {
			throw new NullPointerException("Dates must not be null");
		}
		if (from.greaterThan(to)) {
			this.to = from;
			this.from = to;
		} else {
			this.from = from;
			this.to = to;
		}
	}

	public Date getDateFrom() {
		return from;
	}

	public Date getDateTo() {
		return to;
	}

	public boolean isMatch(LogEntry entry) {
		Date date = entry.getDate().getDate();
		if (from.equals(to)) {
			return date.equals(from);
		} else {
			return date.greaterThan(from) && date.lessOrEqualThan(to);
		}
	}

	@Override
	public String toString() {
		if (from.lessThan(to)) {
			return "occured:" + from + "/" + to;
		} else {
			return "occured:" + from;
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
