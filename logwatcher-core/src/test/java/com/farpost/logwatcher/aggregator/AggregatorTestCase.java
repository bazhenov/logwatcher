package com.farpost.logwatcher.aggregator;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.AttributeValueMatcher;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

abstract public class AggregatorTestCase {

	private Aggregator aggregator;
	private Marshaller marshaller = new Jaxb2Marshaller();

	@BeforeMethod
	protected void setUp() throws Exception {
		aggregator = createAggregator(marshaller);
	}

	@Test
	public void testAggregatorCanFilterEntries() {
		DateTime date = new DateTime(2009, 11, 12, 15, 12);
		int problemSize = 333;
		List<byte[]> entries = new ArrayList<byte[]>(problemSize);

		for (int i = 0; i < problemSize; i++) {
			LogEntry entry = entry().
				occurred(date).
				attribute("machine", "aux" + (i % 3) + ".srv.loc").
				create();
			entries.add(marshaller.marshall(entry));
		}
		List<LogEntryMatcher> matchers = new ArrayList<LogEntryMatcher>();

		matchers.add(new AttributeValueMatcher("machine", "aux1.srv.loc"));
		Collection<AggregatedEntry> aggregated = aggregator.aggregate(entries, matchers);
		assertThat(aggregated.size(), equalTo(1));

		AggregatedEntry[] arr = aggregated.toArray(new AggregatedEntry[aggregated.size()]);
		assertThat(arr[0].getLastTime(), equalTo(date));
	}

	abstract protected Aggregator createAggregator(Marshaller marshaller);
}
