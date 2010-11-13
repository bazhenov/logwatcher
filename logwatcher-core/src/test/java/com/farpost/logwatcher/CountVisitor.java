package com.farpost.logwatcher;

import com.farpost.logwatcher.Visitor;

import java.util.concurrent.atomic.AtomicInteger;

public class CountVisitor<T> implements Visitor<T> {

	private final AtomicInteger count = new AtomicInteger(0);

	public void visit(T entry) {
		count.incrementAndGet();
	}

	public int getCount() {
		return count.intValue();
	}
}
