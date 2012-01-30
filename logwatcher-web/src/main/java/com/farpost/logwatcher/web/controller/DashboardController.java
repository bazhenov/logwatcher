package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.web.ApplicationInfo;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

	@Autowired
	private LogStorage storage;

	public DashboardController() {
	}

	public DashboardController(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/dashboard")
	public String doDashboard(ModelMap map) {
		map.put("applicationIds", storage.getUniqueApplicationIds(LocalDate.now()));
		return "dashboard";
	}

	@RequestMapping("/widget/dashboard-widget")
	public String doDashboardWidget(@RequestParam String applicationId, ModelMap map) {
		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, LocalDate.now(), Severity.error);
		map.put("info", new ApplicationInfo(applicationId, entries));
		return "widget/dashboard-widget";
	}
}
