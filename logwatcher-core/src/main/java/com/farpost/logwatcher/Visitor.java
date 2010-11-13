package com.farpost.logwatcher;

public interface Visitor<T> {

	void visit(T entry);
}
