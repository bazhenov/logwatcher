package com.farpost.logwatcher.web;

import com.google.common.base.Splitter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class AttributeFormatterImpl implements AttributeFormatter {

	private Map<String, String> templates = newHashMap();
	private final Pattern advancedPattern = Pattern.compile("(?:\\$\\[([^]]+)\\]|\\*)");

	/**
	 * Пример задания шаблонов
	 * <pre>
	 *   app1/attr1->some data: [*]|app2/attr2->&lt;b&gt;*&lt;/b&gt;
	 *   |app2/attr3->&lt;a href="$[attr4]"&gt;*&lt;/a&gt;
	 * </pre>
	 *
	 * Важно отметить, что форма записи {@code}$[attr4]{@code} будет работать только для {@link #format(String, String, Map)},
	 * так как {@link #format(String, String, String)} не принимает необходимые данные об атрибутах. Кроме того,
	 * форматирование не будет работать в случае, если в Map'е отсутствунт хотя-бы один нужный атрибут.
	 *
	 * @param templates шаблоны
	 */
	public AttributeFormatterImpl(String templates) {
		for (String p : Splitter.on('|').split(templates)) {
			if (!p.isEmpty()) {
				String[] parts = p.split("->", 2);
				this.templates.put(parts[0].trim(), parts[1].trim());
			}
		}
	}

	@Override
	public String format(String applicationId, String attributeName, String attributeValue) {
		return format(applicationId, attributeName, singletonMap(attributeName, attributeValue));
	}

	@Override public String format(String applicationId, String attributeName, Map<String, String> attributeValues) {
		String key = applicationId + "/" + attributeName;
		String mainValue = escapeHtml(attributeValues.get(attributeName));
		if (templates.containsKey(key)) {
			Matcher matcher = advancedPattern.matcher(templates.get(key));
			StringBuffer result = new StringBuffer();
			while(matcher.find()) {
				if(matcher.group().equals("*")) {
					matcher.appendReplacement(result, mainValue);
				} else if(attributeValues.containsKey(matcher.group(1))) {
					matcher.appendReplacement(result, escapeHtml(attributeValues.get(matcher.group(1))));
				} else {
					return mainValue;
				}
			}
			matcher.appendTail(result);
			return result.toString();
		} else {
			return mainValue;
		}
	}
}
