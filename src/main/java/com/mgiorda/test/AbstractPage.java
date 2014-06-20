package com.mgiorda.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.ISuite;

import com.google.common.base.Predicate;
import com.mgiorda.annotations.PageContext;
import com.mgiorda.annotations.PageProperties;
import com.mgiorda.annotations.PageURL;
import com.mgiorda.commons.SpringUtil;
import com.mgiorda.page.Browser;
import com.mgiorda.page.WebDriverFactory;

public abstract class AbstractPage {

	protected static final class PageElement {

		private static final Log logger = LogFactory.getLog(PageElement.class);

		private final WebElement element;

		private PageElement(WebElement element) {
			this.element = element;
		}

		public void click() {
			logger.info(String.format("PageElement(%s) - Clicking", this.hashCode()));

			element.click();
		}

		public void submit() {
			logger.info(String.format("PageElement(%s) - Submitting", this.hashCode()));

			element.submit();
		}

		public void sendKeys(CharSequence... keysToSend) {

			String keys = "";
			for (CharSequence seq : keysToSend) {
				keys += seq.toString();
			}
			logger.info(String.format("PageElement(%s) - Sending keys '%s'", this.hashCode(), keys));

			element.sendKeys(keysToSend);
		}

		public void clear() {
			logger.info(String.format("PageElement(%s) - Clearing", this.hashCode()));

			element.clear();
		}

		public String getTagName() {
			return element.getTagName();
		}

		public String getAttribute(String name) {
			return element.getAttribute(name);
		}

		public boolean isSelected() {
			return element.isSelected();
		}

		public boolean isEnabled() {
			return element.isEnabled();
		}

		public String getText() {
			return element.getText();
		}

		public boolean isDisplayed() {
			return element.isDisplayed();
		}

		public Point getLocation() {
			return element.getLocation();
		}

		public Dimension getSize() {
			return element.getSize();
		}

		public String getCssValue(String propertyName) {
			return element.getCssValue(propertyName);
		}
	}

	protected static abstract class AbstractElement {

		protected final Log logger = LogFactory.getLog(this.getClass());

		protected final PageElement pageElement;

		public AbstractElement(PageElement pageElement) {
			this.pageElement = pageElement;
		}

		protected boolean existsElement(Locator elementLocator) {
			// TODO
			return false;
		}

		protected int countElements(Locator elementLocator) {
			// TODO
			return 0;
		}

		protected PageElement getElement(Locator elementLocator) {
			// TODO
			return null;
		}

		protected List<PageElement> getElements(Locator elementLocator) {
			// TODO
			return null;
		}
	}

	protected static final class Locator {

		private final Class<? extends By> byClass;

		private final String value;

		private Locator(Class<? extends By> byClass, String value) {

			if (value == null) {
				throw new IllegalArgumentException("Locator value cannot be null");
			}

			this.byClass = byClass;
			this.value = value;
		}

		public static Locator byId(String id) {
			return new Locator(By.ById.class, id);
		}

		public static Locator byLinkText(String linkText) {
			return new Locator(By.ByLinkText.class, linkText);
		}

		public static Locator byPartialLinkText(String partialLinkText) {
			return new Locator(By.ByPartialLinkText.class, partialLinkText);
		}

		public static Locator byName(String name) {
			return new Locator(By.ByName.class, name);
		}

		public static Locator byTagName(String tagName) {
			return new Locator(By.ByTagName.class, tagName);
		}

		public static Locator byXpath(String xpath) {
			return new Locator(By.ByXPath.class, xpath);
		}

		public static Locator byClass(String className) {
			return new Locator(By.ByClassName.class, className);
		}

		public static Locator byCssSelector(String cssSelector) {
			return new Locator(By.ByCssSelector.class, cssSelector);
		}
	}

	private static final Log staticLogger = LogFactory.getLog(AbstractPage.class);

	protected final Log logger = LogFactory.getLog(this.getClass());

	private final WebDriver driver;
	private final AbstractPage parentPage;

	private ApplicationContext applicationContext;

	private String pageUrl;

	@Autowired
	private WebDriverFactory driverHandler;

	@Value("${suite.waitTimeOut}")
	private int waitTimeOut;

	@Value("${suite.browser}")
	private Browser browser;

	protected AbstractPage() {

		Class<?> pageClass = this.getClass();
		PageURL annotation = pageClass.getAnnotation(PageURL.class);
		if (annotation == null) {
			throw new IllegalStateException("Cannot instantiate Page whitout String url constructor parameter or @PageURL class annotation");
		}

		String value = annotation.value();

		this.parentPage = null;

		initPageContext();

		pageUrl = applicationContext.getEnvironment().resolvePlaceholders(value);

		this.driver = driverHandler.getNewDriver(browser);

		goToUrl(pageUrl);
		AnnotationsSupport.initLocateBy(this);
	}

