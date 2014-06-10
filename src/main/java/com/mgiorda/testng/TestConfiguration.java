package com.mgiorda.testng;

import com.mgiorda.selenium.Browser;
import com.mgiorda.selenium.WebDriverHandler;

public class TestConfiguration {

	private final WebDriverHandler driverHandler;

	private int waitTimeOut;

	private Browser browser;

	public TestConfiguration(WebDriverHandler driverHandler) {

		if (driverHandler == null) {
			throw new IllegalArgumentException("DriverHandler constructor parameter cannot be null");
		}

		this.driverHandler = driverHandler;
	}

	public int getWaitTimeOut() {
		return waitTimeOut;
	}

	public Browser getBrowser() {
		return browser;
	}

	public void setBrowser(Browser browser) {
		this.browser = browser;
	}

	public WebDriverHandler getDriverHandler() {
		return driverHandler;
	}

	public void setWaitTimeOut(int waitTimeOut) {
		this.waitTimeOut = waitTimeOut;
	}
}
