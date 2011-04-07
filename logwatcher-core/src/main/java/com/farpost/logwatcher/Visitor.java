package com.farpost.logwatcher;

public interface Visitor<I, O> {

	void visit(I entry);

	O getResult();
}
