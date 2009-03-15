package org.bazhenov.logging.storage;

public class InMemoryLogStorageTest extends LogStorageTest {

	protected LogStorage createStorage() {
		return new InMemoryLogStorage();
	}
}
