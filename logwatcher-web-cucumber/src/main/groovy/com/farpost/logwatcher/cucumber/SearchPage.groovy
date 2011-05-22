package com.farpost.logwatcher.cucumber

import geb.Page

class SearchPage extends Page {

	static url = "/search"
	static at = { title == 'LogWatcher Search' }

	static content = {
		searchField { $("input", name: "q") }
		searchButton(to: SearchResultsPage) { $("input[value=submit]") }
	}

}
