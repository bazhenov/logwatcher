package org.bazhenov.logging;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Вспомогательный класс учавствующий в сериализации объектов {@link LogEntryImpl}
 *
 * @see org.bazhenov.logging.JaxbAttributesMapAdapter
 */
@XmlType
public class Attribute {

	@XmlAttribute
	private final String name;

	@XmlAttribute
	private final String value;

	@Deprecated
	public Attribute() {
		this(null, null);
	}

	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
