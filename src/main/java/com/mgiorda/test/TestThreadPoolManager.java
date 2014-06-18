package com.mgiorda.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.ITestResult;

final class TestThreadPoolManager {

	private static final Map<Thread, ITestResult> threadTests = new HashMap<>();
	private static final Map<ITestResult, List<AbstractPage>> testPages = new HashMap<>();
	private static final Map<Thread, Properties> suiteProperties = new HashMap<>();
	private static final Map<ISuite, Thread> suiteThreads = new HashMap<>();

	private TestThreadPoolManager() {

	}

	public synchronized static void registerTest(ITestResult test) {

		Object testInstance = test.getInstance();
		if (!AbstractTest.class.isAssignableFrom(testInstance.getClass())) {

			throw new IllegalStateException("Cannot register non AbstractTest class - " + testInstance.getClass());
		}

		Thread thread = Thread.currentThread();
		threadTests.put(thread, test);
	}

	public synchronized static void registerPage(AbstractPage page) {

		ITestResult test = getCurrentTestResult();

		List<AbstractPage> pages = testPages.get(test);
		if (pages == null) {
			pages = new ArrayList<>();
			testPages.put(test, pages);
		}

		pages.add(page);
	}

	public synchronized static void registerSuiteProperties(Properties properties) {
		Thread thread = Thread.currentThread();
		suiteProperties.put(thread, properties);
	}

	public static void finishPages(ITestResult test) {

		List<AbstractPage> pages = testPages.get(test);
		if (pages != null) {
			for (AbstractPage page : pages) {
				page.onTestFinish();
			}
		}
	}

	public static AbstractTest getCurrentTest() {

		ITestResult test = getCurrentTestResult();

		AbstractTest absTest = (AbstractTest) test.getInstance();
		return absTest;
	}

	public static ISuite getCurrentTestSuite() {

		ITestResult currentTest = getCurrentTestResult();
		ISuite suite = currentTest.getTestContext().getSuite();

		return suite;
	}

	public static Properties getSuiteProperties() {

		Properties properties = null;

		ISuite suite = getCurrentTestSuite();
		Thread suiteThread = suiteThreads.get(suite);
		properties = suiteProperties.get(suiteThread);

		return properties;
	}

	private static ITestResult getCurrentTestResult() {
		Thread thread = Thread.currentThread();

		ITestResult test = threadTests.get(thread);
		if (test == null) {
			throw new IllegalStateException(String.format("Exception reading ITestResult for thread id %s", thread.getId()));
		}

		return test;
	}

	public static void registerSuite(ISuite suite) {
		Thread thread = Thread.currentThread();
		suiteThreads.put(suite, thread);
	}
}