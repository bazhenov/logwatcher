import com.farpost.logwatcher.geb.DashboardPage

this.metaClass.mixin(cuke4duke.GroovyDsl)

Given(~/у приложения "([^\"]+)" нет логов/) { String applicationName ->
	browser.to DashboardPage
	browser.waitFor { browser.at DashboardPage }
	assert !browser.applications.contains(applicationName)
}

Then(~/на главной не должно быть виджета "([^\"]+)"/) { String applicationName ->
	browser.to DashboardPage
	browser.waitFor { browser.at DashboardPage }
	assert !browser.applications.contains(applicationName)
}

Then(~/на главной должен быть виджет "([^\"]+)"/) { String applicationName ->
	browser.to DashboardPage
	browser.waitFor { browser.at DashboardPage }
	assert browser.applications.contains(applicationName)
}