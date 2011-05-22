import org.openqa.selenium.WebDriver
import geb.Browser
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import static java.lang.Boolean.getBoolean
import static java.lang.System.getProperty

this.metaClass.mixin(cuke4duke.GroovyDsl)

WebDriver driver

Before() {
	if(getProperty("browser").equals("firefox")) {
		driver = new FirefoxDriver();
	} else {
		driver = new HtmlUnitDriver();
	}
	browser = new Browser("http://localhost:8080/")
}

After() {
	if(!getBoolean("preventClose")) {
		driver.close();
	}
}