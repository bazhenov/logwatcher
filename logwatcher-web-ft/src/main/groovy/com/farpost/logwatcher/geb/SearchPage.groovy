package com.farpost.logwatcher.geb

import geb.Page
import org.openqa.selenium.Keys

class SearchPage extends Page {

	static url = "/search"
	@SuppressWarnings("GroovyUnusedDeclaration")
	static at = { title == 'LogWatcher Search' }

	@SuppressWarnings("GroovyUnusedDeclaration")
	static content = {
		searchField { $("form", name: "searchForm").q() }
		search(to: SearchResultsPage) {
			searchField << Keys.ENTER
			true
		}
	}

}
