package com.farpost.logwatcher.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

class ResultSetIterable implements Iterable<String> {

	private final ResultSet result;

	public ResultSetIterable(ResultSet result) {
		this.result = result;
	}

	public Iterator<String> iterator() {
		return new ResultIterator();
	}

	private class ResultIterator implements Iterator<String> {

		public boolean hasNext() {
			try {
				return result.next();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		public String next() {
			try {
				return result.getString(1);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
