package com.farpost.logwatcher;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Set;

import static com.farpost.logwatcher.LogEntryBuilder.entry;
import static com.farpost.logwatcher.Severity.debug;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleChecksumCalculatorTest {

	private ChecksumCalculator calculator;

	@BeforeMethod
	public void setUp() throws Exception {
		calculator = new SimpleChecksumCalculator();
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithSameExceptions() {
		Exception exception = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_1", debug, "checksum_1", "application", null,
			new Cause(exception));

		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_2", debug, "checksum_1", "application", null,
			new Cause(exception));

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutExceptionsAndWithSameExistingChecksum() {
		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_1", debug, "checksum_same", "application", null);
		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_2", debug, "checksum_same", "application", null);

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void calculatorShouldCalculateSameChecksumForEntriesWithoutChecksumAndWithSameMessages() {
		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "", "application", null);
		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_same", debug, null, "application", null);

		assertThat(calculator.calculateChecksum(firstEntry), equalTo(calculator.calculateChecksum(secondEntry)));
	}

	@Test
	public void applicationIdAndSeverityHaveHighestPriority() {
		Exception exceptionSame = new RuntimeException();
		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_same", "application_1",
			null, new Cause(exceptionSame));
		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_same", "application_2",
			null, new Cause(exceptionSame));

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void exceptionHasHigherPriorityThanChecksum() {
		Exception exception1 = new RuntimeException();
		Exception exception2 = new IllegalArgumentException();

		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_same", "application_same",
			null, new Cause(exception1));
		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_same", "application_same",
			null, new Cause(exception2));

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
	}

	@Test
	public void existingChecksumHasHigherPriorityThanMessage() {
		LogEntry firstEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_1", "application_same",
			null);

		LogEntry secondEntry = new LogEntryImpl(new Date(), "", "message_same", debug, "checksum_2", "application_same",
			null);

		assertThat(calculator.calculateChecksum(firstEntry), not(equalTo(calculator.calculateChecksum(secondEntry))));
		assertThat(calculator.calculateChecksum(firstEntry), equalTo("77a11fd86c33ba53e92d2d4a9f1109eb"));
	}

	@Test
	public void shouldCollapsePatternBasedMessages() {
		assertAllMessagesHaveSameChecksum(calculator,
			"Maximum execution time of 30 seconds exceeded in /var/www/baza.farpost.ru/rev/20130708-1134/slr/core/src/xml/xmlUtils.class.php:139",
			"Maximum execution time of 30 seconds exceeded in /var/www/baza.farpost.ru/rev/20130710-1156/vendor/pear-pear.dev.loc/search_php_client/FarPost/Search/Client/Document.php:92",
			"Maximum execution time of 30 seconds exceeded in /var/www/baza.farpost.ru/rev/20130716-0737/slr/db/src/sqlStatement.class.php(133) : regexp code:1");

		assertAllMessagesHaveSameChecksum(calculator,
			"Allowed memory size of 268435456 bytes exhausted (tried to allocate 14313983 bytes) in /var/www/baza.farpost.ru/rev/20130709-1602/app/src/viewdir/cacheViewDirSearchFacade.class.php:35",
			"Allowed memory size of 234245234 bytes exhausted (tried to allocate 32423 bytes) in /var/www/baza.farpost.ru/rev/viewdir/cacheViewDirSearchFacade.class.php");

		assertAllMessagesHaveSameChecksum(calculator,
			"Call to undefined method good::getOriginalPartnerSite() in /var/www/baza.farpost.ru/rev/20130716-1357/app/src/template/personal/personal_bulls_dir.inc.php:9",
			"Call to undefined method good::getOriginalPartnerSite() in /var/www/baza.farpost.ru/rev/20130717-1357/app/src/template/personal/personal_bulls_dir.inc.php:19");

		assertAllMessagesHaveDifferentChecksum(calculator,
			"Call to undefined method good::getOriginalPartnerSite() in /var/www/baza.farpost.ru/rev/20130717-1357/app/src/template/personal/personal_bulls_dir.inc.php:19",
			"Call to undefined method good::getPartnerSite() in /var/www/baza.farpost.ru/rev/20130717-1357/app/src/template/personal/personal_bulls_dir.inc.php:19");

		assertAllMessagesHaveSameChecksum(calculator,
			"Call to a member function execInterval() on a non-object in /var/www/baza.farpost.ru/rev/20130717-1139/src/FarPost/Baza/Personal/Notification/Services/GoodNotificationService.php:49",
			"Call to a member function execInterval() on a non-object in /var/www/baza.farpost.ru/rev/20130717-1139/src/FarPost/Baza/Personal/Notification/GoodNotificationService.php:48");

		assertAllMessagesHaveDifferentChecksum(calculator,
			"Call to a member function execInterval() on a non-object in /var/www/baza.farpost.ru/rev/20130717-1139/src/FarPost/Baza/Personal/Notification/Services/GoodNotificationService.php:49",
			"Call to a member function execInterval2() on a non-object in /var/www/baza.farpost.ru/rev/20130717-1139/src/FarPost/Baza/Personal/Notification/Services/GoodNotificationService.php:49");
	}

	public void assertAllMessagesHaveSameChecksum(ChecksumCalculator checksumCalculator, String... messages) {
		Set<String> checksums = getMessageChecksumSet(checksumCalculator, messages);
		assertThat("All messages should be mapped to single checksum", checksums.size(), is(1));
	}

	public void assertAllMessagesHaveDifferentChecksum(ChecksumCalculator checksumCalculator, String... messages) {
		Set<String> checksums = getMessageChecksumSet(checksumCalculator, messages);
		assertThat("All messages should have unique checksum", checksums.size(), is(messages.length));
	}

	private Set<String> getMessageChecksumSet(ChecksumCalculator checksumCalculator, String[] messages) {
		Set<String> checksums = newHashSet();
		for (String message : messages) {
			LogEntry entry = entry().message(message).create();
			checksums.add(checksumCalculator.calculateChecksum(entry));
		}
		return checksums;
	}
}
