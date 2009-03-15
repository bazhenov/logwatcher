package org.bazhenov.logging.storage;

import org.bazhenov.logging.LogEntry;

/**
 * ��� �������������� �������� ������������ ��������� ����
 * {@link LogEntry} � ������ ������������� ���������� ������.
 */
public class LogStorageException extends Exception {

	public LogStorageException(Throwable e) {
		super(e);
	}
}
