package org.bazhenov.logging.web;

import org.bazhenov.logging.AggregatedAttribute;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

import static com.farpost.timepoint.Date.today;
import static org.bazhenov.logging.storage.LogEntries.entries;

@Controller
public class BackController {

	private LogStorage storage;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/service/attributes")
	public String handleAttributes(ModelMap map, @RequestParam("checksum") String checksum) throws
		LogStorageException, InvalidCriteriaException {

		List<AggregatedLogEntry> entries = entries().
			checksum(checksum).
			date(today()).
			find(storage);
		if ( entries.size() > 0 ) {
			map.addAttribute("attributes", entries.get(0).getAttributes());
		}else{
			map.addAttribute("attributes", new HashMap<String, AggregatedAttribute>());
		}
		return "service/attributes";
	}
}
