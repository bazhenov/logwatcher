package org.bazhenov.logging.aggregator;

import com.farpost.logging.marshalling.JDomMarshaller;
import com.farpost.logging.marshalling.Marshaller;
import com.farpost.logging.marshalling.MarshallerException;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.AggregatedEntry;
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
	private Marshaller marshaller = new JDomMarshaller();

	@BeforeMethod
	protected void setUp() throws Exception {
		aggregator = createAggregator(marshaller);
	}

	@Test
	public void testAggregatorCanFilterEntries() throws MarshallerException {
		DateTime date = november(12, 2009).at(15, 12);
		int problemSize = 333;
		List<String> entries = new ArrayList<String>(problemSize);

		for ( int i = 0; i < problemSize; i++ ) {
			LogEntry entry = entry().
				occured(date).
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
