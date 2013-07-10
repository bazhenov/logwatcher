package com.farpost.logwatcher;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.joda.time.DateTime.now;

public class SimpleChecksumCalculatorTest {

	private ChecksumCalculator calculator;

	@BeforeMethod
	public void setUp() throws Exception {
		calculator = new SimpleChecksumCalculator();
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithSameExceptions() {
		Exception exception = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_1", Severity.debug,
			"checksum_1", "application", null,
			new Cause(exception));

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_2", Severity.debug,
			"checksum_2", "application", null,
			new Cause(exception));

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutExceptionsAndWithSameExistingChecksum() {
		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_1", Severity.debug,
			"checksum_same", "application", null);

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_2", Severity.debug,
			"checksum_same", "application", null);

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutChecksumAndWithSameMessages() {
		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"", "application", null);

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			null, "application", null);

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void applicationIdAndSeverityHaveHighestPriority() {
		Exception exceptionSame = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_same", "application_1", null, new Cause(exceptionSame));

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_same", "application_2", null, new Cause(exceptionSame));

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void exceptionHasHigherPriorityThanChecksum() {
		Exception exception1 = new RuntimeException();
		Exception exception2 = new IllegalArgumentException();

		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_same", "application_same", null, new Cause(exception1));

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_same", "application_same", null, new Cause(exception2));

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void existingChecksumHasHigherPriorityThanMessage() {
		LogEntry firstEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_1", "application_same", null);

		LogEntry secondEntry = new LogEntryImpl(now(), "", "message_same", Severity.debug,
			"checksum_2", "application_same", null);

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
		assertThat(calculator.calculateChecksum(firstEntry), equalTo("77a11fd86c33ba53e92d2d4a9f1109eb"));
	}

	@Test
	public void shouldCollapsePatternBasedMessages() {
		LogEntry firstEntry = entry()
			.message("Maximum execution time of 30 seconds exceeded in /var/www/baza.farpost.ru/rev/20130708-1134/slr/core/src/xml/xmlUtils.class.php:139")
			.create();

		LogEntry secondEntry = entry()
			.message("Maximum execution time of 30 seconds exceeded in /var/www/baza.farpost.ru/rev/20130710-1156/vendor/pear-pear.dev.loc/search_php_client/FarPost/Search/Client/Document.php:92")
			.create();

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));

		firstEntry = entry()
			.message("Allowed memory size of 268435456 bytes exhausted (tried to allocate 14313983 bytes) in /var/www/baza.farpost.ru/rev/20130709-1602/app/src/viewdir/cacheViewDirSearchFacade.class.php:35")
			.create();

		secondEntry = entry()
			.message("Allowed memory size of 234245234 bytes exhausted (tried to allocate 32423 bytes) in /var/www/baza.farpost.ru/rev/viewdir/cacheViewDirSearchFacade.class.php")
			.create();

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}
}
