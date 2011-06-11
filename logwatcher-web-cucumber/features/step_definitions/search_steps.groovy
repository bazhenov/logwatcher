import com.farpost.logwatcher.geb.SearchPage
import com.farpost.logwatcher.geb.SearchResultsPage

this.metaClass.mixin(cuke4duke.GroovyDsl)

When(~/я поискал по запросу "([^\"]+)"/) { String query ->
	browser.to(SearchPage)
	browser.waitFor { browser.at SearchPage }
	browser.searchField.value(query)
	browser.search()
	browser.waitFor { browser.at SearchResultsPage }
}

Then(~/результат поиска #(\d+) должен содержать строку "([^\"]+)"/) { int index, String needle ->
	assert browser.result(index).contains(needle)
}

Then(~/результат поиска #(\d+) не должен содержать строку "([^\"]+)"/) { int index, String needle ->
	assert !browser.result(index).contains(needle)
}