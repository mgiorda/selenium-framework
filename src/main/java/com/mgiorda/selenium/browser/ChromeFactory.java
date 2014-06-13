package com.mgiorda.selenium.browser;

import java.io.File;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.mgiorda.commons.ClasspathUtil;
import com.mgiorda.selenium.BrowserFactory;

public class ChromeFactory implements BrowserFactory {

	public ChromeFactory(Map<OperativeSystem, String> driverPropertiesByOS) {

		if (driverPropertiesByOS == null) {
			throw new IllegalArgumentException("DriverPropertiesByOS cannot be null");
		}

		OperativeSystem currentOS = OperativeSystem.getCurrentOS();
		String driverProperty = driverPropertiesByOS.get(currentOS);

		if (driverProperty == null) {
			throw new IllegalStateException(String.format("Couldn't found chromeDriverProperty for current OperativeSystem: %s", currentOS));
		}

		File chromeFile = ClasspathUtil.getClasspathFile(driverProperty);

		System.setProperty("webdriver.chrome.driver", chromeFile.getAbsolutePath());
	}

	@Override
	public WebDriver newDriver() {
		DesiredCapabilities chromeCapabilities = DesiredCapabilities.chrome();
		WebDriver driver = new ChromeDriver(chromeCapabilities);

		return driver;
	}

}
