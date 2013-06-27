package com.farpost.logwatcher.web;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.storage.LogStorage;
import com.google.common.base.Charsets;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public class Bootstrap implements InitializingBean {

	private static final Logger log = getLogger(Bootstrap.class);

	private LogStorage storage;
	private ClusterDao clusterDao;
	private ClusterStatistic clusterStatistic;

	private boolean loadSampleDump = false;
	private File indexLocation;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	public void setClusterDao(ClusterDao clusterDao) {
		this.clusterDao = clusterDao;
	}

	public void setClusterStatistic(ClusterStatistic clusterStatistic) {
		this.clusterStatistic = clusterStatistic;
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
			loadSampleDump();
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

	private void loadSampleDump() {
		Random rnd = new Random();
		log.info("Loading sample dump...");
		write(new LogEntryImpl(now(), "group", "AdvertServiceException: Error Fetching http headers", Severity.error, "sum", "advertisement", null));
		Cause cause = new Cause("java.lang.RuntimeException", "Socket reading timeout", "AdvertServiceException: Error Fetching http headers\n" +
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
			DateTime now = now();
			write(new LogEntryImpl(now.minusSeconds(rnd.nextInt(800)), "group", "OverflowFundsException", Severity.warning, "sum2",
				"billing", new HashMap<String, String>() {{
				put("url", "/some/foo/very/long/url/to/fit/in/screen");
				put("machine", "aux1.srv.loc");
			}}, cause));
		}

		write(new LogEntryImpl(now().minusHours(1), "group", "Ooops", Severity.info, "sum4",
			"geocoder", null, cause));
		write(new LogEntryImpl(now().minusHours(1), "group", "Ooops", Severity.debug, "sum4",
			"geocoder", null, cause));

		write(new LogEntryImpl(now().minusHours(1), "group", "Ooops", Severity.trace, "sum4",
			"geocoder", null, cause));

		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));
		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very very long longvery very long longvery very very long long Exception", Severity.error, "sum4",
			"frontend", null, cause));

		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
			"frontend", null, null));
		write(new LogEntryImpl(now().minusHours(1), "group", "very 'very' very long longvery very very long longvery very very long long Exceptionvery very very long longvery very very long longvery very very long long Exception", Severity.error, "sum5",
			"frontend", null, null));
	}

	private void write(LogEntry entry) {
		storage.writeEntry(entry);
		Checksum checksum = new Checksum(entry.getChecksum().getBytes(Charsets.UTF_8));
		if (!clusterDao.isClusterRegistered(entry.getApplicationId(), checksum)) {
			clusterDao.registerCluster(new Cluster(entry.getApplicationId(), entry.getSeverity(), entry.getMessage(), checksum));
		}
		clusterStatistic.registerEvent(entry.getApplicationId(), entry.getDate(), checksum);
	}
}
