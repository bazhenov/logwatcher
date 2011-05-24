package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.web.page.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

	@RequestMapping("/search")
	@ModelAttribute("p")
	public SearchPage handleSearch(@RequestParam(required = false) String q) {
		return new SearchPage(q);
	}
}
