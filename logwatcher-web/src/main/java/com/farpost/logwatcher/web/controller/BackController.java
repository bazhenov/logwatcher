package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.AggregateAttributesVisitor;
import com.farpost.logwatcher.AggregationResult;
import com.farpost.logwatcher.ByOccurrenceDateComparator;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.statistics.ByDayStatistic;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.MinuteVector;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.google.common.collect.Ordering;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.statistics.MinuteVector.SIZE;
import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.joda.time.LocalDate.fromDateFields;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
public class BackController {

	@Autowired
	private LogStorage storage;

	@Autowired
	private ClusterStatistic stat;

	public BackController() {
	}

	public BackController(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/service/content")
	public String handleAttributes(@RequestParam("checksum") String checksum, ModelMap map)
		throws LogStorageException, InvalidCriteriaException, ParseException {

		AggregateAttributesVisitor visitor = new AggregateAttributesVisitor();

		AggregationResult result = entries().
			checksum(checksum).
			date(LocalDate.now()).
			walk(storage, visitor);

		map.addAttribute("attributes", result.getAttributeMap());
		return "service/aggregated-entry-content";
	}

	@RequestMapping("/service/stat/by-minute.json")
	@ResponseBody
	public Map<String, Object> handleMinuteStat(@RequestParam String application, @RequestParam String checksum,
																							@RequestParam(defaultValue = "5") int minutes) {
		checkArgument(minutes > 0, "Minute threshold should be positive");

		MinuteVector vec = stat.getMinuteVector(application, fromHexString(checksum));
		Map<String, Object> result = newHashMap();
		List<Long> data = newArrayList();
		List<String> labels = newArrayList();
		int start = calculateRelativeWindow(vec, minutes);
		for (int i = start; i < start + minutes; i++) {
			data.add(vec.get(i));
			labels.add(DateTime.now().plusMinutes(i).toString("HH:mm"));
		}
		result.put("data", data);
		result.put("labels", labels);

		return result;
	}

	@RequestMapping("/service/stat/by-day.json")
	@ResponseBody
	public Map<String, Object> handleDayStat(@RequestParam String application, @RequestParam String checksum,
																					 @RequestParam(required = false, defaultValue = "7") int days) {
		checkArgument(days > 0, "Day threshold should be positive");

		ByDayStatistic statistic = stat.getByDayStatistic(application, fromHexString(checksum));
		Map<String, Object> result = newHashMap();
		List<Integer> data = newArrayList();
		List<String> labels = newArrayList();
		for (int i = -days + 1; i <= 0; i++) {
			LocalDate date = LocalDate.now().plusDays(i);
			data.add(statistic.getCount(date));
			labels.add(date.toString("dd MMMM"));
		}
		result.put("data", data);
		result.put("labels", labels);

		return result;
	}

	@RequestMapping("/service/log")
	@ModelAttribute("p")
	public DetailsLogPage handleLog(@RequestParam String application,
																	@RequestParam @DateTimeFormat(iso = DATE) Date date,
																	@RequestParam String checksum) {
		return new DetailsLogPage(application, checksum, date);
	}

	/**
	 * @param v     minute vector
	 * @param width the width of the wished window
	 * @return the first relative index of minute vector so the window of given width will contains
	 *         some statistical data (the empty tail of the graph will be cut off).
	 */
	public static int calculateRelativeWindow(MinuteVector v, int width) {
		int start = 0 - width + 1;
		while (start > -SIZE && v.get(start + width - 1) == 0) start--;
		return Math.max(start, -1439);
	}

	public class DetailsLogPage {

		private final List<LogEntry> entries;

		public DetailsLogPage(String application, String checksum, Date date) {
			Collection<LogEntry> logEntries = entries().
				applicationId(application).
				checksum(checksum).
				date(fromDateFields(date)).
				find(storage);
			entries = Ordering.from(new ByOccurrenceDateComparator()).greatestOf(logEntries, 50);
		}

		public List<LogEntry> getEntries() {
			return entries;
		}
	}
}
