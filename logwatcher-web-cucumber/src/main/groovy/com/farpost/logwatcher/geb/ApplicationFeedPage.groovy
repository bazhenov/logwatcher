package com.farpost.logwatcher.geb

import geb.Page

class ApplicationFeedPage extends Page {

	static applicationName

	static at = { title == "LogWatcher: " + applicationName + " feed"}

	static content = {
		entriesMessages { $(".entry .entryHeader .message")*.text() }
		severityMenu(required: false) { $("#severityMenu") }
		selectedSeverity(required: false) { $("#severityMenu .selected span") }
	}

	def changeSeverity(severity) {
		$("#severityMenu").jquery.addClass("hover")
		$("a", text: severity).click()
		waitFor {
			selectedSeverity.present && selectedSeverity.text() == severity
		}

	}

	def convertToPath(String applicationName) {
		this.applicationName = applicationName
		"/feed/" + this.applicationName
	}

}
