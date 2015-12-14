package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ByDayStatistic;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.farpost.logwatcher.statistics.MinuteVector;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.web.AttributeFormatter;
import com.farpost.logwatcher.web.LogEntryClassifier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

import static com.farpost.logwatcher.Checksum.fromHexString;
import static com.farpost.logwatcher.statistics.MinuteVector.SIZE;
import static com.farpost.logwatcher.storage.LogEntries.entries;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.joda.time.LocalDate.fromDateFields;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class BackController {

	@Autowired
	private LogStorage storage;

	@Autowired
	private ClusterStatistic stat;

	@Autowired
	private ClusterDao clusterDao;

	@Autowired
	private AttributeFormatter formatter;

	@Autowired
	private LogEntryClassifier entryClassifier;

	private static final Logger log = LoggerFactory.getLogger(BackController.class);

	public BackController() {
	}

	public BackController(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/service/content")
	public String handleAttributes(
			@RequestParam String application,
			@RequestParam("checksum") String checksum,
			@RequestParam @DateTimeFormat(iso = DATE) LocalDate date,
			ModelMap map
	) throws LogStorageException, InvalidCriteriaException, ParseException {

		AggregateAttributesVisitor visitor = new AggregateAttributesVisitor();

		AggregationResult result = entries()
				.applicationId(application)
				.checksum(checksum)
				.date(date)
				.walk(storage, visitor);

		Multimap<String, AggregatedAttributeEntry> attributes = ArrayListMultimap.create();
		for(AggregatedAttribute a : result.getAttributeMap().values()) {
			for(AttributeValue v : a.getValues()) {
				String formatted = formatter.format(application, a.getName(), v.getValue());
				attributes.put(a.getName(), new AggregatedAttributeEntry(v.getValue(), formatted, v.getCount()));
			}
		}
		map.addAttribute("attributes", attributes.asMap());
		return "service/aggregated-entry-content";
	}

	@RequestMapping(value = "/cluster/{application}/{checksum}", method = POST)
	public ResponseEntity<String> handleClusterSave(@PathVariable String application, @PathVariable String checksum,
																									@RequestParam final String issueKey, @RequestParam final String title,
																									@RequestParam final String description) {
		try {
			clusterDao.changeCluster(application, fromHexString(checksum), input -> {
				input.setTitle(title);
				input.setIssueKey(issueKey);
				input.setDescription(description);
			});
			return new ResponseEntity<>("Ok", OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
		}

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
																	@RequestParam @DateTimeFormat(iso = DATE) LocalDate date,
																	@RequestParam String checksum) {
		log.error("Date registered: {}", date);
		return new DetailsLogPage(application, checksum, date.toDate());
	}

	/**
	 * @param v     minute vector
	 * @param width the width of the wished window
	 * @return the first relative index of minute vector so the window of given width will contains
	 * some statistical data (the empty tail of the graph will be cut off).
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
			entries = Ordering.from(new ByOccurrenceDateComparator()).greatestOf(logEntries, 500);
		}

		public List<LogEntry> getEntries() {
			return entries;
		}

		public AttributeFormatter getFormatter() {
			return formatter;
		}

		public LogEntryClassifier getClassifier() {
			return entryClassifier;
		}
	}

	public static class AggregatedAttributeEntry {
		private final String value;
		private final String formattedValue;
		private final int count;

		public AggregatedAttributeEntry(String value, String formattedValue, int count) {
			this.value = value;
			this.formattedValue = formattedValue;
			this.count = count;
		}

		public String getValue() {
			return value;
		}

		public String getFormattedValue() {
			return formattedValue;
		}

		public int getCount() {
			return count;
		}
	}
}
