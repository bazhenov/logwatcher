package org.bazhenov.logging;

import java.util.Comparator;

public class AttributeValueComparator implements Comparator<AttributeValue> {

	public int compare(AttributeValue attr1, AttributeValue attr2) {
		return attr2.getCount() - attr1.getCount();
	}
}
