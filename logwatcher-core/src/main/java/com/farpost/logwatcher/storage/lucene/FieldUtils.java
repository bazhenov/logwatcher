package com.farpost.logwatcher.storage.lucene;

import org.apache.lucene.document.Field;

class FieldUtils {

	static Field term(String name, String value) {
		return new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
	}

	static Field storedTerm(String name, String value) {
		return new Field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
	}
}
