package com.farpost.logwatcher.aggregator;

import com.farpost.logwatcher.marshalling.Marshaller;

public class SimpleAggregatorTest extends AggregatorTestCase {

	@Override
	protected Aggregator createAggregator(Marshaller marshaller) {
		return new SimpleAggregator(marshaller);
	}
}
