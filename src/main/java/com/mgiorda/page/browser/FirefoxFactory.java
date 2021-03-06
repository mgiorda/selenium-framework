package com.mgiorda.page.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class FirefoxFactory extends AbstractBrowserFactory {

    @Override
    public WebDriver newDriver() {
        DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();
        WebDriver driver = new FirefoxDriver(firefoxCapabilities);

        return driver;
    }
}
