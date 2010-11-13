package com.farpost.logwatcher.web;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.ByOccurenceCountComparator;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import org.bazhenov.logging.Severity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static com.farpost.logwatcher.web.vm.FeedViewModel.groupByApplication;
import static com.farpost.timepoint.Date.today;
import static java.util.Collections.sort;

@Controller
public class DashboardController {

	private LogStorage storage;

	public DashboardController(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/dashboard")
	public String doDashboard(ModelMap map) throws LogStorageException, InvalidCriteriaException {
		List<AggregatedEntry> allEntries = storage.getAggregatedEntries(today(), Severity.error);
		List<ApplicationInfo> infos = groupEntriesByApplicationId(allEntries);
		map.put("infos", infos);
		return "dashboard";
	}

	@RequestMapping("/widget/dashboard-widget")
	public String doDashboardWidget(@RequestParam String applicationId, ModelMap map)
		throws LogStorageException, InvalidCriteriaException {

		List<AggregatedEntry> entries = storage.getAggregatedEntries(applicationId, today(), Severity.error);
		map.put("info", new ApplicationInfo(applicationId, entries));
		return "widget/dashboard-widget";
	}

	private List<ApplicationInfo> groupEntriesByApplicationId(List<AggregatedEntry> allEntries) {
		List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
		for (String applicationId : groupByApplication(allEntries)) {
			List<AggregatedEntry> applicationsEntries = FeedController.filter(allEntries, applicationId);
			sort(applicationsEntries, new ByOccurenceCountComparator());
			ApplicationInfo info = new ApplicationInfo(applicationId, applicationsEntries);
			infos.add(info);
		}
		return infos;
	}
}
