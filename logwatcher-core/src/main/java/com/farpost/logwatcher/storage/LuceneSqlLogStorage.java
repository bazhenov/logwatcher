package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.marshalling.Jaxb2Marshaller;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.spi.AnnotationDrivenMatcherMapperImpl;
import com.farpost.logwatcher.storage.spi.MatcherMapper;
import com.farpost.logwatcher.storage.spi.MatcherMapperException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

import static com.farpost.logwatcher.storage.LuceneUtils.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.nanoTime;
import static org.apache.lucene.index.IndexWriter.MaxFieldLength;
import static org.apache.lucene.search.BooleanClause.Occur;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

public class LuceneSqlLogStorage implements LogStorage, Closeable {

	private static final Sort DATETIME_SORT = new Sort(new SortField("datetime", SortField.LONG, true));
	private final MatcherMapper<Query> matcherMapper;
	private int nextId;

	private final JdbcTemplate jdbc;
	private final IndexWriter writer;
	private final ChecksumCalculator checksumCalculator = new SimpleChecksumCalculator();

	private static final Logger log = LoggerFactory.getLogger(LuceneSqlLogStorage.class);

	private volatile int commitThreshold = 5;
	private volatile SearcherReference searcherRef;
	private volatile long lastCommitTime = nanoTime();
	private Marshaller marshaller;
	private final RowMapper<AggregatedEntry> aggregateEntryMapper;

