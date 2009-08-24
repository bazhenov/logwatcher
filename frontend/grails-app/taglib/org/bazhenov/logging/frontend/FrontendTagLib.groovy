package org.bazhenov.logging.frontend
import groovy.xml.MarkupBuilder
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.frontend.FrontendDateFormat
import java.text.DateFormat


public class FrontendTagLib {

	public final int MAX_LENGTH = 80;
	DateFormat shortFormat = new FrontendDateFormat()
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
		def withStacktrace = !entry.withStacktrace()

		classes.add entry.severity as String

		if ( withStacktrace ) {
			classes.add "withStacktrace"
		}

		def html = new MarkupBuilder(out)
		html.div ('class': classes.join(" ")) {
			div ('class': 'entryHeader') {
				span 'class': 'marker', (withStacktrace ? "&bull;" : "&empty;")
				span 'class': 'message', title
				div ('class': 'times') {
					span 'class': 'applicationId', applicationId
					span "&mdash последний раз " + shortFormat.format(entry.lastTime.asDate())
				}
			}

			div('class': 'entryContent') {
				if ( withStacktrace ) {
					pre 'class': 'stacktrace', message
				}
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