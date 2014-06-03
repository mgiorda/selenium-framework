package com.mgiorda.testng;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteLogger implements ISuiteListener {

	private static final Log logger = LogFactory.getLog(SuiteLogger.class);

	@Override
	public void onStart(ISuite suite) {
		logger.info(String.format("Starting test suite '%s'", suite.getName()));
	}

	@Override
	public void onFinish(ISuite suite) {
		logger.info(String.format("Finished test suite '%s'", suite.getName()));
	}
}
