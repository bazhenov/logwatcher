package org.bazhenov.logging.aggregator;

public class SimpleAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator() {
		return new SimpleAggregator();
	}
}
