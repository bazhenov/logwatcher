package com.farpost.logwatcher.geb

import geb.Page

class DashboardPage extends Page {

	static url = "/"
	static at = { title == "LogWatcher Dashboard" }

	static content = {
		applications { $(".dashboardWidget .title a")*.text() }
	}

}
