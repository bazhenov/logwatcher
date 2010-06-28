package org.bazhenov.logging.storage;

public class InMemoryLogStorageTest extends LogStorageTestCase {

	protected LogStorage createStorage() {
		return new InMemoryLogStorage();
	}
}
