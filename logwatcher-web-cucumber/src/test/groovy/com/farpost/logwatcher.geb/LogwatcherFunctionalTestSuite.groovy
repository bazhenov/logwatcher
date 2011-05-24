package com.farpost.logwatcher.geb

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.farpost.logwatcher.logback.LogWatcherAppender
import geb.testng.GebReportingTest
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.testng.annotations.AfterSuite

abstract class LogwatcherFunctionalTestSuite extends GebReportingTest {

	private static final String APPLICATION_URL = System.getProperty("it.location")
	private static final String REPORT_DIRECTORY = System.getProperty("reportDirectory")
	private static final int port = 6578
	private HashMap<String, Appender<ILoggingEvent>> appenders = new HashMap<String, Appender<ILoggingEvent>>()
	Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(LogwatcherFunctionalTestSuite.class)

	@Override
	WebDriver createDriver() {
		if (System.getProperty("browser").equals("firefox")) {
			logger.info("Firefox driver selected")
			return new FirefoxDriver()
		} else {
			logger.info("HtmlUnit driver selected")
			def driver = new HtmlUnitDriver()
			driver.javascriptEnabled = true
			return driver
		}
	}

	@Override
	String getBaseUrl() {
		return APPLICATION_URL;
	}

	@AfterSuite
	public void tearDown() {
		if (!Boolean.getBoolean("preventClose")) {
			driver.close()
		}
	}

	@Override
	File getReportDir() {
		File file = new File(REPORT_DIRECTORY)
		if (file.exists() || file.mkdirs()) {
			return file
		} else {
			logger.warn("Can't create " + REPORT_DIRECTORY + " directory for test reports")
			return null;
		}
	}

	private Appender<ILoggingEvent> getAppender(String applicationId) {
		if (appenders.containsKey(applicationId)) {
			return appenders.get(applicationId)
		} else {
			LogWatcherAppender appender = new LogWatcherAppender()
			appender.setAddress("0.0.0.0:" + port)
			appender.setApplicationId(applicationId)
			appender.start()
			appenders.put(applicationId, appender)
			return appender
		}
	}

	protected Logger getLogger(String applicationId) {
		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(applicationId)
		logger.setLevel(Level.DEBUG)
		if (logger.getAppender(applicationId) == null) {
			logger.addAppender(getAppender(applicationId))
		}
		return logger
	}
}
