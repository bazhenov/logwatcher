package org.bazhenov.logging.storage;

public interface MapOperation<I, O> {

	O map(I input);
}
