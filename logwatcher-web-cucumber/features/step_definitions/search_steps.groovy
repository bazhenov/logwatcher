import com.farpost.logwatcher.cucumber.SearchPage
import com.farpost.logwatcher.cucumber.SearchResultsPage

this.metaClass.mixin(cuke4duke.GroovyDsl)

Given(~/я открыл страницу поиска/) {
	browser.to(SearchPage)
}

When(~/я ввел в строку поиска "([^\"]+)"/) { String value ->
	browser.searchField.value(value)
}

When(~/я нажал на Enter/) { String selector ->
	browser.search()
}

Then(~/я должен оказаться на странице результатов поиска/) {
	browser.waitFor { browser.at(SearchResultsPage) }
}

Then(~/результат номер "([^\"]+)" содержит текст "([^\"]+)"/) { int index, String needle ->
	assert browser.result(index).contains(needle)
}

