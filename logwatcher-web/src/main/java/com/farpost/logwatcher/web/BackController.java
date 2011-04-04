package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregateAttributesVisitor;
import com.farpost.logwatcher.AggregationResult;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.timepoint.Date;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.farpost.logwatcher.storage.LogEntries.entries;

@Controller
public class BackController {

	private LogStorage storage;
	private final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/service/content")
	public String handleAttributes(ModelMap map, @RequestParam("checksum") String checksum,
																 @RequestParam("date") String date)
		throws LogStorageException, InvalidCriteriaException, ParseException {

		AggregateAttributesVisitor visitor = new AggregateAttributesVisitor();
		Date dt = new Date(format.get().parse(date).getTime());

		AggregationResult result = entries().
			checksum(checksum).
			date(dt).
			walk(storage, visitor);

		map.addAttribute("attributes", result.getAttributeMap());
		map.addAttribute("entry", result.getFirstEntry());
		return "service/aggregated-entry-content";
	}
}
