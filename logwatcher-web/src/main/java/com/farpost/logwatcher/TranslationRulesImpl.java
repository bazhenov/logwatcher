package com.farpost.logwatcher;

import com.farpost.logwatcher.storage.*;
import com.farpost.timepoint.Date;
import com.farpost.logwatcher.Severity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.farpost.timepoint.Date.today;

public class TranslationRulesImpl {

	ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	@Criteria("severity")
	public LogEntryMatcher severity(String severity) {
		return new SeverityMatcher(Severity.forName(severity));
	}

	@Criteria("at")
	public LogEntryMatcher applicationId(String applicationId) {
		return new ApplicationIdMatcher(applicationId);
	}

	@Criteria("caused-by")
	public LogEntryMatcher causeType(String expectedCauseType) {
		return new CauseTypeMatcher(expectedCauseType);
	}

	@Criteria("occurred")
	public LogEntryMatcher date(String dateString) throws ParseException {
		if ( dateString.startsWith("last") ) {
			// Парсим строчку вида occured: last X (days|weeks|month)
			String[] parts = dateString.split(" ", 3);
			int period;
			String quantificatorStr;
			try {
				period = Integer.parseInt(parts[1]);
				quantificatorStr = parts[2];
			} catch ( NumberFormatException e ) {
				period = 1;
				quantificatorStr = parts[1];
			}
			Date from;
			Date to = today();
			if ( "days".equals(quantificatorStr) ) {
				from = to.minusDay(period);
			} else if ( "weeks".equals(quantificatorStr) ) {
				from = to.minusDay(period*7);
			} else if ( "month".equals(quantificatorStr) ) {
				from = to.minusMonth(period);
			} else {
				throw new IllegalArgumentException("Invalid quantificator: " + quantificatorStr);
			}
			return new DateMatcher(from, to);

		} else if ( dateString.contains("/") ) {
			// Парсим строчку вида occured: 2009-12-19/2009-12-21
			String[] parts = dateString.split("/", 2);
			Date from = new Date(dateFormat.get().parse(parts[0]));
			Date to = new Date(dateFormat.get().parse(parts[1]));
			return new DateMatcher(from, to);

		} else {
			// Парсим строчку вида occured: 2009-12-19
			Date date = new Date(dateFormat.get().parse(dateString));
			return new DateMatcher(date);
		}
	}

	@DefaultCriteria
	public LogEntryMatcher attribute(String name, String value) {
		if ( !name.startsWith("@") ) {
			throw new IllegalArgumentException("Attribute names must starts with '@'");
		}
		name = name.substring(1);
		return new AttributeValueMatcher(name, value);
	}
}
