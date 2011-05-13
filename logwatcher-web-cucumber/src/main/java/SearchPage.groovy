import geb.Page

class SearchPage extends Page {

	static url = "/search"
	static at = { title == 'LogWatcher Search' }

	static content = {
		searchField { $(".queryBox form input[name=q]").value() }
		searchButton(to: SearchResultsPage) { $("input[value=submit]") }
	}

}
