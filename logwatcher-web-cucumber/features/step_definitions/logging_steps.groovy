import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.farpost.logwatcher.logback.LogWatcherAppender
import org.slf4j.Logger

this.metaClass.mixin(cuke4duke.GroovyDsl)


Given(~/приложение "([^\"]+)" залогировало ошибку "([^\"]+)"/) { String applicationName, String errorMessage ->
	sleep(1000)
	getLogger(applicationName).error(errorMessage)
}

Given(~/приложение "([^\"]+)" залогировало warning "([^\"]+)"/) { String applicationName, String warningMessage ->
	sleep(1000)
	getLogger(applicationName).warn(warningMessage)
}

Given(~/приложение "([^\"]+)" залогировало исключение "([^\"]+)"/) { String applicationName,
																																		 String exceptionName ->
	sleep(1000)
	getLogger(applicationName).error("message", Class.forName(exceptionName).newInstance())
}

Appender<ILoggingEvent> getAppender(String applicationId) {
	if (appenders.containsKey(applicationId)) {
		return appenders.get(applicationId)
	} else {
		LogWatcherAppender appender = new LogWatcherAppender()
		appender.setAddress("0.0.0.0:6578")
		appender.setApplicationId(applicationId)
		appender.start()
		appenders.put(applicationId, appender)
		return appender
	}
}

Logger getLogger(String applicationId) {
	Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(applicationId)
	logger.setLevel(Level.DEBUG)
	if (logger.getAppender(applicationId) == null) {
		logger.addAppender(getAppender(applicationId))
	}
	loggers.add(logger)
	return logger
}