package com.farpost.logwatcher.storage.lucene;

import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageTestCase;
import org.apache.lucene.store.RAMDirectory;

public class LuceneBdbLogStorageTest extends LogStorageTestCase {

	@Override
	protected LogStorage createStorage() throws Exception {
		LuceneBdbLogStorage storage = new LuceneBdbLogStorage(new RAMDirectory());
		storage.setCommitThreshold(0);
		return storage;
	}
}
