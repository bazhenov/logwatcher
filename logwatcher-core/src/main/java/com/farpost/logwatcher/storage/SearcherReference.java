package com.farpost.logwatcher.storage;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;

import java.util.List;

/**
 * Tuple хранящий ссылки на {@link org.apache.lucene.search.IndexSearcher} и связанный с ним
 * {@link org.apache.lucene.search.FieldCache} по полю id
 * <p/>
 * Для корректного закрытия {@link org.apache.lucene.search.IndexSearcher}'а и
 * {@link org.apache.lucene.index.IndexReader}'а, используется финализация.
 */
final class SearcherReference {

	private final IndexReader indexReader;

	private final Searcher searcher;
	private final List<int[]> idFieldCache;

	SearcherReference(IndexReader indexReader, Searcher searcher, List<int[]> idFieldCache) {
		this.indexReader = indexReader;
		this.searcher = searcher;
		this.idFieldCache = idFieldCache;
	}

	Searcher getSearcher() {
		return searcher;
	}

	public int getDocumentId(int doc) {
		for (int[] ids : idFieldCache) {
			if (ids.length > doc) {
				return ids[doc];
			} else {
				doc = doc - ids.length;
			}
		}

		throw new RuntimeException("Document not found");
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		searcher.close();
		indexReader.close();
	}
}
