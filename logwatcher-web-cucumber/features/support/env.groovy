import geb.Browser

this.metaClass.mixin(cuke4duke.GroovyDsl)

Before() {
	driver = new org.openqa.selenium.firefox.FirefoxDriver();
	//driver = new org.openqa.selenium.htmlunit.HtmlUnitDriver();
	browser = new Browser(driver, "http://baza.farpost.ru")
}

After() {
	browser.close()
}