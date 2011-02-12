package com.farpost.logwatcher.storage.lucene;

import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageTestCase;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;

public class LuceneBdbLogStorageTest extends LogStorageTestCase {

	@Override
	protected LogStorage createStorage() throws Exception {
		File f = File.createTempFile("bdb", "tmp");
		f.delete();
		f.mkdirs();
		f.deleteOnExit();

		EnvironmentConfig config = new EnvironmentConfig();
		config.setAllowCreate(true);
		Environment environment = new Environment(f, config);
		LuceneBdbLogStorage storage = new LuceneBdbLogStorage(new RAMDirectory(), environment);
		storage.setCommitThreshold(0);
		return storage;
	}
}
