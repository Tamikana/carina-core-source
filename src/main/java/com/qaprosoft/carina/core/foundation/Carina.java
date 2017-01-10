package com.qaprosoft.carina.core.foundation;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;

public class Carina {
	private static final Logger LOGGER = Logger.getLogger(Carina.class);

	protected static ThreadLocal<AbstractTest> tests = new ThreadLocal<AbstractTest>();

	public static void executeBeforeSuite(ITestContext context) throws Throwable {
		LOGGER.info("BeforeSuite...");
		DriverMode driverMode = Configuration.getDriverMode();
		if (driverMode == DriverMode.NONE) {
			APITest currentTest = new APITest();
			currentTest.executeBeforeTestSuite(context);
			setTest(currentTest);
		} else {
			UITest currentTest = new UITest();
			currentTest.executeBeforeTestSuite(context);
			setTest(currentTest);
		}
	}

	public static void executeBeforeClass(ITestContext context) throws Throwable {
		LOGGER.info("BeforeClass...");
		getTest().executeBeforeTestClass(context);
	}

	public static void executeAfterClass(ITestContext context) throws Throwable {
		LOGGER.info("AfterClass...");
		getTest().executeAfterTestClass(context);
	}

	public static void executeBeforeMethod(ITestResult result) throws Throwable {
		LOGGER.info("Beforemethod...");
		Method testMethod = result.getMethod().getConstructorOrMethod().getMethod();
		getTest().executeBeforeTestMethod(result.getTestContext().getCurrentXmlTest(), testMethod, result.getTestContext());
	}

	public static void executeAfterMethod(ITestResult result) throws Throwable {
		LOGGER.info("Aftermethod...");
		getTest().executeAfterTestMethod(result);
	}

	@AfterSuite(alwaysRun = true)
	public static void executeAfterSuite(ITestContext context) throws Throwable {
		LOGGER.info("AfterSuite...");
		getTest().executeAfterTestSuite(context);
	}
	
	
	public static WebDriver getDriver() {
		AbstractTest test = tests.get();
		if (test != null) {
			if (test.isUITest()) {
				return ((UITest) test).getDriver();
			} else {
				throw new RuntimeException("Only CarinaTest->UITest could find driver!");
			}
		} else {
			throw new RuntimeException("Unable to find driver using null CarinaTest->test class!");
		}
	}
	
	protected static void setTest(AbstractTest test) {
		tests.set(test);
	}
	
	protected static AbstractTest getTest() {
		return tests.get();
	}
}
