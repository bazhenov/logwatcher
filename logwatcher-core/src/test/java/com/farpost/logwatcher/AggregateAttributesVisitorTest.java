package com.farpost.logwatcher;

import com.farpost.logwatcher.storage.InMemoryLogStorage;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import org.testng.annotations.Test;

import java.util.Map;

import static com.farpost.logwatcher.storage.LogEntries.entries;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AggregateAttributesVisitorTest {

	@Test
	public void visitorCanAggregateAttributes() throws LogStorageException, InvalidCriteriaException {
		AggregateAttributesVisitor visitor = new AggregateAttributesVisitor();

		LogStorage storage = new InMemoryLogStorage();

		LogEntryBuilder.entry().attribute("foo", "foo").saveIn(storage);
		LogEntryBuilder.entry().attribute("foo", "foo").saveIn(storage);
		LogEntryBuilder.entry().attribute("foo", "bar").saveIn(storage);

		LogEntryBuilder.entry().attribute("bar", "foo").saveIn(storage);

		AggregationResult result = storage.walk(entries().all(), visitor);

		Map<String, AggregatedAttribute> attributes = result.getAttributeMap();
		assertThat(attributes.get("foo").getCountFor("foo"), equalTo(2));
		assertThat(attributes.get("foo").getCountFor("bar"), equalTo(1));
		assertThat(attributes.get("bar").getCountFor("foo"), equalTo(1));
	}


}
