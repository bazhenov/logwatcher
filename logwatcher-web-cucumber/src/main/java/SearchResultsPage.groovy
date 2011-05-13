import geb.Page

class SearchResultsPage extends Page {

	static at = { title.startsWith("LogWatcher Search") }

	static content = {
		results { $("div.logEntry") }
		result { index -> results[index].text() }
	}

}