	protected AbstractPage(String url) {

		if (url == null) {
			throw new IllegalArgumentException("Url constructor parameter cannot be null");
		}
		this.parentPage = null;

		initPageContext();

		this.pageUrl = applicationContext.getEnvironment().resolvePlaceholders(url);

		this.driver = driverHandler.getNewDriver(browser);

		goToUrl(pageUrl);
		AnnotationsSupport.initLocateBy(this);
	}

	protected AbstractPage(AbstractPage parentPage, String url) {

		if (parentPage == null || url == null) {
			throw new IllegalArgumentException("ParentPage and url constructor parameters cannot be null");
		}

		this.parentPage = parentPage;

		// means not opening a new browser
		this.driver = parentPage.driver;
		this.waitTimeOut = parentPage.waitTimeOut;
		initPageContext();

		this.pageUrl = applicationContext.getEnvironment().resolvePlaceholders(url);

		goToUrl(pageUrl);
		AnnotationsSupport.initLocateBy(this);
	}

	public void takeScreenShot(String filePath) {

		TakesScreenshot screenShotDriver = null;
		try {
			screenShotDriver = (TakesScreenshot) driver;
		} catch (Exception e) {
			logger.warn(String.format("Driver '%s' cannot take screenshots", driver));
		}

		if (screenShotDriver != null) {
			File screenShot = screenShotDriver.getScreenshotAs(OutputType.FILE);

			try {
				logger.info(String.format("Saving screenshot to file '%s'", filePath));

				FileUtils.copyFile(screenShot, new File(filePath));
			} catch (IOException e) {

				throw new IllegalStateException(String.format("Exception trying to save screenshot to file '%s'", filePath), e);
			}
		}
	}

	public String getTitle() {
		return driver.getTitle();
	}

	public String getUrl() {
		return pageUrl;
	}

	public void quit() {
		if (parentPage != null) {
			parentPage.quit();
		} else {
			if (!driver.toString().contains("(null)")) {
				driver.quit();
			}
		}
	}

	void onTestFail() {

		ISuite currentTestSuite = TestThreadPoolManager.getCurrentTestSuite();
		String filePath = currentTestSuite.getOutputDirectory() + File.separator + "fail-photos" + File.separator + browser + File.separator + getCurrentTime() + ".png";

		takeScreenShot(filePath);
	}

	// So that never would be two photos with same time stamp
	private synchronized long getCurrentTime() {
		return new Date().getTime();
	}

	void onTestFinish() {
		this.quit();
	}

	boolean existsElement(Locator elementLocator) {

		boolean exists = true;
		By by = getLocatorByPlaceholder(elementLocator);

		try {
			waitForElement(by);

		} catch (TimeoutException e) {
			exists = false;
		}

		return exists;
	}

	int getElementCount(Locator elementLocator) {

		int count = 0;

		By by = getLocatorByPlaceholder(elementLocator);

		if (existsElement(elementLocator)) {

			List<WebElement> elements = driver.findElements(by);
			count = elements.size();
		}

		return count;
	}

	PageElement getElement(Locator elementLocator) {

		By by = getLocatorByPlaceholder(elementLocator);
		waitForElement(by);

		WebElement element = driver.findElement(by);

		PageElement pageElement = new PageElement(element);

		return pageElement;
	}

	List<PageElement> getElements(Locator elementLocator) {

		By by = getLocatorByPlaceholder(elementLocator);
		waitForElement(by);

		List<WebElement> elements = driver.findElements(by);

		List<PageElement> pageElements = new ArrayList<>();

		for (WebElement element : elements) {
			PageElement pageElement = new PageElement(element);

			pageElements.add(pageElement);
		}

		return Collections.unmodifiableList(pageElements);
	}

	boolean existsSubElement(PageElement pageElement, Locator elementLocator) {

		boolean exists = true;

		WebElement element = pageElement.element;
		By by = getLocatorByPlaceholder(elementLocator);

		try {
			waitForSubElement(element, by);

		} catch (TimeoutException e) {
			exists = false;
		}

		return exists;
	}

	int getSubElementCount(PageElement pageElement, Locator elementLocator) {

		int count = 0;

		WebElement element = pageElement.element;
		By by = getLocatorByPlaceholder(elementLocator);

		if (existsElement(elementLocator)) {

			List<WebElement> elements = element.findElements(by);
			count = elements.size();
		}

		return count;
	}

