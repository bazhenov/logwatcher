package org.bazhenov.logging.aggregator;

import static com.farpost.timepoint.Date.november;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.LogEntry;
import static org.bazhenov.logging.TestSupport.entry;
import org.bazhenov.logging.storage.AttributeValueMatcher;
import org.bazhenov.logging.storage.DateMatcher;
import org.bazhenov.logging.storage.LogEntryMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AggregatorTest {

	private Aggregator aggregator;

	@BeforeMethod
	protected void setUp() throws Exception {
		aggregator = createAggregator();
	}

	@Test
	public void testAggregatorCanFilterEntries() {
		DateTime date = november(12, 2009).at(15, 12);
		int times = 500000;
		List<LogEntry> entries = new ArrayList<LogEntry>(times);
		long start = System.currentTimeMillis();
		for ( int i = 0; i < times; i++ ) {
			LogEntry entry = entry().
				occured(date).
				attribute("machine", "aux" + (i % 10) + ".srv.loc").
				create();
			entries.add(entry);
		}
		long end = System.currentTimeMillis();
		System.out.println("Generating: "+(end-start)+"ms.");

		start = System.currentTimeMillis();
		List<LogEntryMatcher> matchers = new ArrayList<LogEntryMatcher>();

		matchers.add(new DateMatcher(date.minusHour(1), date.plusDay(1)));
		matchers.add(new DateMatcher(date.minusHour(1), date.plusDay(1)));
		matchers.add(new DateMatcher(date.minusHour(1), date.plusDay(1)));
		matchers.add(new AttributeValueMatcher("machine", "aux3.srv.loc"));
		List<AggregatedLogEntry> aggregated = aggregator.aggregate(entries, matchers);
		end = System.currentTimeMillis();
		System.out.println("Filtering: "+(end-start)+"ms.");
		//assertThat(aggregated.size(), equalTo(1));
	}

	private Aggregator createAggregator() {
		ExecutorService service = Executors.newFixedThreadPool(2);
		return new ExecutorServiceAggregator(service);
		//return new SimpleAggregator();
	}
}
