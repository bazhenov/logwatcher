package com.farpost.logwatcher.storage.lucene;

import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.LogEntry;
import com.farpost.logwatcher.Severity;
import com.farpost.logwatcher.Visitor;
import com.farpost.logwatcher.storage.InvalidCriteriaException;
import com.farpost.logwatcher.storage.LogEntryMatcher;
import com.farpost.logwatcher.storage.LogStorage;
import com.farpost.logwatcher.storage.LogStorageException;
import com.farpost.logwatcher.storage.spi.AnnotationDrivenMatcherMapperImpl;
import com.farpost.logwatcher.storage.spi.MatcherMapper;
import com.farpost.logwatcher.storage.spi.MatcherMapperException;
import com.farpost.timepoint.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

import static com.farpost.logwatcher.storage.lucene.LuceneUtils.*;
import static java.lang.System.nanoTime;
import static org.apache.lucene.index.IndexWriter.MaxFieldLength;
import static org.apache.lucene.search.BooleanClause.Occur;

public class LuceneBdbLogStorage implements LogStorage {

	private final Directory directory;
	private final MatcherMapper<Query> matcherMapper;
	private final Map<Integer, LogEntry> entries = new HashMap<Integer, LogEntry>();
	private int nextId = 1;

	private long lastCommitTime = nanoTime();
	private volatile int commitThreshold = 5;
	private final IndexWriter writer;
	private volatile SearcherReference searcherRef;

	public LuceneBdbLogStorage(Directory directory) throws IOException {
		this.directory = directory;
		matcherMapper = new AnnotationDrivenMatcherMapperImpl<Query>(new LuceneMatcherMapperRules());
		writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), MaxFieldLength.UNLIMITED);
		searcherRef = reopenSearcher(directory);
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

	@Override
	public void writeEntry(LogEntry entry) throws LogStorageException {
		try {
			int entryId = getNextId();
			Document document = createLuceneDocument(entry, entryId);
			writer.addDocument(document);
			entries.put(entryId, entry);

			commitChangesIfNeeded();
		} catch (IOException e) {
			throw new LogStorageException(e);
		}
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
		document.add(storedTerm("id", Integer.toString(entryId)));
		return document;
	}

	@Override
	public List<LogEntry> findEntries(Collection<LogEntryMatcher> criterias)
		throws LogStorageException, InvalidCriteriaException {

		try {
			Query query = createLuceneQuery(criterias);

			/**
			 * Сохраняем ссылку на tuple({@link IndexSearcher}, {@link FieldCache}) локально
			 * чтобы избежать race condition
			 */
			SearcherReference ref = searcherRef;
			Searcher searcher = ref.getSearcher();
			String[] ids = ref.getIdFieldCache();

			TopDocs topDocs = searcher.search(query, 100);

			List<LogEntry> result = new ArrayList<LogEntry>(topDocs.scoreDocs.length);
			for (ScoreDoc doc : topDocs.scoreDocs) {
				int entryId = Integer.parseInt(ids[doc.doc]);
				result.add(entries.get(entryId));
			}

			return result;

		} catch (MatcherMapperException e) {
			throw new InvalidCriteriaException(e);
		} catch (CorruptIndexException e) {
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
		return 0;	//To change body of implemented methods use File | Settings | File Templates.
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
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void walk(Collection<LogEntryMatcher> criterias, Visitor<LogEntry> visitor)
		throws LogStorageException, InvalidCriteriaException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Set<String> getUniquieApplicationIds(Date date) {
		return null;	//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<AggregatedEntry> getAggregatedEntries(String applicationId, Date date, Severity severity)
		throws LogStorageException, InvalidCriteriaException {
		return null;	//To change body of implemented methods use File | Settings | File Templates.
	}
}
