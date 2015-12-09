package com.farpost.logwatcher.marshalling;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * JAXB Адаптер сериализующий тип Map<String, String> в XML дерево аттрибутов
 */
public class JaxbAttributesMapAdapter extends XmlAdapter<AttributeList, Map<String, String>> {

	@Override
	public Map<String, String> unmarshal(AttributeList list) throws Exception {
		Map<String, String> map = new HashMap<>();
		for (Attribute attribute : list.getAttributes()) {
			map.put(attribute.getName(), attribute.getValue());
		}
		return map;
	}

	@Override
	public AttributeList marshal(Map<String, String> map) throws Exception {
		return new AttributeList(map.entrySet().stream()
				.map(row -> new Attribute(row.getKey(), row.getValue()))
				.collect(toList())
		);
	}
}
