package com.farpost.logwatcher.storage;

import java.io.IOException;

public interface SearcherTask<T> {
	T call(SearcherReference ref) throws IOException;
}
