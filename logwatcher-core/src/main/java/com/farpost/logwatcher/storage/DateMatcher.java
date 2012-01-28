package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.LogEntry;
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
	private final LocalDate from;
	private final LocalDate to;

	public DateMatcher(LocalDate date) {
		checkNotNull(date, "Date must not be null");
		interval = date.toInterval();
		from = date;
		to = date;
	}

	/**
	 * Создает matcher для фильтрации по диапазону дат. Если дата выражаемая первым аргументом
	 * больше чем дата выражаемая вторым, то даты будут поменяны местами. Если переданные даты
	 * одинаковы - это семантически эквивалентно использованию конструктора
	 * {@link DateMatcher#DateMatcher(LocalDate)}.
	 *
	 * @param from начало диапазона дат (исключается из диапазона поиска)
	 * @param to	 конец диапазона дат
	 */
	public DateMatcher(LocalDate from, LocalDate to) {
		checkNotNull(from, "From date must not be null");
		checkNotNull(to, "To date must not be null");

		this.from = from;
		this.to = to;

		if (from.equals(to)) {
			interval = from.toInterval();
		} else if (from.isAfter(to)) {
			interval = new Interval(to.plusDays(1).toDateTimeAtStartOfDay(), from.plusDays(1).toDateTimeAtStartOfDay());
		} else {
			interval = new Interval(from.plusDays(1).toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay());
		}
	}

	public LocalDate getDateFrom() {
		return from;
	}

	public LocalDate getDateTo() {
		return to;
	}

	public boolean isMatch(LogEntry entry) {
		return interval.contains(entry.getDate());
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

		if (interval != null ? !interval.equals(that.interval) : that.interval != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return interval != null ? interval.hashCode() : 0;
	}
}
