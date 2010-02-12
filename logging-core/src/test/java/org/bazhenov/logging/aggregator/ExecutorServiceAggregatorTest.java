package org.bazhenov.logging.aggregator;

import com.farpost.logging.marshalling.Marshaller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator(Marshaller marshaller) {
		ExecutorService service = Executors.newFixedThreadPool(2);
		return new ExecutorServiceAggregator(marshaller, service);
	}
}
