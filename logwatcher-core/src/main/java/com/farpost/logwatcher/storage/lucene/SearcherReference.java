package com.farpost.logwatcher.storage.lucene;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;

/**
 * Tuple хранящий ссылки на {@link org.apache.lucene.search.IndexSearcher} и связанный с ним {@link org.apache.lucene.search.FieldCache} по полю id
 * <p/>
 * Для корректного закрытия {@link org.apache.lucene.search.IndexSearcher}'а и {@link org.apache.lucene.index.IndexReader}'а, используется финализация.
 */
final class SearcherReference {

	private final IndexReader indexReader;

	private final Searcher searcher;
	private final String[] idFieldCache;

	SearcherReference(IndexReader indexReader, Searcher searcher, String[] idFieldCache) {
		this.indexReader = indexReader;
		this.searcher = searcher;
		this.idFieldCache = idFieldCache;
	}

	Searcher getSearcher() {
		return searcher;
	}

	String[] getIdFieldCache() {
		return idFieldCache;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		searcher.close();
		indexReader.close();
	}
}
