import com.farpost.logwatcher.geb.ApplicationFeedPage

this.metaClass.mixin(cuke4duke.GroovyDsl)


When(~/я захожу на страницу приложения "([^\"]+)"/) { String applicationName ->
	browser.to ApplicationFeedPage, applicationName
	browser.waitFor { browser.at ApplicationFeedPage }
}

When(~/я меняю severity на "([^\"]+)"/) { String severity ->
	browser.changeSeverity severity
}

Then(~/я вижу лог "([^\"]+)"/) { String needle ->
	assert browser.entriesMessages.contains(needle)
}

Then(~/я не вижу лог "([^\"]+)"/) { String needle ->
	assert !browser.entriesMessages.contains(needle)
}