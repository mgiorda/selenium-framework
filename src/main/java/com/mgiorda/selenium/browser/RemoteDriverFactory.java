package com.mgiorda.selenium.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.mgiorda.selenium.BrowserFactory;

public class RemoteDriverFactory implements BrowserFactory {

	private final URL remoteUrl;
	private final Capabilities capabilities;

	public RemoteDriverFactory(String remoteUrl, Capabilities capabilities) {

		if (remoteUrl == null || capabilities == null) {
			throw new IllegalArgumentException("RemoteUrl and capabilities cannot be null");
		}

		try {
			this.remoteUrl = new URL(remoteUrl);

		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
		this.capabilities = capabilities;
	}

	@Override
	public WebDriver newDriver() {

		WebDriver driver = new RemoteWebDriver(remoteUrl, capabilities);

		return driver;
	}

}
