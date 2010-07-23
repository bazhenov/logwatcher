package org.bazhenov.logging;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class AttributeValueTest {

	@Test
	public void clientCanTestEqualityOfAttributeValues() {
		AttributeValue foo = new AttributeValue("foo", 15);

		assertThat(foo, equalTo(new AttributeValue("foo", 15)));
		assertThat(foo, equalTo(new AttributeValue("foo", 18)));
		assertThat(foo, not(new AttributeValue("bar", 15)));
	}

	@Test
	public void clientCanMergeTowAttributeValues() {
		AttributeValue first = new AttributeValue("boo", 12);
		AttributeValue second = new AttributeValue("boo", 15);

		first.add(second);
		assertThat(first.getCount(), equalTo(27));
	}
}
