package org.bazhenov.logging.storage;

import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.bazhenov.logging.marshalling.Marshaller;
import org.bazhenov.logging.marshalling.MarshallerException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class MySqlLogStorage implements LogStorage {

	private final DataSource dataSource;
	private final Marshaller marshaller;

	public MySqlLogStorage(DataSource dataSource, Marshaller marshaller) {
		this.dataSource = dataSource;
		this.marshaller = marshaller;
	}

	public void writeEntry(LogEntry entry) throws LogStorageException {
		Connection conn = null;
		PreparedStatement st = null;
		try {
			conn = dataSource.getConnection();
			st = conn.prepareStatement(
				"INSERT INTO log_entry (`date`, `checksum`, `group`, `text`, `count`, `last_date`) " +
					"VALUES(?, ?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE `last_date` = ?, `count` = `count` + 1");

			DateTime date = entry.getDate();
			st.setDate(1, date(date.getDate()));
			st.setString(2, entry.getChecksum());
			st.setString(3, entry.getGroup());
			st.setString(4, marshaller.marshall(entry));
			st.setInt(5, 1);
			st.setTimestamp(6, timestamp(date));
			st.setTimestamp(7, timestamp(date));
			st.executeUpdate();
		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} finally {
			close(st);
			close(conn);
		}
	}

	public List<AggregatedLogEntry> getEntries(Date date) throws LogStorageException {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<AggregatedLogEntry> list = new ArrayList<AggregatedLogEntry>();
		try {
			conn = dataSource.getConnection();
			st = conn.prepareStatement("SELECT * FROM `log_entry` WHERE `date` = ?");
			st.setDate(1, date(date));
			rs = st.executeQuery();

			while ( rs.next() ) {
				list.add(createEntry(rs));
			}

		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		} catch ( MarshallerException e ) {
			throw new LogStorageException(e);
		} finally {
			close(rs);
			close(st);
			close(conn);
		}
		return list;
	}

	public int getEntryCount(Date date) throws LogStorageException {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet set = null;
		try {
			conn = dataSource.getConnection();
			st = conn.prepareStatement("SELECT SUM(`count`) FROM `log_entry` WHERE `date` = ?");
			st.setDate(1, new java.sql.Date(date.asTimestamp()));

			set = st.executeQuery();
			if ( set.first() ) {
				return set.getInt(1);
			}
			return 0;
		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		} finally {
			close(set);
			close(st);
			close(conn);
		}
	}

	private java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	private Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	private AggregatedLogEntry createEntry(ResultSet rs) throws MarshallerException, SQLException {
		LogEntry sampleEntry = marshaller.unmarshall(rs.getString("text"));
		DateTime lastTime = new DateTime(rs.getTimestamp("last_date"));
		int count = rs.getInt("count");
		AggregatedLogEntry aggregate = new AggregatedLogEntryImpl(sampleEntry, lastTime, count);
		return aggregate;
	}

	private void close(ResultSet set) throws LogStorageException {
		try {
			if ( set != null ) {
				set.close();
			}
		} catch ( SQLException e ) {
			throw new LogStorageException(e);
		}
	}

	private void close(PreparedStatement st) throws LogStorageException {
		if ( st != null ) {
			try {
				st.close();
			} catch ( SQLException e ) {
				throw new LogStorageException(e);
			}
		}
	}

	private void close(Connection conn) throws LogStorageException {
		if ( conn != null ) {
			try {
				conn.close();
			} catch ( SQLException e ) {
				throw new LogStorageException(e);
			}
		}
	}
}
