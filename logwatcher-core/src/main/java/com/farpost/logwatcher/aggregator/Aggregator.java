package com.farpost.logwatcher.aggregator;


import com.farpost.logwatcher.AggregatedEntry;
import com.farpost.logwatcher.marshalling.MarshallerException;
import com.farpost.logwatcher.storage.LogEntryMatcher;

import java.util.Collection;

/**
 * Имплементации этого интферйеса занимаются тем, что фильтрую и аггрегируют объекты типа
 * {@link com.farpost.logwatcher.LogEntry} в объекты типы {@link AggregatedEntry}.
 * <p/>
 * На входе аггрегатор принимает итератор по строчкам и условия фильтрации. Итератор по строчкам
 * нужен для того чтобы можно было делегировать десериализацию объектов типа {@code LogEntry}
 * в параллельные потоки. Это необходимо для того, чтобы не блокировать основной поток, который
 * занимается тем, что читает данные из хранилища.
 */
public interface Aggregator {

	/**
	 * Аггрегирует и фильтрует объекты типа {@link com.farpost.logwatcher.LogEntry}. На выходе
	 * возвращает аггрегированные объекты типа {@link com.farpost.logwatcher.AggregatedEntry}.
	 *
	 * @param entries	итератор по строковым представлениям объектов {@link com.farpost.logwatcher.LogEntryImpl}
	 * @param matchers условия отбора
	 * @return коллекцию отфильрованных и саггрегированных записей
	 * @throws MarshallerException в случае ошибок десирализации
	 */
	Collection<AggregatedEntry> aggregate(Iterable<byte[]> entries, Collection<LogEntryMatcher> matchers)
		throws MarshallerException;
}
