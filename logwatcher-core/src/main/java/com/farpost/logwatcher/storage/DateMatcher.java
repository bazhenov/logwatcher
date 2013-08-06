package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matcher по дате возникновения ошибки.
 * <p/>
 * Может фильтровать как по одной дате так и по диапазону дат. В случае если фильтрация
 * происходит по диапазону дат, то начальная дата исключается из поискового диапазона.
 */
public class DateMatcher implements LogEntryMatcher {

	private final Interval interval;

	public DateMatcher(LocalDate date) {
		checkNotNull(date, "Date must not be null");
		interval = date.toInterval();
	}

	/**
	 * Создает matcher для фильтрации по диапазону дат. Если дата выражаемая первым аргументом
	 * больше чем дата выражаемая вторым, то даты будут поменяны местами. Если переданные даты
	 * одинаковы - это семантически эквивалентно использованию конструктора
	 * {@link DateMatcher#DateMatcher(LocalDate)}.
	 *
	 * @param from начало диапазона дат
	 * @param to   конец диапазона дат (исключается из диапазона поиска)
	 */
	public DateMatcher(LocalDate from, LocalDate to) {
		checkNotNull(from, "Date 'from' must not be null");
		checkNotNull(to, "Date 'to' must not be null");

		if (from.isEqual(to)) {
			interval = from.toInterval();
		} else if (from.isAfter(to)) {
			interval = new Interval(to.toDateTimeAtStartOfDay(), from.toDateTimeAtStartOfDay());
		} else {
			interval = new Interval(from.toDateTimeAtStartOfDay(), to.toDateTimeAtStartOfDay());
		}
	}

	public LocalDate getDateFrom() {
		return interval.getStart().toLocalDate();
	}

	public LocalDate getDateTo() {
		return interval.getEnd().toLocalDate();
	}

	public boolean isMatch(LogEntry entry) {
		return interval.contains(new DateTime(entry.getDate()));
	}

	@Override
	public String toString() {
		return "occurred:" + interval.getStart() + "/" + interval.getEnd();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DateMatcher that = (DateMatcher) o;

		return interval.equals(that.interval);

	}

	@Override
	public int hashCode() {
		return interval.hashCode();
	}
}
