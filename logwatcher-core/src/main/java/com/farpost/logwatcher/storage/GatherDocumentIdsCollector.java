package com.farpost.logwatcher.storage;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class GatherDocumentIdsCollector extends Collector {

	private final String fieldName;
	private final List<int[]> values = newArrayList();

	public GatherDocumentIdsCollector(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		//do nothing
	}

	@Override
	public void collect(int doc) throws IOException {
		//do nothing
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		values.add(FieldCache.DEFAULT.getInts(reader, fieldName));
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	public List<int[]> getValues() {
		return values;
	}
}
