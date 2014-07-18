package com.farpost.logwatcher;

import java.util.Date;
import java.util.Map;

/**
 * Обьект представляющий собой запись лога. Запись лога описывается датой, группой, описанием,
 * важностью ({@link Severity}), контрольной суммой и причиной ({@link Cause}).
 * <p/>
 * Severity описывается соответствуюшим enum'ом и необходима для определения серьезности ошибки.
 * <p/>
 * Контрольная сумма нужна для группировки нескольких записей лога в одну. Например, один и тот же
 * exception должен иметь одинаковую контрольную сумму для того чтобы можно было сгруппировать
 * исключительные ситуации для получения статистической информации.
 */
public interface LogEntry {

	Date getDate();

	String getMessage();

	Severity getSeverity();

	Cause getCause();

	String getChecksum();

	String getApplicationId();

	Map<String, String> getAttributes();

	String getGroup();
}
