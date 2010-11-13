package com.farpost.logwatcher.aggregator;

import com.farpost.logwatcher.marshalling.Marshaller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator(Marshaller marshaller) {
		ExecutorService service = Executors.newFixedThreadPool(2);
		return new ExecutorServiceAggregator(marshaller, service);
	}
}
