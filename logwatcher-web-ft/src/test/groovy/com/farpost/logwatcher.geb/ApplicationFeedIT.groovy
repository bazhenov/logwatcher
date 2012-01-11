package com.farpost.logwatcher.geb

import org.testng.annotations.Test
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.not

class ApplicationFeedIT extends LogwatcherFunctionalTestSuite {

	@Test
	public void changingSeverityShouldInitiateFilteringLogEntries() {
		def applicationName = "SeverityTest"
		getLogger(applicationName).warn("warning log")
		getLogger(applicationName).error("error log")

		to ApplicationFeedPage, applicationName
		changeSeverity "warning"
		waitFor { entriesMessages.size == 2 }
		assertThat entriesMessages, hasItem("error log")
		assertThat entriesMessages, hasItem("warning log")

		changeSeverity "error"
		waitFor { entriesMessages.size == 1 }
		assertThat entriesMessages, hasItem("error log")
		assertThat entriesMessages, not(hasItem("warning log"))
	}

}
