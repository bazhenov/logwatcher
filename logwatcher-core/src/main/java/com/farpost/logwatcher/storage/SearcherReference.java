package com.farpost.logwatcher.storage;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.Thread.yield;

/**
 * Tuple хранящий ссылки на {@link org.apache.lucene.search.IndexSearcher} и связанный с ним
 * {@link org.apache.lucene.search.FieldCache} по полю id
 * <p/>
 * Для корректного закрытия {@link org.apache.lucene.search.IndexSearcher}'а и
 * {@link org.apache.lucene.index.IndexReader}'а, используется метод {@link this#close()}.
 */
final class SearcherReference implements Closeable {

	private final IndexReader indexReader;
	private final AtomicInteger referenceCount = new AtomicInteger(1);
	private final Searcher searcher;
	private final List<int[]> idFieldCache;

	SearcherReference(IndexReader indexReader, Searcher searcher, List<int[]> idFieldCache) {
		this.indexReader = indexReader;
		this.searcher = searcher;
		this.idFieldCache = idFieldCache;
	}

	Searcher getSearcher() {
		return searcher;
	}

	public int getDocumentId(int doc) {
		for (int[] ids : idFieldCache) {
			if (ids.length > doc) {
				return ids[doc];
			} else {
				doc = doc - ids.length;
			}
		}

		throw new RuntimeException("Document not found");
	}

	public <T> T withSearch(SearcherTask<T> task) {
		try {
			if (acquire() <= 1) {
				throw new IllegalStateException("Reader reference already closed. Race condition probably?");
			}
			return task.call(this);
		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			release();
		}
	}

	/**
	 * Блокирует клиента до того момента когда reference count на данном reader'е
	 * будет равен 0, затем закрывает данный экземпляр.
	 * <p/>
	 * Обратите внимание что после того момента как этот метод вернет управление reference
	 * count может опять изменится, поэтому клиенты должны самы выполнить необходимую
	 * синхронизацию, если это им необходимо.
	 */
	@Override
	public synchronized void close() {
		referenceCount.decrementAndGet();
		while (referenceCount.get() > 0) {
			yield();
		}
		closeQuietly(searcher);
		closeQuietly(indexReader);
	}

	/**
	 * Декрементирует счетчик клиентов, которые сейчас используют данный экземпляр reader'а и возврщает
	 * общее количество клиентов без учета текущего клиента (то есть считается что текущий клиент уже
	 * не использует данный экземпляр).
	 * <p/>
	 * Таким образом данный метод возвращает число большее либо равное
	 * {@code 0}. Ноль возвращается если клиент который вызвал этот метод был последним, кто использовал
	 * данный экземпляр reader'а.
	 *
	 * @return количество клиентов которые используют данный экземпляр reader'а
	 */
	private int release() {
		int refCount = referenceCount.decrementAndGet();
		if (refCount < 0) {
			throw new IllegalStateException("Invalid ref count found: " + refCount);
		}
		return refCount;
	}

	/**
	 * Инкрементирует счетчик клиентов, которые сейчас используют данный экземпляр
	 * reader'а, и возвращает общее количество клиентов с учетом клиента который вызвал этот метод.
	 * Таким образом этот метод всегда возвращает число больше либо равное {@code 1}.
	 *
	 * @return количество клиентов которые используют данный экземпляр reader'а
	 */
	private int acquire() {
		int refCount = referenceCount.incrementAndGet();
		if (refCount < 2) {
			throw new RuntimeException("Invalid ref count found: " + refCount);
		}
		return refCount;
	}
}