	public LuceneSqlLogStorage(Directory directory, DataSource dataSource) throws IOException {
		matcherMapper = new AnnotationDrivenMatcherMapperImpl<Query>(new LuceneMatcherMapperRules());
		writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), MaxFieldLength.UNLIMITED);
		searcherRef = reopenSearcher();
		this.jdbc = new JdbcTemplate(dataSource);
		this.marshaller = new Jaxb2Marshaller();
		this.aggregateEntryMapper = new CreateAggregatedEntryRowMapper(marshaller);
		nextId = jdbc.queryForInt("SELECT MAX(id) + 1 FROM entry");
	}

	@Override
	public synchronized void close() throws IOException {
		writer.commit();
		writer.close();
	}

	// TODO: объективных причин для сериализации потоков при записи нет
	@Override
	public synchronized void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			checkNotNull(entry);
			// TODO: контрольная сумма должна расчитыватся где-то в другом месте. Это слой хранения данных
			LogEntryImpl impl = (LogEntryImpl) entry;
			final String checksum = checksumCalculator.calculateChecksum(entry);
			impl.setChecksum(checksum);

			int entryId = getNextId();

			Timestamp entryTimestamp = timestamp(impl.getDate());
			Date entryDate = date(impl.getDate().toLocalDate());
			byte[] marshaledEntry = marshaller.marshall(impl);

			jdbc.update("INSERT INTO entry (id, date, checksum,value) VALUES (?, ?, ?, ?)", entryId, entryDate,
					checksum, marshaledEntry);

			int affectedRows = jdbc.update(
					"UPDATE aggregated_entry SET count = count + 1, last_time = ? WHERE date = ? AND checksum = ?",
					entryTimestamp, entryDate, checksum);
			if (affectedRows == 0) {
				jdbc.update(
						"INSERT INTO aggregated_entry (date, checksum, last_time, category, severity, application_id, count, content) VALUES (?, ?, ?, ?, ?, ?, 1, ?)",
						entryDate, checksum, entryTimestamp, impl.getCategory(),
						impl.getSeverity().getCode(), impl.getApplicationId(), marshaledEntry);
			}

			if (log.isDebugEnabled()) {
				log.debug("Entry with checksum: " + checksum + " wrote to database");
			}

			writer.addDocument(createLuceneDocument(entry, entryId));

			commitChangesIfNeeded();

		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	private static Date date(LocalDate date) {
		return new Date(date.toDateTimeAtStartOfDay().getMillis());
	}

	private static Timestamp timestamp(DateTime date) {
		return new Timestamp(date.getMillis());
	}

	/**
	 * Переоткрывает {@link IndexSearcher} для заданной директории, а также выполняет предзагрузку
	 * {@link FieldCache}'а для поля {@code id}.
	 *
	 * @return tuple состоящий из {@link IndexSearcher}'а и {@link FieldCache}'а.
	 * @throws IOException в случае ошибки ввода/вывода открытии {@link IndexReader}'а или {@link IndexSearcher}'а.
	 */
	private SearcherReference reopenSearcher() throws IOException {
		IndexReader indexReader = writer.getReader();
		IndexSearcher searcher = new IndexSearcher(indexReader);
		return new SearcherReference(indexReader, searcher, gatherDocumentIds(searcher));
	}

	public void setCommitThreshold(int commitThreshold) {
		this.commitThreshold = commitThreshold;
	}

	private synchronized int getNextId() {
		return nextId++;
	}

	/**
	 * Коммитит изменения в индекс если с момента последнего коммита прошло более {@link #commitThreshold} секунд.
	 *
	 * @throws IOException в случае ошибки ввода/вывода при коммите изменений в индекс
	 */
	private synchronized void commitChangesIfNeeded() throws IOException {
		boolean realTimeModeEnabled = commitThreshold <= 0;
		boolean commitThresholdReached = lastCommitTime < nanoTime() - commitThreshold * 1e+9;

		if (realTimeModeEnabled || commitThresholdReached) {
			writer.commit();
			searcherRef = reopenSearcher();
			lastCommitTime = nanoTime();
		}
	}

	private Document createLuceneDocument(LogEntry entry, int entryId) {
		Document document = new Document();
		document.add(term("applicationId", normalize(entry.getApplicationId())));
		document.add(term("date", normalizeDate(entry.getDate())));
		document.add(numeric("datetime", entry.getDate().getMillis()));
		document.add(text("message", entry.getMessage()));
		document.add(term("severity", entry.getSeverity().name()));
		document.add(term("checksum", normalize(entry.getChecksum())));
		Cause cause = entry.getCause();
		while (cause != null) {
			document.add(term("caused-by", normalize(cause.getType())));
			document.add(text("stacktrace", cause.getStackTrace()));
			cause = cause.getCause();
		}
		document.add(term("id", Integer.toString(entryId)));
		for (Map.Entry<String, String> row : entry.getAttributes().entrySet()) {
			document.add(term("@" + row.getKey(), normalize(row.getValue())));
		}
		return document;
	}

	@Override
	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criteria)
			throws LogStorageException, InvalidCriteriaException {
		return walk(criteria, new CollectingVisitor<LogEntry>());
	}

	private Integer[] findEntriesIds(Collection<LogEntryMatcher> criteria) {
		return findEntriesIds(criteria, null);
	}

	private Integer[] findEntriesIds(Collection<LogEntryMatcher> criteria, Sort sort) {
		try {
			Query query = createLuceneQuery(criteria);

			/**
			 * Сохраняем ссылку на tuple({@link org.apache.lucene.search.IndexSearcher}, {@link org.apache.lucene.search.FieldCache}) локально
			 * чтобы избежать race condition
			 */
			SearcherReference ref = searcherRef;
			Searcher searcher = ref.getSearcher();

			TopDocs topDocs = sort != null
					? searcher.search(query, null, 100, DATETIME_SORT)
					: searcher.search(query, null, 100);

			Integer result[] = new Integer[topDocs.scoreDocs.length];
			for (int i = 0; i < topDocs.scoreDocs.length; i++) {
				result[i] = ref.getDocumentId(topDocs.scoreDocs[i].doc);
			}
			return result;

		} catch (MatcherMapperException e) {
			throw new LogStorageException(e);
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	private Query createLuceneQuery(Collection<LogEntryMatcher> criteria) throws MatcherMapperException {
		BooleanQuery query = new BooleanQuery();
		for (LogEntryMatcher matcher : criteria) {
			Query q = matcherMapper.handle(matcher);
			if (q == null) {
				throw new InvalidCriteriaException("Unable to map matcher of type: " + matcher.getClass().getName());
			}
			query.add(q, Occur.MUST);
		}
		return query;
	}

	@Override
	public int removeOldEntries(LocalDate date) throws LogStorageException {
		try {
			checkNotNull(date);
			Collection<LogEntryMatcher> criteria = new ArrayList<LogEntryMatcher>();
			criteria.add(new DateMatcher(new LocalDate(0), date));
			Integer[] ids;
			int recordsRemoved = 0;

			while ((ids = findEntriesIds(criteria)).length > 0) {
				jdbc.update("DELETE FROM entry WHERE id IN (" + arrayToCommaDelimitedString(ids) + ")");
				for (int id : ids) {
					writer.deleteDocuments(new Term("id", Integer.toString(id)));
				}
				recordsRemoved += ids.length;
				commitChangesIfNeeded();
			}

			return recordsRemoved;

		} catch (DataAccessException e) {
			throw new LogStorageException(e);
		} catch (CorruptIndexException e) {
			throw new LogStorageException(e);
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	@Override
	public int countEntries(Collection<LogEntryMatcher> criteria) throws LogStorageException, InvalidCriteriaException {
		try {
			Searcher searcher = searcherRef.getSearcher();
			Query query = criteria.isEmpty()
					? new MatchAllDocsQuery()
					: createLuceneQuery(criteria);
			return searcher.search(query, 1).totalHits;
		} catch (MatcherMapperException e) {
			throw new LogStorageException(e);
		} catch (IOException e) {
			throw new LogStorageException(e);
		}

	}

	@Override
	public void removeEntriesWithChecksum(String checksum) throws LogStorageException {
		try {
			writer.deleteDocuments(new Term("checksum", normalize(checksum)));
			jdbc.update("DELETE FROM entry WHERE checksum = ?", checksum);
			jdbc.update("DELETE FROM aggregated_entry WHERE checksum = ?", checksum);

			commitChangesIfNeeded();
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	@Override
	public <T> T walk(Collection<LogEntryMatcher> criteria, Visitor<LogEntry, T> visitor)
			throws LogStorageException, InvalidCriteriaException {
		Integer[] ids = findEntriesIds(criteria, DATETIME_SORT);
		if (ids.length > 0) {
			String idString = arrayToCommaDelimitedString(ids);

			List<byte[]> rows = jdbc.queryForList("SELECT value FROM entry WHERE id IN ( " + idString + " )", byte[].class);
			for (byte[] data : rows) {
				visitor.visit(marshaller.unmarshall(data));
			}
		}
		return visitor.getResult();
	}

	@Override
	public Set<String> getUniqueApplicationIds(LocalDate date) {
		checkNotNull(date);
		List<String> ids = jdbc.queryForList("SELECT application_id FROM aggregated_entry WHERE date = ? GROUP BY application_id",
				String.class, date(date));
		return new HashSet<String>(ids);
	}

	@Override
	public List<AggregatedEntry> getAggregatedEntries(String applicationId, LocalDate date, Severity severity)
			throws LogStorageException, InvalidCriteriaException {
		return jdbc.query(
				"SELECT checksum, application_id, last_time, count, severity, content FROM aggregated_entry WHERE application_id = ? AND date = ? AND severity >= ?",
				aggregateEntryMapper, applicationId, date(date), severity.getCode());
	}

	private static List<int[]> gatherDocumentIds(IndexSearcher searcher) throws IOException {
		int documentCount = searcher.maxDoc();
		if (documentCount > 0) {
			GatherDocumentIdsCollector collector = new GatherDocumentIdsCollector("id");
			searcher.search(new MatchAllDocsQuery(), collector);
			return collector.getValues();
		} else {
			return Collections.emptyList();
		}
	}
}
