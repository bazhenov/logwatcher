package org.bazhenov.logging.aggregator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator() {
		ExecutorService service = Executors.newFixedThreadPool(2);
		return new ExecutorServiceAggregator(service);
	}
}
