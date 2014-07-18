package com.farpost.logwatcher.web;

import com.farpost.logwatcher.Cause;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.LogEntryImpl;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.transport.LogEntryListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class Bootstrap implements InitializingBean {

	private static final Logger log = getLogger(Bootstrap.class);

	private LogEntryListener entryListener;

	private boolean loadSampleDump = false;
	private File indexLocation;

	public void setEntryListener(LogEntryListener entryListener) {
		this.entryListener = entryListener;
	}

	public void setIndexLocation(File indexLocation) {
		this.indexLocation = indexLocation;
	}

	public void setLoadSampleDump(boolean loadSampleDump) {
		this.loadSampleDump = loadSampleDump;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (loadSampleDump) {
			deleteDirectory(indexLocation);
			checkState(indexLocation.mkdir());
			log.info("Loading sample dump...");
			new Thread(new DumpLoader()).start();
		}
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = checkNotNull(path.listFiles());
			for (File file : files) {
				if (file.isDirectory()) {
					checkState(deleteDirectory(file), "Unable to remove directory", file.getAbsolutePath());
				} else {
					checkState(file.delete(), "Unable to remove file: %s", file.getAbsolutePath());
				}
			}
		}
		return path.delete();
	}

	private void write(LogEntry entry) {
		entryListener.onEntry(entry);
	}

	private class DumpLoader implements Runnable {

		private Random rnd = new Random();
		private List<LogEntry> entries = newArrayList();

		@Override
		public void run() {
			register(new LogEntryImpl(new Date(), "com.farpost.AdvertManager", "AdvertServiceException: Error Fetching http headers", Severity.error, "sum", "advertisement", null));
			Cause cause = new Cause("java.lang.RuntimeException", "Socket reading timeout",
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

			for (int i = 0; i < 200; i++) {
				register(new LogEntryImpl(new Date(), "com.farpost.AuditPolicy", "OverflowFundsException", Severity.warning, "sum2",
					"billing", new HashMap<String, String>() {{
					put("url", "/some/foo/very/long/url/to/fit/in/screen");
					put("machine", "aux1.srv.loc");
				}}, cause));
			}

			register(new LogEntryImpl(new Date(), "", "Ooops", Severity.info, "sum4",
				"geocoder", null, cause));
			register(new LogEntryImpl(new Date(), "", "Ooops", Severity.debug, "sum4",
				"geocoder", null, cause));

			register(new LogEntryImpl(new Date(), "FarPost\\Geocoder\\ServiceImpl", "Ooops", Severity.trace, "sum4",
				"geocoder", null, cause));

			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very very long longvery very long longvery very very long long Exception", Severity.error, "sum4",
				"frontend", null, cause));

			register(new LogEntryImpl(new Date(), "FarPost\\Geocoder\\AdvertServiceManager", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
				"frontend", null, null));
			register(new LogEntryImpl(new Date(), "", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
				"frontend", null, null));

			while (!currentThread().isInterrupted()) {
				fireNewEvent();
				sleepUninterruptibly(1, SECONDS);
			}
		}

		private void fireNewEvent() {
			LogEntry e = entries.get(rnd.nextInt(entries.size()));
			LogEntry n = new LogEntryImpl(new Date(), e.getGroup(), e.getMessage(), e.getSeverity(), e.getChecksum(), e.getApplicationId(),
				e.getAttributes(), e.getCause());
			write(n);
		}

		private void register(LogEntry entry) {
			entries.add(entry);
		}
	}
}
