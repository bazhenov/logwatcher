package com.farpost.logwatcher.geb

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

public class SearchIT extends LogwatcherFunctionalTestSuite {

	@Test
	public void searchByApplicationTest() {
		getLogger("Foo").error("foo error")
		getLogger("Bar").error("bar error")

		to SearchPage
		searchField.value("at: Foo")
		search()
		waitFor { at SearchResultsPage }
		assertThat results, hasItem(containsString("foo error"))
		assertThat results, not(hasItem(containsString("bar error")))
	}

	@Test
	public void searchByCausedTest() {
		getLogger("Foo").error("message", new IllegalArgumentException());
		getLogger("Foo").error("message", new AssertionError());

		to SearchPage
		searchField.value("caused-by: IllegalArgumentException")
		search()
		waitFor { at SearchResultsPage }
		assertThat results, hasItem(containsString("IllegalArgumentException"))
		assertThat results, not(hasItem(containsString("AssertionError")))
	}

	@Test
	public void searchPageSortingTest() {
		getLogger("Bar").error("First error")
		sleep(1000)
		getLogger("Bar").error("Second error")

		to SearchPage
		searchField.value("at: Bar")
		search()
		waitFor { at SearchResultsPage }
		assertThat result(0), containsString("Second error")
		assertThat result(1), containsString("First error")
	}

}
