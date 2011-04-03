package com.farpost.logwatcher;

import java.util.ArrayList;
import java.util.List;

public class CollectingVisitor<T> implements Visitor<T> {

	private final List<T> collectedEntries = new ArrayList<T>();

	@Override
	public void visit(T entry) {
		collectedEntries.add(entry);
	}

	public List<T> getEntries() {
		return collectedEntries;
	}
}
