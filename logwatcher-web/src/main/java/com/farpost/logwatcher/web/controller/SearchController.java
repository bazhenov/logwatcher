package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.web.page.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SearchController extends AbstractController {

	@RequestMapping("/search")
	public ModelAndView handleSearch(@RequestParam(required = false) String q) {
		SearchPage p = new SearchPage(q);
		return modelAndView(p);
	}
}
