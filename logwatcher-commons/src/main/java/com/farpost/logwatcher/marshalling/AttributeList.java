package com.farpost.logwatcher.marshalling;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Вспомогательный класс для JAXB используемый при сериализации атрибутов записи {@link LogEntry}
 *
 * @see JaxbAttributesMapAdapter
 */
@XmlType
public class AttributeList {

	@XmlElement(name = "attribute")
	private final List<Attribute> attributes;

	@Deprecated
	public AttributeList() {
		this(null);
	}

	public AttributeList(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}
}
