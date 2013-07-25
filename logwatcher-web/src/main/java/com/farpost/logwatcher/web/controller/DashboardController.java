package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static org.joda.time.LocalDate.now;

@Controller
public class DashboardController {

	@Autowired
	private ClusterStatistic clusterStatistic;

	@RequestMapping("/dashboard")
	@ModelAttribute("p")
	public DashboardPage doDashboard() {
		return new DashboardPage();
	}

	public class DashboardPage {

		public Set<String> getApplications() {
			return clusterStatistic.getActiveApplications();
		}

		public Map<String, Integer> getApplicationStatistics(String application) {
			Map<String, Integer> result = newHashMap();
			Map<Severity, Integer> s = clusterStatistic.getSeverityStatistics(application, now());
			for (Map.Entry<Severity, Integer> r : s.entrySet()) {
				result.put(r.getKey().name(), r.getValue());
			}
			return result;
		}
	}
}
