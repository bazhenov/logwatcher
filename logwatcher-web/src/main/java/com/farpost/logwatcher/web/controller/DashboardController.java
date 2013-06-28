package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.Checksum;
import com.farpost.logwatcher.Cluster;
import com.farpost.logwatcher.cluster.ClusterDao;
import com.farpost.logwatcher.statistics.ActiveApplicationsService;
import com.farpost.logwatcher.statistics.ClusterStatistic;
import com.google.common.base.Function;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;

@Controller
public class DashboardController {

	@Autowired
	private ActiveApplicationsService activeApplicationsService;

	@Autowired
	private ClusterStatistic clusterStatistic;

	@Autowired
	private ClusterDao clusterDao;

	@RequestMapping("/dashboard")
	@ModelAttribute("p")
	public DashboardPage doDashboard() {
		return new DashboardPage();
	}

	public class DashboardPage {

		public Set<String> getApplications() {
			return activeApplicationsService.getActiveApplications();
		}

		public Collection<Cluster> getClusters(final String applicationId) {
			return from(clusterStatistic.getActiveClusterChecksums(applicationId, new LocalDate()))
				.transform(new Function<Checksum, Cluster>() {
					@Override
					public Cluster apply(Checksum input) {
						return clusterDao.findCluster(applicationId, input);
					}
				})
				.toList();
		}
	}
}
