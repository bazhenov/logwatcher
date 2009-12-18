package org.bazhenov.logging.aggregator;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.storage.AttributeValueMatcher;
import org.bazhenov.logging.storage.LogEntryMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.farpost.timepoint.Date.november;
import static org.bazhenov.logging.TestSupport.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

abstract public class AggregatorTestCase {

	private Aggregator aggregator;

	@BeforeMethod
	protected void setUp() throws Exception {
		aggregator = createAggregator();
	}

	@Test
	public void testAggregatorCanFilterEntries() {
		DateTime date = november(12, 2009).at(15, 12);
		int problemSize = 3333;
		List<LogEntry> entries = new ArrayList<LogEntry>(problemSize);

		for ( int i = 0; i < problemSize; i++ ) {
			LogEntry entry = entry().
				occured(date).
				attribute("machine", "aux" + (i % 3) + ".srv.loc").
				create();
			entries.add(entry);
		}
		List<LogEntryMatcher> matchers = new ArrayList<LogEntryMatcher>();

		matchers.add(new AttributeValueMatcher("machine", "aux1.srv.loc"));
		Collection<AggregatedLogEntry> aggregated = aggregator.aggregate(entries, matchers);
		assertThat(aggregated.size(), equalTo(1));

		AggregatedLogEntry[] arr = aggregated.toArray(new AggregatedLogEntry[aggregated.size()]);
		assertThat(arr[0].getAttributes().get("machine").getCountFor("aux1.srv.loc"), equalTo(1111));
	}

	abstract protected Aggregator createAggregator();
}
