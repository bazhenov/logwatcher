package org.bazhenov.logging;

import org.bazhenov.logging.storage.InMemoryLogStorage;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.testng.annotations.Test;

import java.util.Map;

import static org.bazhenov.logging.TestSupport.entry;
import static org.bazhenov.logging.storage.LogEntries.entries;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AggregateAttributesVisitorTest {

	@Test
	public void visitorCanAggregateAttributes() throws LogStorageException, InvalidCriteriaException {
		AggregateAttributesVisitor visitor = new AggregateAttributesVisitor();

		LogStorage storage = new InMemoryLogStorage();

		entry().attribute("foo", "foo").saveIn(storage);
		entry().attribute("foo", "foo").saveIn(storage);
		entry().attribute("foo", "bar").saveIn(storage);

		entry().attribute("bar", "foo").saveIn(storage);

		storage.walk(entries().all(), visitor);

		Map<String, AggregatedAttribute> attributes = visitor.getAttributeMap();
		assertThat(attributes.get("foo").getCountFor("foo"), equalTo(2));
		assertThat(attributes.get("foo").getCountFor("bar"), equalTo(1));
		assertThat(attributes.get("bar").getCountFor("foo"), equalTo(1));
	}


}
