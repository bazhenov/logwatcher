package com.farpost.logwatcher.geb

import geb.Page

class DashboardPage extends Page {

	static url = "/"
	@SuppressWarnings("GroovyUnusedDeclaration")
	static at = { title == "LogWatcher Dashboard" }

	@SuppressWarnings("GroovyUnusedDeclaration")
	static content = {
		applications { $(".dashboard .item .name")*.text() }
	}

}
