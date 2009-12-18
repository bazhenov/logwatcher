package org.bazhenov.logging.aggregator;

import static com.farpost.timepoint.Date.november;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedAttributeTest;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.AggregatedAttribute;
import static org.bazhenov.logging.TestSupport.entry;
import org.bazhenov.logging.storage.AttributeValueMatcher;
import org.bazhenov.logging.storage.LogEntryMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AggregatorTest {

	private Aggregator aggregator;

	@BeforeMethod
	protected void setUp() throws Exception {
		aggregator = createAggregator();
	}

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

	private Aggregator createAggregator() {
		ExecutorService service = Executors.newFixedThreadPool(2);
		return new ExecutorServiceAggregator(service);
	}
}
