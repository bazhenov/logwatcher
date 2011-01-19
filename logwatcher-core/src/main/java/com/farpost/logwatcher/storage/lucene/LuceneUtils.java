package com.farpost.logwatcher.storage.lucene;

import com.farpost.timepoint.Date;
import org.apache.lucene.document.Field;

class LuceneUtils {

	static Field term(String name, String value) {
		return new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
	}

	static Field storedTerm(String name, String value) {
		return new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
	}

	static String normalizeTerm(String term) {
		return term.toLowerCase().trim();
	}

	/**
	 * Для заданной даты возвращает строку в формате YYYYMMDD. Например, "20110118"
	 *
	 * @param date дата
	 * @return строковое представление даты в формате без разделителей
	 */
	static String normilizeDate(Date date) {
		return String.format("%d%02d%02d", date.getYear(), date.getMonth(), date.getDay());
	}
}
