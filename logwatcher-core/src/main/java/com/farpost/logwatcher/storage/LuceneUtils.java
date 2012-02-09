package com.farpost.logwatcher.storage;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.joda.time.LocalDate;
import org.joda.time.ReadableDateTime;

import static java.lang.String.format;

class LuceneUtils {

	static NumericField numeric(String name, long value) {
		return new NumericField(name, Field.Store.NO, true).setLongValue(value);
	}

	static Field term(String name, String value) {
		return new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
	}

	static Field text(String name, String value) {
		return new Field(name, value, Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
	}

	static String normalize(String term) {
		return term.toLowerCase().trim();
	}

	static String normalizeDate(ReadableDateTime date) {
		return normalizeDate(date.toDateTime().toLocalDate());
	}

	/**
	 * Для заданной даты возвращает строку в формате YYYYMMDD. Например, "20110118"
	 *
	 * @param date дата
	 * @return строковое представление даты в формате без разделителей
	 */
	static String normalizeDate(LocalDate date) {
		return format("%d%02d%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
	}
}
