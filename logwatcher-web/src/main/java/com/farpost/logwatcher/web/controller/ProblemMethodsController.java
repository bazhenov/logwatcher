package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.listener.TrackProblemMethodsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;

import static com.farpost.logwatcher.listener.TrackProblemMethodsListener.ClusterReference;

@Controller
public class ProblemMethodsController {

	@Autowired
	private TrackProblemMethodsListener listener;

	@RequestMapping("/{applicationId}/problem-methods.json")
	@ResponseBody
	public Set<ClusterReference> handleProblemMethods(
		@SuppressWarnings("UnusedParameters") @PathVariable("applicationId") String applicationId) {

		return listener.getTrackedClusterReferences();
	}
}
