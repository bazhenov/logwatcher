package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.AggregateAttributesVisitor;
import com.farpost.logwatcher.AggregationResult;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

import static com.farpost.logwatcher.storage.LogEntries.entries;

@Controller
public class BackController {

	@Autowired
	private LogStorage storage;

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
}
