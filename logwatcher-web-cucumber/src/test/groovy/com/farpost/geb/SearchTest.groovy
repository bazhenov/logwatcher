package com.farpost.geb

import com.farpost.logwatcher.cucumber.SearchPage
import com.farpost.logwatcher.cucumber.SearchResultsPage
import geb.Browser
import org.testng.annotations.Test
import static org.testng.Assert.assertFalse
import static org.testng.Assert.assertTrue

public class SearchTest extends GebFunctionalTestSuite {

	@Test
	public void searchByApplicationTest() {
		getLogger("Foo").error("foo error")
		getLogger("Bar").error("bar error")

		Browser.drive(driver, applicationUri) {
			to SearchPage
			searchField.value("at: Foo")
			search()
			waitFor { at(SearchResultsPage) }
			assertTrue result(0).contains("foo error")
			assertFalse result(0).contains("bar error")
		}
	}

}
