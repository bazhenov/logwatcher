package com.farpost.logwatcher.web.controller;

import com.farpost.logwatcher.web.ViewNameAwarePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

public class AbstractController {

	@Autowired
	private ApplicationContext context;

	protected ModelAndView modelAndView(ViewNameAwarePage page) {
		initializeBean(page);
		return new ModelAndView(page.getViewName(), "p", page);
	}

	private void initializeBean(Object page) {
		AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
		factory.autowireBeanProperties(page, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(page, "null");
	}

	protected ModelAndView modelAndView(String viewName, Object page) {
		initializeBean(page);
		return new ModelAndView(viewName, "p", page);
	}
}
