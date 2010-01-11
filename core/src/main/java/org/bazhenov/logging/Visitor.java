package org.bazhenov.logging;

public interface Visitor<T> {

	void visit(T entry);
}
