package org.bazhenov.logging.frontend
import groovy.xml.MarkupBuilder
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.frontend.FrontendDateFormat
import java.text.DateFormat
import java.text.SimpleDateFormat
import groovy.xml.StreamingMarkupBuilder


public class FrontendTagLib {

	static namespace = 'f'

	public final int MAX_LENGTH = 80;
	DateFormat shortFormat = new FrontendDateFormat()
	DateFormat fullFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm:ss zz", new Locale("Ru"))
	Writer out

	def entry = {attrs, body ->
		Entry entry = attrs['ref'] as Entry
		def title = entry.title
		def message = entry.text
		def applicationId = entry.applicationId

		if ( title.length() > MAX_LENGTH + "...".length() ) {
			title = title.substring(0, MAX_LENGTH) + "..."
		}

		def classes = ['entry']
		def withStacktrace = entry.withStacktrace()

		String severety = entry.severity as String
		classes.add severety

		if ( withStacktrace ) {
			classes.add "withStacktrace"
		}
		def isExceptionNew = entry.lastTime.plusMinute(30).isInFuture()
		def additionalInfoClasses = ['additionalInfo']
		if ( isExceptionNew ) {
			additionalInfoClasses.add 'warningMarker'
		}

		def markerClasses = ['marker']
		if ( !withStacktrace ) {
			markerClasses.add "emptyMarker"
		}

		def shortDate = shortFormat.format(entry.lastTime.asDate())
		def fullDate = fullFormat.format(entry.lastTime.asDate())

		def jiraLink = "http://jira.dev.loc/jira/secure/CreateIssueDetails.jspa?pid=10000&" +
			"issuetype=1&summary=" + URLEncoder.encode(title) + "&description=" +
			URLEncoder.encode(message) + "&priority=3"

		def html = new MarkupBuilder(out)
		html.nospace = true
		html.div ('class': classes.join(" ")) {
			div ('class': 'entryHeader') {
				span 'class': markerClasses.join(" "), (withStacktrace ? "•" : "∅")
				span 'class': 'message', title
				div ('class': 'times') {
					span 'class': 'applicationId', applicationId
					span " — последний раз "
					span 'class': additionalInfoClasses.join(" "), title: fullDate, shortDate
				}
			}

			if ( withStacktrace ) {
				div('class': 'entryContent') {
					pre 'class': 'stacktrace', message
				}
			}

			div ('class': 'operations') {
				yieldUnescaped("<a href='${jiraLink}' target='_blank'>создать таск</a>")
			}

		}
		return;

		out << "<div class='entry withStacktrace warning'>\
		<div class='entryHeader'>\
			<span class='marker'>&bull;</span><span class='message'\
				>Exception occured during service execution: Error Fetching http headers</span>\
			<div class='times'><span class='applicationId'>baza-frontend</span>\
					&mdash; <span class='additionalInfo' title='2565 раз'>более 1000 раз</span>,\
					последний раз <span class='additionalInfo warningMarker' title='12 июля 2009, 18:25:12 VLAST'>менее минуты назад</span>\
			</div>\
		</div>\
		<div class='entryContent'>\
			<ul>\
				<li><span>сервера:</span> n1.baza.loc, n2.baza.loc...</li>\
				<li><span>адреса:</span> /auto/sale</li>\
			</ul>\
			<pre class='stacktrace'>AdvertServiceException: Error Fetching http headers\
  at /var/www/baza.farpost.ru/rev/20090817-1520/slr/advert/src/remote/AdvertSoapDecorator.class.php:16\
  10 : slrSoapDecorator.class.php:94 AdvertSoapDecorator->handleException('Error Fetc...', SoapFault)\
  9 : unknown:0 slrSoapDecorator->__call('findLinks', [1])\
  8 : AdvertRemoteProvider.class.php:331 AdvertSoapDecorator->findLinks([2])\
  7 : AdvertLinkFinder.class.php:77 AdvertRemoteProvider->findLinks(AdvertLinkFinder)\
  6 : AdvertAbstractFinder.class.php:59 AdvertLinkFinder->getResults()\
  5 : AdvertAbstractFinder.class.php:35 AdvertAbstractFinder->fetchResults()\
  4 : AdvertRemoteAdvertisement.class.php:118 AdvertAbstractFinder->results()\
  3 : advertUnpopularDeactivationService.class.php:34 AdvertRemoteAdvertisement->getLinks(true)\
  2 : advertUnpopularDeactivationService.class.php:23 advertUnpopularDeactivationService->deactivateByMaxViews([849])\
  1 : service_runner.php:38 advertUnpopularDeactivationService->run()</pre>\
		</div>\
		<div class='operations'>\
			<a href='#'>cоздать таск</a> или\
			<a href='#' class='asynchronous removeEntry'>удалить</a>\
		</div>\
	</div>"
	}
}