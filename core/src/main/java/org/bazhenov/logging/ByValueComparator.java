package org.bazhenov.logging;

import java.util.*;
import static java.lang.Math.signum;

public class ByValueComparator implements Comparator<AttributeValue> {

	public int compare(AttributeValue a, AttributeValue b) {
		if ( a.equals(b) ) {
			return 0;
		}else{
			int order = b.getCount() - a.getCount();
			return order == 0 ? a.getValue().compareTo(b.getValue()) : order;
		}
	}
}
