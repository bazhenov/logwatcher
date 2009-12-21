package org.bazhenov.logging.aggregator;

import org.bazhenov.logging.marshalling.Marshaller;

public class SimpleAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator(Marshaller marshaller) {
		return new SimpleAggregator(marshaller);
	}
}