	PageElement getSubElement(PageElement pageElement, Locator elementLocator) {

		WebElement element = pageElement.element;
		By by = getLocatorByPlaceholder(elementLocator);

		waitForSubElement(element, by);

		WebElement subElement = element.findElement(by);
		PageElement pageSubElement = new PageElement(subElement);

		return pageSubElement;
	}

	List<PageElement> getSubElements(PageElement pageElement, Locator elementLocator) {

		WebElement element = pageElement.element;
		By by = getLocatorByPlaceholder(elementLocator);

		waitForSubElement(element, by);

		List<WebElement> elements = element.findElements(by);

		List<PageElement> pageElements = new ArrayList<>();

		for (WebElement subElement : elements) {
			PageElement pageSubElement = new PageElement(subElement);

			pageElements.add(pageSubElement);
		}

		return Collections.unmodifiableList(pageElements);
	}

	private void waitForElement(By by) throws TimeoutException {

		long start = new Date().getTime();

		new WebDriverWait(driver, waitTimeOut).until(ExpectedConditions.presenceOfElementLocated(by));

		long end = new Date().getTime();
		long waitTime = end - start;

		staticLogger.info(String.format("Found '%s' page element %s - Waited %s milliseconds", this.getClass().getSimpleName(), by, waitTime));
	}

	private void waitForSubElement(final WebElement element, final By by) throws TimeoutException {

		long start = new Date().getTime();

		ExpectedCondition<WebElement> presenceOfSubElement = new ExpectedCondition<WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				return element.findElement(by);
			}
		};

		new WebDriverWait(driver, waitTimeOut).until(presenceOfSubElement);

		long end = new Date().getTime();
		long waitTime = end - start;

		staticLogger.info(String.format("Found '%s' nested page element '%s' - Waited %s milliseconds", this.getClass().getSimpleName(), by, waitTime));
	}

	private void goToUrl(String url) {

		driver.navigate().to(url);
		long loadTime = waitForPageToLoad();

		staticLogger.info(String.format("Navigated form page '%s' to url '%s' - Waited %s milliseconds", this.getClass().getSimpleName(), url, loadTime));
	}

	private long waitForPageToLoad() {

		long start = new Date().getTime();

		new WebDriverWait(driver, waitTimeOut).until(new Predicate<WebDriver>() {

			@Override
			public boolean apply(WebDriver driver) {
				JavascriptExecutor js = (JavascriptExecutor) driver;
				Object obj = js.executeScript("return document.readyState");
				if (obj == null) {
					return false;
				}
				String str = (String) obj;
				if (str.equals("complete")) {
					return true;
				}
				return false;
			}
		});

		long end = new Date().getTime();
		long waitTime = end - start;

		return waitTime;
	}

	private void initPageContext() {

		String[] locations = { "classpath:/context/page-context.xml" };

		String[] contextLocations = getContextLocations();
		locations = ArrayUtils.addAll(locations, contextLocations);

		applicationContext = new GenericXmlApplicationContext(locations);

		Properties suiteProperties = TestThreadPoolManager.getSuitePropertiesForPage();
		SpringUtil.addProperties(applicationContext, suiteProperties);

		addPageProperties();

		SpringUtil.autowireBean(applicationContext, this);
		TestThreadPoolManager.registerPage(this);
	}

	private void addPageProperties() {

		Class<?> pageClass = this.getClass();

		InputStream resource = pageClass.getResourceAsStream(pageClass.getSimpleName() + ".properties");
		if (resource != null) {
			Properties properties = new Properties();
			try {
				properties.load(resource);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			SpringUtil.addProperties(applicationContext, properties);
		}

		PageProperties annotation = pageClass.getAnnotation(PageProperties.class);
		if (annotation != null) {

			String[] values = annotation.value();

			for (String propertySource : values) {
				SpringUtil.addPropertiesFile(applicationContext, propertySource);
			}
		}
	}

	private String[] getContextLocations() {

		String[] contextLocations = {};

		Class<?> testClass = this.getClass();
		PageContext annotation = testClass.getAnnotation(PageContext.class);
		if (annotation != null) {
			contextLocations = annotation.value();
		}

		return contextLocations;
	}

	private By getLocatorByPlaceholder(Locator elementLocator) {

		String replacedValue = applicationContext.getEnvironment().resolvePlaceholders(elementLocator.value);
		Class<? extends By> byClass = elementLocator.byClass;
		try {
			Constructor<? extends By> constructor = byClass.getConstructor(String.class);
			By byLocator = constructor.newInstance(replacedValue);

			return byLocator;

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
