package com.farpost.logwatcher;

import java.util.concurrent.atomic.AtomicInteger;

public class CountVisitor<I> implements Visitor<I, Integer> {

	private final AtomicInteger count = new AtomicInteger(0);

	public void visit(I entry) {
		count.incrementAndGet();
	}

	@Override
	public Integer getResult() {
		return count.intValue();
	}
}
