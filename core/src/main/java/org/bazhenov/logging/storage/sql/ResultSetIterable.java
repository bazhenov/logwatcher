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

		private int hasNexCount = 0;
		private int nextCount = 0;
		
		public boolean hasNext() {
			try {
				return result.next();
			} catch ( SQLException e ) {
				throw new RuntimeException(e);
			}
		}

		public String next() {
			try {
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
