package com.farpost.logwatcher.geb

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.farpost.logwatcher.logback.LogWatcherAppender
import com.gargoylesoftware.htmlunit.BrowserVersion
import geb.Configuration
import geb.testng.GebReportingTest
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterSuite

import static org.slf4j.LoggerFactory.getLogger

abstract class LogwatcherFunctionalTestSuite extends GebReportingTest {

	private static final String APPLICATION_URL = System.getProperty("it.location", "http://localhost:8181")
	private static final String REPORT_DIRECTORY = System.getProperty("reportsDir", "./report")
	private static final int PORT = 6578
	private static WebDriver driver;
	private HashMap<String, Appender<ILoggingEvent>> appenders = new HashMap<String, Appender<ILoggingEvent>>()
	protected Logger logger = getLogger(getClass())

	@Override
	Configuration createConf() {
		def config = new Configuration()
		config.driver = createDriver()
		config.baseUrl = APPLICATION_URL
		config.reportsDir = getReportDir()
		return config
	}

	private WebDriver createDriver() {
		if (driver == null) {
			if (System.getProperty("browser").equals("firefox")) {
				logger.info("Firefox driver selected")
				driver = new FirefoxDriver()
			} else {
				logger.info("HtmlUnit driver selected")
				driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_17)
				driver.javascriptEnabled = true
			}
		}

		return driver
	}

	@AfterSuite
	public void tearDown() {
		if (!Boolean.getBoolean("preventClose")) {
			driver.close()
		}
	}

	File getReportDir() {
		File file = new File(REPORT_DIRECTORY)
		if (file.exists() || file.mkdirs()) {
			return file
		} else {
			logger.warn("Can't create {} directory for test reports", REPORT_DIRECTORY)
			return null;
		}
	}

	private Appender<ILoggingEvent> getAppender(String applicationId) {
		if (appenders.containsKey(applicationId)) {
			return appenders.get(applicationId)
		} else {
			LogWatcherAppender appender = new LogWatcherAppender()
			appender.setAddress("0.0.0.0:" + PORT)
			appender.setApplicationId(applicationId)
			appender.start()
			appenders.put(applicationId, appender)
			return appender
		}
	}

	protected ch.qos.logback.classic.Logger getLogger(String applicationId) {
		applicationId = applicationId.toLowerCase()
		//noinspection GroovyAssignabilityCheck
		ch.qos.logback.classic.Logger logger = LoggerFactory.getLogger(applicationId)
		logger.setLevel(Level.DEBUG)
		if (logger.getAppender(applicationId) == null) {
			logger.addAppender(getAppender(applicationId))
		}
		return logger
	}
}
