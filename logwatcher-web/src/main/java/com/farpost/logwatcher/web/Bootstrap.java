package com.farpost.logwatcher.web;

import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.Cause;
import org.bazhenov.logging.LogEntryImpl;
import org.bazhenov.logging.Severity;

import java.util.HashMap;

public class Bootstrap {

	private LogStorage storage;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	public void load() throws LogStorageException {
		storage.writeEntry(new LogEntryImpl(DateTime.now(), "group", "AdvertServiceException: Error Fetching http headers", Severity.error, "sum", "advertisement", null));
		Cause cause = new Cause("RuntimeException", "Socket reading timeout", "AdvertServiceException: Error Fetching http headers\n" +
			"  at /var/www/baza.farpost.ru/rev/20100325-1520/slr/advert/src/remote/AdvertSoapDecorator.class.php:16\n" +
			"  10: slrSoapDecorator.class.php:94 AdvertSoapDecorator->handleException(\"Error Fetc...\", SoapFault)\n" +
			"  9 : unknown:0 slrSoapDecorator->__call(\"findLinks\", [1])\n" +
			"  8 : AdvertRemoteProvider.class.php:331 AdvertSoapDecorator->findLinks([2])\n" +
			"  7 : AdvertLinkFinder.class.php:77 AdvertRemoteProvider->findLinks(AdvertLinkFinder)\n" +
			"  6 : AdvertAbstractFinder.class.php:59 AdvertLinkFinder->getResults()\n" +
			"  5 : AdvertAbstractFinder.class.php:35 AdvertAbstractFinder->fetchResults()\n" +
			"  4 : AdvertRemoteAdvertisement.class.php:118 AdvertAbstractFinder->results()\n" +
			"  3 : advertUnpopularDeactivationService.class.php:34 AdvertRemoteAdvertisement->getLinks(true)\n" +
			"  2 : advertUnpopularDeactivationService.class.php:23 advertUnpopularDeactivationService->deactivateByMaxViews([849])\n" +
			"  1 : service_runner.php:38 advertUnpopularDeactivationService->run()");
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>() {{
				put("url" ,"/some/foo/very/long/url/to/fit/in/screen");
				put("machine", "aux1.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=1");
				put("machine", "aux1.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=2");
				put("machine", "aux4.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=3");
				put("machine", "aux5.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusMinute(18), "group", "java.lang.OutOfMemoryException", Severity.info, "sum3", "search", null));

		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "Ooops", Severity.info, "sum4",
			"geocoder", null, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "Ooops", Severity.debug, "sum4",
			"geocoder", null, cause));

		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "Ooops", Severity.trace, "sum4",
			"geocoder", null, cause));

		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very very long longvery very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));

		storage.writeEntry(new LogEntryImpl(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
			"frontend", null, null));
	}
}
