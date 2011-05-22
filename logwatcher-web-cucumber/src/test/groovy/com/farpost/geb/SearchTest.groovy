package com.farpost.geb

import com.farpost.logwatcher.cucumber.SearchPage
import com.farpost.logwatcher.cucumber.SearchResultsPage
import geb.Browser
import org.testng.annotations.Test
import static org.testng.Assert.assertTrue

public class SearchTest extends GebFunctionalTestSuite {

	@Test
	public void searchTest() {
		Browser.drive(driver, applicationUri) {
			to SearchPage
			searchField.value("at: frontend")
			search()
			waitFor { at(SearchResultsPage) }
			assertTrue result(0).contains("very 'very' very")
		}
	}

}
