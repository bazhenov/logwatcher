package org.bazhenov.logging.frontend

import java.text.DateFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.frontend.DateTimeFormat
import static java.lang.Math.abs
import static java.lang.Math.min
import static java.net.URLEncoder.encode
import org.codehaus.groovy.grails.plugins.codecs.HTMLCodec

public class FrontendTagLib {

	static {
		Integer.metaClass.pluralize = FrontendTagLib.pluralize;
	}

	static namespace = 'f'

	static def pluralize = { titles ->
		def number = delegate
		def abs = abs(number)
		def cases = [2, 0, 1, 1, 1, 2]

		if ( titles instanceof String ) {
			titles = titles.split(" ")
		}

		def result = titles[ (abs%100 > 4 && abs%100 < 20) ? 2 : cases[min(abs%10, 5)] ]
		number + " " + result
	}

	public final int MAX_LENGTH = 80;
	DateFormat shortFormat = new DateTimeFormat()
	DateFormat fullFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm:ss zz", new Locale("Ru"))
	Writer out

	def entry = {attrs, body ->
		Entry entry = attrs['ref'] as Entry
		def title = entry.title

		def applicationId = entry.applicationId
		def count = entry.count

		def withStacktrace = entry.withStacktrace()
		def isTitleTooLong = title.length() > MAX_LENGTH + "...".length();
		if ( isTitleTooLong ) {
			title = title.substring(0, MAX_LENGTH) + "..."
		}
		def hasMessage = withStacktrace || isTitleTooLong
		def message = (!withStacktrace && isTitleTooLong)	? entry.title	: entry.text

		def classes = ['entry']

		String severety = entry.severity as String
		classes.add severety

		if ( hasMessage ) {
			classes.add "withStacktrace"
		}
		def isExceptionNew = entry.lastTime.plusMinute(30).isInFuture()
		def additionalInfoClasses = ['additionalInfo']
		if ( isExceptionNew ) {
			additionalInfoClasses.add 'warningMarker'
		}

		def markerClasses = ['marker']
		if ( !hasMessage ) {
			markerClasses.add "emptyMarker"
		}

		def fieldPosition = new FieldPosition(DateFormat.HOUR0_FIELD)
		def buffer = new StringBuffer()
		shortFormat.format(entry.lastTime.asDate(), buffer, fieldPosition)
		def fullDate = fullFormat.format(entry.lastTime.asDate())

		/**
		 * Не забываем, что теги надо вставлять в обратной последовательности, чтобы не допустить
		 * смещения индексов в FieldPosition
		 */
		buffer.insert(fieldPosition.endIndex, "</span>")
		buffer.insert(fieldPosition.beginIndex, "<span class='${additionalInfoClasses.join(' ')}' title='${fullDate}'>")

		def lastOccurenceInfo = buffer.toString();

		def timesInfo
		timesInfo = count.pluralize("раз раза раз")
		if ( count > 10000 ) {
			timesInfo = "<span class='additionalInfo' title='${timesInfo}'>более 10 000 раз</span>"
		} else if ( count > 5000 ) {
			timesInfo = "<span class='additionalInfo' title='${timesInfo}'>более 5 000 раз</span>"
		} else if ( count > 1000 ) {
			timesInfo = "<span class='additionalInfo' title='${timesInfo}'>более 1 000 раз</span>"
		}

		def jiraLink = "http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa?pid=10000&" +
			"issuetype=1&summary=${encode(title)}&description=${encode(message)}&priority=3"

		title = HTMLCodec.encode(title)
		message = HTMLCodec.encode(message)

		out <<  "<a name='${entry.checksum}'></a>"
		out <<  "<div class='${classes.join(" ")}' checksum='${entry.checksum}'>"
		out <<    "<div class='entryHeader'>"
		out <<      "<span class='${markerClasses.join(" ")}'>${hasMessage ? "•" : "∅"}</span>"
		out <<      "<span class='message'>${title}</span>"
		out <<      "<div class='times'>"
		out <<        "<span class='applicationId'>${applicationId}</span> &mdash "
		out <<        (count > 1 ? "${timesInfo}, последний раз " : "")
		out <<        lastOccurenceInfo
		out <<      "</div>"

		if ( hasMessage ) {
			out <<      "<div class='entryContent'>"
			out <<        "<pre class='stacktrace'>${message}</pre>"
			out <<      "</div>"
		}

		out <<      "<div class='operations'>"
		out <<        "<a href='${jiraLink}' target='_blank'>создать таск</a>"
		out <<        " или "
		out <<        "<a class='removeEntry asynchronous' href='#'>удалить</a> "
		out <<        "<a href='./${entry.lastTime.date}?severity=${entry.severity}#${entry.checksum}'>"
		out <<          "<img src='./images/link-icon.png' /></a>"
		out <<      "</div>"
		out <<    "</div>"
		out <<  "</div>"
	}
}