package com.farpost.logwatcher;

import java.util.ArrayList;
import java.util.List;

public class CollectingVisitor<I> implements Visitor<I, List<I>> {

	private List<I> collectedEntries = new ArrayList<I>();

	@Override
	public void visit(I entry) {
		collectedEntries.add(entry);
	}

	public List<I> getResult() {
		return collectedEntries;
	}
}
