package org.bazhenov.logging;

/**
 * Простая имплементация {@link ChecksumCalculator}, которая вычисляет контрольную сумму основываясь
 * на application id, severity и root cause записи лога.
 */
public class SimpleChecksumCalculator implements ChecksumCalculator {

	public String calculateChecksum(LogEntry entry) {
		String checksum = entry.getApplicationId() + ":" + entry.getSeverity();
		Cause cause = entry.getCause();
		if ( cause != null ) {
			checksum += ":" + cause.getRootCause().getType();
		}else{
			checksum += ":" + entry.getMessage();
		}
		return checksum;
	}
}
