package com.farpost.logwatcher.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
public class LayoutController {

	@RequestMapping("/layout")
	public String doLayout(ModelMap map) {
		map.put("date", new Date());
		return "layout/main";
	}
}
