package com.farpost.logwatcher.geb

import geb.Page

class SearchResultsPage extends Page {

	@SuppressWarnings("GroovyUnusedDeclaration")
	static at = { title.startsWith("LogWatcher Search:") }

	@SuppressWarnings("GroovyUnusedDeclaration")
	static content = {
		results { $("div.log div")*.text() }
		result { int index -> results[index] }
	}

}
