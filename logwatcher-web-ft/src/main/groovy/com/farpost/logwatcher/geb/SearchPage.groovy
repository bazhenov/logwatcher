package com.farpost.logwatcher.geb

import geb.Page
import org.openqa.selenium.Keys

class SearchPage extends Page {

	static url = "/search"
	static at = { title == 'LogWatcher Search' }

	static content = {
		searchField { $("form", name: "searchForm").q() }
		search (to: SearchResultsPage) {
			searchField << Keys.ENTER
			true
		}
	}

}
