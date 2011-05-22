package com.farpost.geb

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.farpost.logwatcher.logback.LogWatcherAppender
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite

abstract class GebFunctionalTestSuite {

	protected WebDriver driver
	protected final String applicationUri = "http://localhost:8080"
	private int port = 6578
	private String applicationId = "foobar"
	private HashMap<String, Appender<ILoggingEvent>> appenders = new HashMap<String, Appender<ILoggingEvent>>()

	@BeforeSuite
	public void setUp() {
		driver = System.getProperty("browser").equals("firefox") ? new FirefoxDriver() : new HtmlUnitDriver()
	}

	@AfterSuite
	public void tearDown() {
		if (!Boolean.getBoolean("preventClose")) {
			driver.close()
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
		if (logger.getAppender(applicationId) == null) {
			logger.addAppender(getAppender(applicationId))
		}
		return logger
	}
}
