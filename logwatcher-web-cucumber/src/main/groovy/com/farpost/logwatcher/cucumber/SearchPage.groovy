package com.farpost.logwatcher.cucumber

import geb.Page
import org.openqa.selenium.Keys

class SearchPage extends Page {

	static url = "/search"
	static at = { title == 'LogWatcher Search' }

	static content = {
		searchField { $("form", name: "searchForm").q() }
		search (to: SearchResultsPage) {
			searchField.firstElement().sendKeys(Keys.ENTER)
			true
		}
	}

}
