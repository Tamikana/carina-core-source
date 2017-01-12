/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.listeners;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.Carina;

public class CarinaTestNGListener implements ITestListener {
	private static final Logger LOGGER = Logger.getLogger(CarinaTestNGListener.class);
	private static ThreadLocal<String> testClass = new ThreadLocal<String>();
	private static Boolean isSuiteRegistered = false;
	
	private static int methodsCount = 0;

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		LOGGER.info("onTestFailedButWithinSuccessPercentage");
	}
	
	@Override
	public void onStart(ITestContext context) {
		LOGGER.info("<tests> onStart");
		
		try {
			if (!isSuiteRegistered) {
				isSuiteRegistered = true;
				//really 1st execution
				methodsCount = context.getSuite().getAllMethods().size();
				Carina.executeBeforeSuite(context);
			} else {
				LOGGER.warn("Suite->onStart executed one more time!");
			}
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void onFinish(ITestContext context) {
		LOGGER.info("<tests> onFininsh");
		try {
			if (isAnyTestClassRegistered()) {
				//execute AfterClass as next test belongs to new class
				removeTestClass();
				Carina.executeAfterClass(context);
			}
		
			if (methodsCount == 0) {
				// execute afterSuite only when all testMethods were started as minimal
				LOGGER.info("onFinish...");
				Carina.executeAfterSuite(context);
			}
			Carina.executeAfterSuite(context);
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}

	}
	
	// TestMethods listeners

	@Override
	public void onTestStart(ITestResult result) {
		try {
			String className = result.getTestClass().getName();
			if (isTestClassRegistered(className)) {
				LOGGER.info("Already executed beforeClass for class: " + className);
			} else if (isAnyTestClassRegistered()) {
				//execute AfterClass as next test belongs to new class
				removeTestClass();
				Carina.executeAfterClass(result.getTestContext());
			} else {
				//keep information in Set<String> that beforeClass was already launched
				addTestClass(className);
				
				Carina.executeBeforeClass(result.getTestContext());
			}
			
			//execute before method anyway
			LOGGER.info("<method> onTestStart...");
			Carina.executeBeforeMethod(result);
			methodsCount--;

		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		LOGGER.info("<method> onTestSuccess...");
		try {
			Carina.executeAfterMethod(result);
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		LOGGER.info("<method> onTestFailure...");
		try {
			Carina.executeAfterMethod(result);
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		LOGGER.info("<method> onTestSkipped...");
		try {
			Carina.executeAfterMethod(result);
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}



	private void addTestClass(String testClassName) {
		testClass.set(testClassName);
	}
	
	private void removeTestClass() {
		testClass.remove();
	}
	
	private boolean isTestClassRegistered(String testClassName) {
		boolean res = false;
		if (testClass.get() != null) {
			res = testClass.get().equals(testClassName);
		}
		return res;
	}
	
	private boolean isAnyTestClassRegistered() {
		boolean res = false;
		if (testClass.get() != null) {
			res = !testClass.get().isEmpty();
		}
		return res;
	}

	
	// for unit tests
	public static int getMethodsCount() {
		return methodsCount;
	}
	
	public static String getCurrentTestClass() {
		String name = "";
		if (testClass.get() != "") {
			name = testClass.get();
		}
		return name;
	}
}
