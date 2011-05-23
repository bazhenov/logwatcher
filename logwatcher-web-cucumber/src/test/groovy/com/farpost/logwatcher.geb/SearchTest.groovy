package com.farpost.logwatcher.geb

import org.testng.annotations.Test
import static org.testng.Assert.assertFalse
import static org.testng.Assert.assertTrue

public class SearchTest extends LogwatcherFunctionalTestSuite {

	@Test
	public void searchByApplicationTest() {
		getLogger("Foo").error("foo error")
		getLogger("Bar").error("bar error")

		to SearchPage
		searchField.value("at: Foo")
		search()
		waitFor { at(SearchResultsPage) }
		assertTrue result(0).contains("foo error")
		assertFalse result(0).contains("bar error")
	}

	@Test
	public void searchByCausedTest() {
		getLogger("Foo").error("message", new IllegalArgumentException());
		getLogger("Foo").error("message", new AssertionError());

		to SearchPage
		searchField.value("caused-by: IllegalArgumentException")
		search()
		waitFor { at(SearchResultsPage) }
		assertTrue result(0).contains("IllegalArgumentException")
		assertFalse result(0).contains("AssertionError")
	}

}
