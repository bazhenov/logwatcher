package com.farpost.logwatcher.storage;

public interface SearcherTask<T> {
	T call(SearcherReference ref);
}
