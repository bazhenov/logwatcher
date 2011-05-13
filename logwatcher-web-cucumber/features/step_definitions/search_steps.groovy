this.metaClass.mixin(cuke4duke.GroovyDsl)

Given(~/я открыл страницу поиска/) {
	browser.to(SearchPage)
}

When(~/я ввел в строку поиска "([^\"]+)"/) { String value ->
	browser.searchField = value
}

When(~/я нажал на Enter/) { String selector ->
	browser.searchButton.click()
}

When(~/я нажал на кнопку "([^\"]+)"/) { String selector ->
	browser.$(selector, 0).click()
}

Then(~/я должен оказаться на странице результатов поиска/) {
	assert browser.at(SearchResultsPage)
}

Then(~/И результат номер "([^\"]+)" содержит текст "([^\"]+)"/) { int index, String needle ->
	assert browser.result[index].contains(needle)
}

