import geb.Browser

this.metaClass.mixin(cuke4duke.GroovyDsl)

Browser browser

Before() {
	//driver = new org.openqa.selenium.firefox.FirefoxDriver();
	driver = new org.openqa.selenium.htmlunit.HtmlUnitDriver();
	browser = new Browser(driver, "http://localhost:8080/")
}

After() {
	browser.close()
	browser.quit()
}