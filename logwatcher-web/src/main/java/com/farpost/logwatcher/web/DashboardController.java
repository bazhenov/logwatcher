package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.LogStorage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.farpost.timepoint.Date.today;

@Controller
public class DashboardController {

	private LogStorage storage;

	public DashboardController(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/dashboard")
	public String doDashboard(ModelMap map) {
		map.put("applicationIds", storage.getUniquieApplicationIds(today()));
		return "dashboard";
	}

	@RequestMapping("/widget/dashboard-widget")
	public String doDashboardWidget(@RequestParam String applicationId, ModelMap map) {
		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, today(), Severity.error);
		map.put("info", new ApplicationInfo(applicationId, entries));
		return "widget/dashboard-widget";
	}
}
