package com.farpost.logwatcher.storage;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.joda.time.LocalDate;

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

	/**
	 * Для заданной даты возвращает ее timestamp
	 *
	 * @param date дата
	 * @return timestamp даты
	 */
	static long normalizeDate(LocalDate date) {
		return date.toDateTimeAtStartOfDay().getMillis();
	}
}
