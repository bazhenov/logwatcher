package com.farpost.logwatcher.transport;

import com.farpost.logwatcher.LogEntry;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class CompositeEventListenerTest {

	@Test
	public void listenerShouldDelegateEventToAllSubscribers() {
		LogEntryListener m1 = mock(LogEntryListener.class);
		LogEntryListener m2 = mock(LogEntryListener.class);
		CompositeEventListener listener = new CompositeEventListener(asList(m1, m2));

		LogEntry e = entry().create();
		listener.onEntry(e);
		verify(m1).onEntry(e);
		verify(m2).onEntry(e);
	}

	@Test
	public void listenerShouldBeCalledEventIfPreviousListenerThrownAnException() {
		LogEntryListener thrower = mock(LogEntryListener.class);
		LogEntryListener m2 = mock(LogEntryListener.class);
		CompositeEventListener listener = new CompositeEventListener(asList(thrower, m2));

		doThrow(new RuntimeException("Fuck you that's why"))
			.when(thrower).onEntry(any(LogEntry.class));
		LogEntry e = entry().create();
		listener.onEntry(e);
		verify(m2).onEntry(e);
	}
}