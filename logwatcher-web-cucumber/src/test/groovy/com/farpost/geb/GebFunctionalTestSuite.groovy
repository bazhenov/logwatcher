package com.farpost.geb

import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite

abstract class GebFunctionalTestSuite {

	protected WebDriver driver
	protected final String applicationUri = "http://localhost:8080"

	@BeforeSuite
	public void setUp() {
		driver = System.getProperty("browser").equals("firefox") ? new FirefoxDriver() : new HtmlUnitDriver()
	}

	@AfterSuite
	public void tearDown() {
		if(!Boolean.getBoolean("preventClose")) {
			driver.close()
		}
	}
}
