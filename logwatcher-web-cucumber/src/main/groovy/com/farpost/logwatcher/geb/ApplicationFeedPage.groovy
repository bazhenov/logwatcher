package com.farpost.logwatcher.geb

import geb.Page
import org.openqa.selenium.StaleElementReferenceException

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
		/** Ебучий HtmlUnit слишком быстр, чтобы перегрузить страницу,
		 * И слишком туп, чтобы пытаться вытаскивать элемент из новой страницы.
		 * по его мнению гораздо круче достать элемент из старой и обнаружить,
		 * что она уже DOM уже давным давно перестроен
		 */
		waitFor {
			try {
				selectedSeverity.present && selectedSeverity.text() == severity
			} catch (StaleElementReferenceException exception) {
				false
			}
		}
	}

	def convertToPath(String applicationName) {
		this.applicationName = applicationName
		"/feed/" + this.applicationName
	}

}
