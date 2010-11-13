package com.farpost.logwatcher.storage;

public class InMemoryLogStorageTest extends LogStorageTestCase {

	protected LogStorage createStorage() {
		return new InMemoryLogStorage();
	}
}
