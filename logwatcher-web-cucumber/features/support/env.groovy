import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.gargoylesoftware.htmlunit.BrowserVersion
import geb.Browser
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.slf4j.Logger
import static java.lang.Boolean.getBoolean

this.metaClass.mixin(cuke4duke.GroovyDsl)

WebDriver driver

Before() {
	String applicationUrl = System.getProperty("it.location")
	appenders = new HashMap<String, Appender<ILoggingEvent>>()
	loggers = new HashSet<Logger>();

	if (System.getProperty("browser").equals("firefox")) {
		driver = new FirefoxDriver()
	} else {
		driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_3)
		driver.javascriptEnabled = true;
	}
	browser = new Browser(driver, applicationUrl)
}

After() {
	loggers.each { Logger logger ->
		logger.detachAndStopAllAppenders()
	}

	if (!getBoolean("preventClose")) {
		driver.close();
	}
}

