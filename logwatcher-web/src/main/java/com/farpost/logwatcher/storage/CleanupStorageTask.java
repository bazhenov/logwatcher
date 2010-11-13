package com.farpost.logwatcher.storage;

import com.farpost.timepoint.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.farpost.timepoint.Date.today;

public class CleanupStorageTask implements Runnable {

	private final LogStorage storage;
	private int daysToKeep = 10;
	private static Logger log = LoggerFactory.getLogger(CleanupStorageTask.class);

	public CleanupStorageTask(LogStorage storage) {
		this.storage = storage;
	}

	/**
	 * Устанавливает сколько дней будут оставлены после clean up'а.
	 *
	 * @param daysToKeep данные за сколько дней оставлять в storage'е
	 */
	public void setDaysToKeep(int daysToKeep) {
		if (daysToKeep <= 0) {
			throw new IllegalArgumentException("Should be positive integer");
		}
		this.daysToKeep = daysToKeep;
	}

	@Override
	public void run() {
		try {
			Date date = today().minusDay(daysToKeep);
			log.info("Removing from storage entries older than: " + date);
			storage.removeOldEntries(date);
		} catch (LogStorageException e) {
			log.error("Error while removing old entries from storage", e);
		}
	}
}
