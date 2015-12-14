package com.farpost.logwatcher.web;

import java.util.Map;

public interface AttributeFormatter {

	String format(String applicationId, String attributeName, String attributeValue);

	String format(String applicationId, String attributeName, Map<String, String> attributeValues);
}
