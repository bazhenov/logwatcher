package com.farpost.logwatcher.geb

import com.google.common.base.Joiner
import geb.Page

class ApplicationFeedPage extends Page {

	static applicationName

	@SuppressWarnings("GroovyUnusedDeclaration")
	static at = {
		title == "LogWatcher: " + applicationName.toLowerCase() + " feed"
	}

	@SuppressWarnings("GroovyUnusedDeclaration")
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

	@Override
	String convertToPath(Object... args) {
		assert args.length > 0
		def path = Joiner.on('/').join(args)
		applicationName = args.length > 0 ? args[args.length - 1] : ""
		return "/feed/" + path.toLowerCase()
	}
}
