package org.bazhenov.logging.storage.sql;

import org.bazhenov.logging.LogEntry;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultSetIterable implements Iterable<LogEntry> {

	private final ResultSet result;
	private final Marshaller marshaller;

	public ResultSetIterable(ResultSet result, Marshaller marshaller) {
		this.result = result;
		this.marshaller = marshaller;
	}

	public Iterator<LogEntry> iterator() {
		return new ResultIterator();
	}

	public class ResultIterator implements Iterator<LogEntry> {

		public boolean hasNext() {
			try {
				return !result.isLast();
			} catch ( SQLException e ) {
				throw new RuntimeException(e);
			}
		}

		public LogEntry next() {
			try {
				result.next();
				String xml = result.getString(1);
				return marshaller.unmarshall(xml);
			} catch ( SQLException e ) {
				throw new RuntimeException(e);
			} catch ( MarshallerException e ) {
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
