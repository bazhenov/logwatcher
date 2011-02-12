package com.farpost.logwatcher.storage;

import com.farpost.logwatcher.*;
import com.farpost.logwatcher.marshalling.BinaryMarshallerV1;
import com.farpost.logwatcher.marshalling.Marshaller;
import com.farpost.logwatcher.storage.spi.AnnotationDrivenMatcherMapperImpl;
import com.farpost.logwatcher.storage.spi.MatcherMapper;
import com.farpost.logwatcher.storage.spi.MatcherMapperException;
import com.farpost.timepoint.Date;
import com.farpost.timepoint.DateTime;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static com.farpost.logwatcher.storage.LuceneUtils.*;
import static com.farpost.timepoint.Date.january;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.nanoTime;
import static org.apache.lucene.index.IndexWriter.MaxFieldLength;
import static org.apache.lucene.search.BooleanClause.Occur;

public class LuceneSqlLogStorage implements LogStorage {

	private final Directory directory;
	private final MatcherMapper<Query> matcherMapper;
	private final Map<Integer, LogEntry> entries = new HashMap<Integer, LogEntry>();
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
		this.directory = directory;
		matcherMapper = new AnnotationDrivenMatcherMapperImpl<Query>(new LuceneMatcherMapperRules());
		writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), MaxFieldLength.UNLIMITED);
		searcherRef = reopenSearcher(directory);
		this.jdbc = new JdbcTemplate(dataSource);
		this.marshaller = new BinaryMarshallerV1();
		this.aggregateEntryMapper = new CreateAggregatedEntryRowMapper(marshaller);
		nextId = jdbc.queryForInt("SELECT MAX(id) + 1 FROM entry");
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
			java.sql.Date entryDate = date(impl.getDate());
			byte[] marshalledEntry = marshaller.marshall(impl);

			jdbc.update("INSERT INTO entry (id, value) VALUES (?, ?)", entryId, marshalledEntry);

			int affectedRows = jdbc.update(
				"UPDATE aggregated_entry SET count = count + 1, last_time = ? WHERE date = ? AND checksum = ?",
				entryTimestamp, entryDate, checksum);
			if (affectedRows == 0) {
				jdbc.update(
					"INSERT INTO aggregated_entry (date, checksum, last_time, category, severity, application_id, count, content) VALUES (?, ?, ?, ?, ?, ?, 1, ?)",
					entryDate, checksum, entryTimestamp, impl.getCategory(),
					impl.getSeverity().getCode(), impl.getApplicationId(), marshalledEntry);
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

	private static java.sql.Date date(Date date) {
		return new java.sql.Date(date.asTimestamp());
	}

	private static Timestamp timestamp(DateTime date) {
		return new Timestamp(date.asTimestamp());
	}

	/**
	 * Переоткрывает {@link IndexSearcher} для заданной директории, а также выполняет предзагрузку
	 * {@link FieldCache}'а для поля {@code id}.
	 *
	 * @param directory директория источник данных
	 * @return tuple состоящий из {@link IndexSearcher}'а и {@link FieldCache}'а.
	 * @throws IOException в случае ошибки ввода/вывода открытии {@link IndexReader}'а или {@link IndexSearcher}'а.
	 */
	private static SearcherReference reopenSearcher(Directory directory) throws IOException {
		IndexReader indexReader = IndexReader.open(directory, true);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		String[] ids = FieldCache.DEFAULT.getStrings(indexReader, "id");
		return new SearcherReference(indexReader, searcher, ids);
	}

	public void setCommitThreshold(int commitThreshold) {
		this.commitThreshold = commitThreshold;
	}

	private int getNextId() {
		return nextId++;
	}

	/**
	 * Коммитит изменения в индекс если с момента последнего коммита прошло более {@link #commitThreshold} секунд.
	 *
	 * @throws IOException в случае ошибки ввода/вывода при коммите изменений в индекс
	 */
	private synchronized void commitChangesIfNeeded() throws IOException {
		if (commitThreshold <= 0 || lastCommitTime < nanoTime() - commitThreshold * 1e+9) {
			writer.commit();
			searcherRef = reopenSearcher(directory);
			lastCommitTime = nanoTime();
		}
	}

	private Document createLuceneDocument(LogEntry entry, int entryId) {
		Document document = new Document();
		document.add(term("applicationId", normalizeTerm(entry.getApplicationId())));
		document.add(term("date", normilizeDate(entry.getDate())));
		document.add(term("severity", entry.getSeverity().name()));
		document.add(term("checksum", normalizeTerm(entry.getChecksum())));
		document.add(storedTerm("id", Integer.toString(entryId)));
		return document;
	}

	@Override
	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		int[] ids = findEntriesIds(criterias);

		List<LogEntry> result = new ArrayList<LogEntry>();
		for (int id : ids) {
			byte[] data = jdbc.queryForObject("SELECT value FROM entry where id = ?", byte[].class, id);
			result.add(marshaller.unmarshall(data));
		}
		return result;
	}

	private int[] findEntriesIds(Collection<LogEntryMatcher> criterias) {
		try {
			Query query = createLuceneQuery(criterias);

			/**
			 * Сохраняем ссылку на tuple({@link org.apache.lucene.search.IndexSearcher}, {@link org.apache.lucene.search.FieldCache}) локально
			 * чтобы избежать race condition
			 */
			SearcherReference ref = searcherRef;
			Searcher searcher = ref.getSearcher();
			String[] ids = ref.getIdFieldCache();

			TopDocs topDocs = searcher.search(query, 100);

			int result[] = new int[topDocs.scoreDocs.length];
			for (int i = 0; i < topDocs.scoreDocs.length; i++) {
				result[i] = Integer.parseInt(ids[topDocs.scoreDocs[i].doc]);
			}
			return result;

		} catch (MatcherMapperException e) {
			throw new LogStorageException(e);
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	private Query createLuceneQuery(Collection<LogEntryMatcher> criterias) throws MatcherMapperException {
		BooleanQuery query = new BooleanQuery();
		for (LogEntryMatcher matcher : criterias) {
			Query q = matcherMapper.handle(matcher);
			if (q == null) {
				throw new InvalidCriteriaException("Unable to map matcher of type: " + matcher.getClass().getName());
			}
			query.add(q, Occur.SHOULD);
		}
		return query;
	}

	@Override
	public int removeOldEntries(Date date) throws LogStorageException {
		try {
			checkNotNull(date);
			Collection<LogEntryMatcher> criterias = new ArrayList<LogEntryMatcher>();
			criterias.add(new DateMatcher(january(1, 1980), date.minusDay(1)));
			int[] ids;
			int recordsRemoved = 0;

			do {
				ids = findEntriesIds(criterias);
				for (int id : ids) {
					jdbc.update("DELETE FROM entry WHERE id = ?", id);
					writer.deleteDocuments(new Term("id", Integer.toString(id)));
				}
				recordsRemoved += ids.length;
				commitChangesIfNeeded();
			} while (ids.length > 0);

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
	public int countEntries(Collection<LogEntryMatcher> criterias) throws LogStorageException, InvalidCriteriaException {
		try {
			Searcher searcher = searcherRef.getSearcher();
			Query query = criterias.isEmpty()
				? new MatchAllDocsQuery()
				: createLuceneQuery(criterias);
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
			writer.deleteDocuments(new Term("checksum", normalizeTerm(checksum)));

			commitChangesIfNeeded();
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
	}

	@Override
	public void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Set<String> getUniquieApplicationIds(Date date) {
		checkNotNull(date);
		List<String> ids = jdbc.queryForList("SELECT application_id FROM aggregated_entry WHERE date = ?", String.class,
			date(date));
		return new HashSet<String>(ids);
	}

	@Override
	public List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date, Severity severity)
		throws LogStorageException, InvalidCriteriaException {
		return jdbc.query(
			"SELECT checksum, application_id, last_time, count, severity, content FROM aggregated_entry WHERE application_id = ? AND date = ? AND severity >= ?",
			aggregateEntryMapper, applicationId, date(date), severity.getCode());
	}
}
