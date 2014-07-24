package com.farpost.logwatcher.web;

import com.google.common.base.Splitter;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class AttributeFormatterImpl implements AttributeFormatter {

	private Map<String, String> templates = newHashMap();

	/**
	 * Пример задания шаблонов:
	 * <pre>
	 *   app1/attr1-><b>*</b>|app2/attr2-><b>*</b>
	 * </pre>
	 *
	 * @param templates шаблоны
	 */
	public AttributeFormatterImpl(String templates) {
		for (String p : Splitter.on('|').split(templates)) {
			if (!p.isEmpty()) {
				String[] parts = p.split("->", 2);
				this.templates.put(parts[0], parts[1]);
			}
		}
	}

	@Override
	public String format(String applicationId, String name, String value) {
		String key = applicationId + "/" + name;
		if (templates.containsKey(key)) {
			return templates.get(key).replaceAll("\\*", escapeHtml(value));
		} else {
			return escapeHtml(value);
		}
	}
}
