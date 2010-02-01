package org.bazhenov.logging;

import org.testng.annotations.Test;
import static org.bazhenov.logging.Severity.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SeverityTest {

	@Test
	public void seveityCanBeCastedToString() {
		assertThat(warning.toString(), equalTo("warning"));
	}

	@Test
	public void seveityCanBeParsedFromString() {
		assertThat(Severity.forName("warning"), equalTo(warning));
		assertThat(Severity.forName("Warning"), equalTo(warning));

		assertThat(Severity.forName("oops"), equalTo(null));
	}

	@Test
	public void seveityCanBeParsedFromInteger() {
		assertThat(Severity.forCode(1), equalTo(trace));
	}
}
