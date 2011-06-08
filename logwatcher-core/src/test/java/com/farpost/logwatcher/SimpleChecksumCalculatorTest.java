package com.farpost.logwatcher;

import com.farpost.timepoint.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleChecksumCalculatorTest {

	private ChecksumCalculator checksumCalculator;

	@BeforeMethod
	public void setUp() throws Exception {
		checksumCalculator = new SimpleChecksumCalculator();
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithSameExceptions() {
		Exception exception = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_1", Severity.debug,
			"checksum_1", "application", null,
			new Cause(exception));

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_2", Severity.debug,
			"checksum_2", "application", null,
			new Cause(exception));

		assertThat(checksumCalculator.calculateChecksum(firstEntry), equalTo(checksumCalculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutExceptionsAndWithSameExistingChecksum() {
		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_1", Severity.debug,
			"checksum_same", "application", null);

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_2", Severity.debug,
			"checksum_same", "application", null);

		assertThat(checksumCalculator.calculateChecksum(firstEntry), equalTo(checksumCalculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutChecksumAndWithSameMessages() {
		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"", "application", null);

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			null, "application", null);

		assertThat(checksumCalculator.calculateChecksum(firstEntry), equalTo(checksumCalculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void applicationIdAndSeverityHaveHighestPriority() {
		Exception exceptionSame = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_same", "application_1", null, new Cause(exceptionSame));

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_same", "application_2", null, new Cause(exceptionSame));

		assertThat(checksumCalculator.calculateChecksum(firstEntry), not(equalTo(checksumCalculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void exceptionHasHigherPriorityThanChecksum() {
		Exception exception1 = new RuntimeException();
		Exception exception2 = new IllegalArgumentException();

		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_same", "application_same", null, new Cause(exception1));

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_same", "application_same", null, new Cause(exception2));

		assertThat(checksumCalculator.calculateChecksum(firstEntry), not(equalTo(checksumCalculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void existingChecksumHasHigherPriorityThanMessage() {
		LogEntry firstEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_1", "application_same", null);

		LogEntry secondEntry = new LogEntryImpl(DateTime.now(), "", "message_same", Severity.debug,
			"checksum_2", "application_same", null);

		assertThat(checksumCalculator.calculateChecksum(firstEntry), not(equalTo(checksumCalculator.calculateChecksum(secondEntry))));
	}


}
