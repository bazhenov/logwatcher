package com.farpost.logwatcher.storage;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;
import java.util.Date;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static org.apache.lucene.document.Field.Index.ANALYZED_NO_NORMS;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED_NO_NORMS;
import static org.apache.lucene.document.Field.Store;
import static org.joda.time.LocalDate.fromDateFields;

class LuceneUtils {

	static NumericField numeric(String name, long value) {
		return new NumericField(name, Store.NO, true).setLongValue(value);
	}

	static Field term(String name, String value) {
		return new Field(name, value, Store.NO, NOT_ANALYZED_NO_NORMS);
	}

	static Field text(String name, String value) {
		return new Field(name, value, Store.NO, ANALYZED_NO_NORMS);
	}

	static String normalize(@Nullable String term) {
		return nullToEmpty(term).toLowerCase().trim();
	}

	static String normalizeDate(Date date) {
		return normalizeDate(fromDateFields(date));
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
