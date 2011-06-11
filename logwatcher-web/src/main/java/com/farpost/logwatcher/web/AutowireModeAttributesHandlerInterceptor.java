package com.farpost.logwatcher.web;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class AutowireModeAttributesHandlerInterceptor implements HandlerInterceptor, ApplicationContextAware {

	private AutowireCapableBeanFactory factory;
	private final static Logger log = getLogger(AutowireModeAttributesHandlerInterceptor.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		factory = applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
		throws Exception {
		if (modelAndView == null) {
			return;
		}
		for (Map.Entry<String, Object> row : modelAndView.getModel().entrySet()) {
			Object o = row.getValue();
			String attributeName = row.getKey();
			if (findAnnotation(o.getClass(), Component.class) != null) {
				log.debug("Autowiring model attribute {} of type {}", attributeName, o.getClass());
				autowireBean(o);
				if (o instanceof ViewNameAwarePage) {
					String viewName = ((ViewNameAwarePage) o).getViewName();
					if (viewName != null) {
						log.debug("Accepting view name {} from page object {}", viewName, o.getClass());
						modelAndView.setViewName(viewName);
					}
				}
			}
		}
	}

	private void autowireBean(Object o) {
		factory.autowireBeanProperties(o, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(o, null);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
		throws Exception {
	}
}
