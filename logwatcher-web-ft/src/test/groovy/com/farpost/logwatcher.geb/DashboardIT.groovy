package com.farpost.logwatcher.geb

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.not

class DashboardIT extends LogwatcherFunctionalTestSuite {

	@Test
	public void newApplicationShouldBeShownOnDashboard() {
		def newAppName = "newapp"

		to DashboardPage
		assertThat applications, not(hasItem(newAppName))

		getLogger(newAppName).error("New log entry")
		to DashboardPage
		assertThat applications, hasItem(newAppName)
	}
}
