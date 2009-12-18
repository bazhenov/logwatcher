package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;

/**
 * Matcher по дате возникновения ошибки.
 *
 * Может фильтровать как по одной дате так и по диапазону дат. В случае если фильтрация
 * происходит по диапазону дат, то начальная дата исключается из поискового диапазона.
 */
public class DateMatcher implements LogEntryMatcher {

	private final Date from;
	private final Date to;

	public DateMatcher(Date date) {
		this.to = this.from = date;
	}

	/**
	 * Создает matcher для фильтрации по диапазону дат. Если дата выражаемая первым аргументом
	 * больше чем дата выражаемая вторым, то даты будут поменяны местами. Если переданные даты
	 * одинаковы - это семантически эквивалентно использованию конструктора
	 * {@link DateMatcher#DateMatcher(Date)}.
	 * @param from начало диапазона дат (исключается из диапазона поиска)
	 * @param to конец диапазона дат
	 */
	public DateMatcher(Date from, Date to) {
		if ( from.greaterThan(to) ) {
			this.to = from;
			this.from = to;
		}else{
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

	public boolean isMatch(AggregatedLogEntry entry) {
		Date date = entry.getLastTime().getDate();
		if ( from.equals(to) ) {
			return date.equals(from);
		}else{
			return date.greaterThan(from) && date.lessOrEqualThan(to);
		}
	}

	public boolean isMatch(LogEntry entry) {
		Date date = entry.getDate().getDate();
		if ( from.equals(to) ) {
			return date.equals(from);
		}else{
			return date.greaterThan(from) && date.lessOrEqualThan(to);
		}
	}
}
