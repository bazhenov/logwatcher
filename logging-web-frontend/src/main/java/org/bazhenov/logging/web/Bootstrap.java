package org.bazhenov.logging.web;

import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.Severity;
import org.bazhenov.logging.Cause;
import com.farpost.timepoint.DateTime;

import java.util.*;

public class Bootstrap {

	private LogStorage storage;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	public void load() throws LogStorageException {
		storage.writeEntry(new LogEntry(DateTime.now(), "group", "AdvertServiceException: Error Fetching http headers", Severity.error, "sum", "advert", null));
		Cause cause = new Cause("RuntimeException", "Devision by zero", "AdvertServiceException: 'Error' Fetching http headers\n" +
			"  at /var/www/baza.farpost.ru/rev/20090817-1520/slr/advert/src/remote/AdvertSoapDecorator.class.php:16\n" +
			"  10 : slrSoapDecorator.class.php:94 AdvertSoapDecorator->handleException(\"Error Fetc...\", SoapFault)\n" +
			"  9 : unknown:0 slrSoapDecorator->__call(\"findLinks\", [1])\n" +
			"  8 : AdvertRemoteProvider.class.php:331 AdvertSoapDecorator->findLinks([2])\n" +
			"  7 : AdvertLinkFinder.class.php:77 AdvertRemoteProvider->findLinks(AdvertLinkFinder)\n" +
			"  6 : AdvertAbstractFinder.class.php:59 AdvertLinkFinder->getResults()\n" +
			"  5 : AdvertAbstractFinder.class.php:35 AdvertAbstractFinder->fetchResults()\n" +
			"  4 : AdvertRemoteAdvertisement.class.php:118 AdvertAbstractFinder->results()\n" +
			"  3 : advertUnpopularDeactivationService.class.php:34 AdvertRemoteAdvertisement->getLinks(true)\n" +
			"  2 : advertUnpopularDeactivationService.class.php:23 advertUnpopularDeactivationService->deactivateByMaxViews([849])advertUnpopularDeactivationService.class.php:23 advertUnpopularDeactivationService->deactivateByMaxViews([849])advertUnpopularDeactivationService.class.php:23 advertUnpopularDeactivationService->deactivateByMaxViews([849])\n" +
			"  1 : service_runner.php:38 advertUnpopularDeactivationService->run()");
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>() {{
				put("url" ,"/some/foo");
				put("machine", "aux1.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=1");
				put("machine", "aux1.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=2");
				put("machine", "aux4.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(2), "group", "OverflowFundsException", Severity.warning, "sum2",
			"billing", new HashMap<String, String>(){{
				put("url" ,"/some/foo/bar?uri=3");
				put("machine", "aux5.srv.loc");
			}}, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusMinute(18), "group", "java.lang.OutOfMemoryException", Severity.info, "sum3", "search", null));

		storage.writeEntry(new LogEntry(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"searcha", null, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"searcha", null, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"searcha", null, cause));
		storage.writeEntry(new LogEntry(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very very long longvery very long longvery very very long long Exception", Severity.error, "sum4",
			"searcha", null, cause));

		storage.writeEntry(new LogEntry(DateTime.now().minusHour(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
			"searcha", null, null));
	}
}
