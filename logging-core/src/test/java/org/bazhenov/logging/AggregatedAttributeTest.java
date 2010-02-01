package org.bazhenov.logging;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AggregatedAttributeTest {

	@Test
	public void clientCanAddValuesToAttribute() {
		AggregatedAttribute attr = new AggregatedAttribute("machine");

		attr.add(new AttributeValue("aux2", 15));
		attr.add(new AttributeValue("aux1", 16));
		attr.add(new AttributeValue("aux2", 8));

		assertThat(attr.getCountFor("aux1"), equalTo(16));
		assertThat(attr.getCountFor("aux2"), equalTo(23));
	}

	@Test
	public void toArrayShouldSortValuesByCount() {
		AggregatedAttribute attr = new AggregatedAttribute("machine");

		attr.add(new AttributeValue("aux2", 15));
		attr.add(new AttributeValue("aux1", 16));

		AttributeValue[] values = attr.getValues();
		assertThat(values[0].getCount(), equalTo(16));
		assertThat(values[1].getCount(), equalTo(15));
	}
}
