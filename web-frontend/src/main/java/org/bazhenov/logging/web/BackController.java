package org.bazhenov.logging.web;

import com.farpost.timepoint.Date;
import org.bazhenov.logging.AggregatedAttribute;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import static org.bazhenov.logging.storage.LogEntries.entries;

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

	@RequestMapping("/service/attributes")
	public String handleAttributes(ModelMap map, @RequestParam("checksum") String checksum,
	                               @RequestParam("date") String date)
		throws LogStorageException, InvalidCriteriaException, ParseException {

		Date dt = new Date(format.get().parse(date).getTime());
		List<AggregatedLogEntry> entries = entries().
			checksum(checksum).
			date(dt).
			find(storage);
		if ( entries.size() > 0 ) {
			map.addAttribute("attributes", entries.get(0).getAttributes());
		} else {
			map.addAttribute("attributes", new HashMap<String, AggregatedAttribute>());
		}
		return "service/attributes";
	}
}
