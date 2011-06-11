package com.farpost.logwatcher.marshalling;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * JAXB Адаптер сериализующий тип Map<String, String> в XML дерево аттрибутов
 */
public class JaxbAttributesMapAdapter extends XmlAdapter<AttributeList, Map<String, String>> {

	@Override
	public Map<String, String> unmarshal(AttributeList list) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		for (Attribute attribute : list.getAttributes()) {
			map.put(attribute.getName(), attribute.getValue());
		}
		return map;
	}

	@Override
	public AttributeList marshal(Map<String, String> map) throws Exception {
		ArrayList<Attribute> list = new ArrayList<Attribute>(map.size());
		for (Map.Entry<String, String> row : map.entrySet()) {
			list.add(new Attribute(row.getKey(), row.getValue()));
		}
		return new AttributeList(list);
	}
}
