package com.qaprosoft.carina.core.foundation.testng;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.listeners.CarinaTestNGListener;


@Listeners({ CarinaTestNGListener.class})
public class ListenerTest 
{
	@BeforeSuite() 
	public void beforeSuite() {
		System.out.println(CarinaTestNGListener.isSuiteRegistered());
	}
	

	@BeforeClass() 
	public void beforeClass() {
		System.out.println(CarinaTestNGListener.isSuiteRegistered());
	}

	@Test
	public void testConfigOverrride()
	{
		
	}
	
}
