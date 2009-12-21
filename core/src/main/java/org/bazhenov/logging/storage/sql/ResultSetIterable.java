package org.bazhenov.logging.storage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultSetIterable implements Iterable<String> {

	private final ResultSet result;

	public ResultSetIterable(ResultSet result) {
		this.result = result;
	}

	public Iterator<String> iterator() {
		return new ResultIterator();
	}

	public class ResultIterator implements Iterator<String> {

		public boolean hasNext() {
			try {
				return !result.isLast() && !result.isAfterLast();
			} catch ( SQLException e ) {
				throw new RuntimeException(e);
			}
		}

		public String next() {
			try {
				result.next();
				return result.getString(1);
			} catch ( SQLException e ) {
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
